package step.learning.android_spd111;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
private static final int FIELD_WIDTH = 16;
private static final int FIELD_HEIGHT = 24;

private TextView[][] gameField;
private final LinkedList<Vector2> snake = new LinkedList<>();
private final Handler handler = new Handler();
private int fieldColor;
private int snakeColor;
private Direction moveDirection;
private boolean isPlaying;
private static final String food = new String( Character.toChars(0x1F34E ));
private static final String bomb = new String( Character.toChars(0x1F4A3 ));
private Vector2 foodPosition;
private Vector2 bombPosition;
private Vector2 heartPosition;
private final ArrayList<Vector2> boombs = new ArrayList<>();
private final static Random _random = new Random();
private int food_amount;
private int speed_vew;
private int snake_speed;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        // Підключення TextView для швидкості та кількості їжі
        TextView speedTextView = findViewById(R.id.speedTextView);
        TextView foodTextView = findViewById(R.id.foodTextView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // додаємо аналізатор (слухач) свайпів на всю активність (R.id.main)
        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this) {
            @Override
            public void onSwipeBottom() {
                //Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.top ) { moveDirection = Direction.bottom; }
            }

            @Override
            public void onSwipeLeft() {
                //Toast.makeText(GameActivity.this, "Left", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.right ) { moveDirection = Direction.left; }
            }

            @Override
            public void onSwipeRight() {
                //Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.left ) { moveDirection = Direction.right; }
            }

            @Override
            public void onSwipeTop() {
               //Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.bottom ) { moveDirection = Direction.top; }
            }
        });

        fieldColor = getResources().getColor(R.color.game_field, getTheme());
        snakeColor = getResources().getColor(R.color.game_snake, getTheme());
        snake_speed = 700;
        food_amount = 0;
        speed_vew = 3;
        // Оновлення значення TextView із змінних класу GameActivity
        speedTextView.setText("Speed: " + speed_vew);
        foodTextView.setText("Food: " + food_amount);

        // Після отримання посилань на TextView, оновлюємо їх значення відповідно до значень полів класу
        updateSpeedAndFoodViews();
        initField();
        newGame();
        boombs.clear();
    }

private void updateSpeedAndFoodViews() {
        TextView speedTextView = findViewById(R.id.speedTextView);
        TextView foodTextView = findViewById(R.id.foodTextView);

        speedTextView.setText("Speed: " + speed_vew);
        foodTextView.setText("Food: " + food_amount);
    }

    private void step() {
        if(!isPlaying) return;
        Vector2 tail = snake.getLast();
        Vector2 head = snake.getFirst();
        Vector2 newHead = new Vector2(head.x, head.y);
        switch (moveDirection) {
            case bottom: newHead.y += 1; break;
            case left:   newHead.x -= 1; break;
            case right:  newHead.x += 1; break;
            case top:    newHead.y -= 1; break;
        }
        // якщо намагається вийти за поля
if(newHead.x < 0 || newHead.x >= FIELD_WIDTH ||
newHead.y < 0 || newHead.y >= FIELD_HEIGHT ) {
    gameOver();
    return;
}
// якщо вкусила сама себе
if(isCellInSnake( newHead )) {
    gameOver();
    return;
}
// Перевірка на зіткнення з бомбою
 boolean collidedWithBomb = false;
 for (Vector2 bom : boombs) {
     if (bom.x == newHead.x && bom.y == newHead.y) {
         collidedWithBomb = true;
         break;
     }
 }
 if (collidedWithBomb) {
     gameOver();
     return;
 }


if(newHead.x == foodPosition.x && newHead.y == foodPosition.y ) {
// поїдання яблука - перенести яблуко але щоб не на змію
    gameField[foodPosition.x][foodPosition.y].setText( "" );
    food_amount ++;
    if(snake_speed > 20) { snake_speed -= 20; }
    if(speed_vew < 105) { speed_vew++; }
    do {
        foodPosition  = Vector2.random();
    }while( isCellInSnake( foodPosition ) );
    do {
        bombPosition  = Vector2.random();
    }while( isCellInSnake( bombPosition ) );
    gameField[foodPosition.x][foodPosition.y].setText( food );
    gameField[bombPosition.x][bombPosition.y].setText( bomb );
    boombs.add(bombPosition);

}
else {
    snake.remove(tail);
    gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
}

     snake.addFirst(newHead);
     gameField[newHead.x][newHead.y].setBackgroundColor(snakeColor);

     handler.postDelayed(this::step, snake_speed);
     updateSpeedAndFoodViews();
}


    private boolean isCellInSnake(Vector2 cell) {
        for(Vector2 v: snake) {
            if(v.x == cell.x && v.y == cell.y) return true;
        }
        return false;
    }
    private void initField() {
        LinearLayout field = findViewById( R.id.game_field );

        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(
                0,
                // ViewGroup.LayoutParams.WRAP_CONTENT  // прив'язано до розміру контенту
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tvLayoutParams.weight = 1f;
        tvLayoutParams.setMargins(4,4,4,4);

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
        );
        rowLayoutParams.weight = 1f;

        gameField = new TextView[FIELD_WIDTH][FIELD_HEIGHT];
    for(int j = 0; j < FIELD_HEIGHT; j++) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation( LinearLayout.HORIZONTAL );
        row.setLayoutParams( rowLayoutParams );
        for (int i = 0; i < FIELD_WIDTH; i++) {
            TextView tv = new TextView(this);
            tv.setBackgroundColor(fieldColor);  //////////
            // tv.setText("0");
            tv.setLayoutParams(tvLayoutParams);
            row.addView(tv);
            gameField[i][j] = tv;
        }
        field.addView( row );
    }

    }
    private void newGame() {
        for(Vector2 v: snake) {
            gameField[v.x][v.y].setBackgroundColor(fieldColor);
        }
        snake.clear();
        if(foodPosition != null) {
            gameField[foodPosition.x][foodPosition.y].setText( "" );
        }

        snake.add(new Vector2(8, 10) );
        snake.add(new Vector2(8, 11) );
        snake.add(new Vector2(8, 12) );
        snake.add(new Vector2(8, 13) );
        snake.add(new Vector2(8, 14) );
        for(Vector2 v: snake) {
           gameField[v.x][v.y].setBackgroundColor(snakeColor);
        }
        foodPosition = new Vector2(3,14);
        gameField[foodPosition.x][foodPosition.y].setText( food );
        moveDirection = Direction.top;
        isPlaying = true;
        step();
    }
    private void gameOver() {
        isPlaying = false;
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Play one more time?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> newGame())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
        boombs.clear();
        snake_speed = 700;
        food_amount = 0;
        speed_vew = 3;
    }
    @Override
    protected void onPause() { // подія деактивації
        super.onPause();
        isPlaying = false;
    }

    @Override
    protected void onResume() { // подія активації
        super.onResume();
        if(!isPlaying) {
            isPlaying = true;
            step();
        }
    }

    static class Vector2 {
        int x;
        int y;

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public static Vector2 random() {
            return new Vector2(_random.nextInt(FIELD_WIDTH), _random.nextInt(FIELD_HEIGHT));
        }
    }
    enum Direction {
        bottom,
        left,
        right,
        top
    }
}
