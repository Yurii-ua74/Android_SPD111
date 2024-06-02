package step.learning.android_spd111;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.drawable.ColorDrawable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalcActivity extends AppCompatActivity {
    private Animation clickAnimation;

    private boolean isActClicked = false; // флаг
    private CalcSwiper calcSwiper;
    private double res;
    private double sum;
    private double memory;
    private TextView tvHistory;
    private TextView tvResult;
    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {   // start калькулятора
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // завантаження анімації
        clickAnimation = AnimationUtils.loadAnimation(this, R.anim.calc);

        tvHistory = findViewById( R.id.calc_tv_history );
        tvResult  = findViewById( R.id.calc_tv_result  );
        if(savedInstanceState == null) { // якщо немає збереженого стану - пеший запуск
            tvResult.setText("0");
        }
        calcSwiper = new CalcSwiper(this, this); // Передаємо посилання на саму активність this
        findViewById(R.id.main).setOnTouchListener(calcSwiper); // Призначаємо CalcSwiper обробником подій для вашого View


        /* Задача: циклом перебрати ресурсні кнопки calc_btn_{i}
        та для кожної з них поставили один обробник onDigitButtonClick
        */
        for(int i=0; i < 10; i++) {
            findViewById( // на заміну R.id.calc_btn_0 приходить наступний вираз
                    getResources().getIdentifier(            // R
                                   "calc_btn_" + i,          // calc_btn_0
                                   "id",                     // .id
                                   getPackageName()
                            )
            ).setOnClickListener(this::onDigitButtonClick);
        }
        findViewById(R.id.calc_btn_inverse   ).setOnClickListener(this::onInverseClick  );
        findViewById(R.id.calc_btn_square    ).setOnClickListener(this::onSquareClick   );
        findViewById(R.id.calc_btn_sqrt      ).setOnClickListener(this::onSqrtClick     );
        findViewById(R.id.calc_btn_plus_minus).setOnClickListener(this::onPlusMinusClick);
        findViewById(R.id.calc_btn_ce        ).setOnClickListener(this::onCEClick  );
        findViewById(R.id.calc_btn_c         ).setOnClickListener(this::onCClick   );
        findViewById(R.id.calc_btn_percent   ).setOnClickListener(this::onPercentClick    );
        findViewById(R.id.calc_btn_divide    ).setOnClickListener(this::onMathActionClick );
        findViewById(R.id.calc_btn_multiply  ).setOnClickListener(this::onMathActionClick );
        findViewById(R.id.calc_btn_minus     ).setOnClickListener(this::onMathActionClick );
        findViewById(R.id.calc_btn_plus      ).setOnClickListener(this::onMathActionClick );
        findViewById(R.id.calc_btn_comma     ).setOnClickListener(this::onCommaClick  );
        findViewById(R.id.calc_btn_mc        ).setOnClickListener(this::onMCClick     );
        findViewById(R.id.calc_btn_mr        ).setOnClickListener(this::onMRClick     );
        findViewById(R.id.calc_btn_ms        ).setOnClickListener(this::onMSClick     );
        findViewById(R.id.calc_btn_m_plus    ).setOnClickListener(this::onMPlusClick  );
        findViewById(R.id.calc_btn_m_minus   ).setOnClickListener(this::onMMinusClick );
        findViewById(R.id.calc_btn_equal     ).setOnClickListener(this::onEqualClick  );
        findViewById(R.id.calc_btn_backspace ).setOnClickListener(this::onBackspaceClick );
    }

    // анімація кнопок
    private void animateButton(View view, int color) {

        // Анімація зміни розміру
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.1f,  // Від початкового розміру до 110%
                1.0f, 1.1f,        // Від початкового розміру до 110%
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300); // Тривалість анімації в мілісекундах
        scaleAnimation.setFillAfter(false); // Не залишати розмір після завершення анімації

        // Анімація зміни кольору
        //ObjectAnimator colorAnimation = ObjectAnimator.ofObject(
        //        view, "backgroundColor", new ArgbEvaluator(),
        //        initialColor, Color.RED
        //);
        //colorAnimation.setDuration(300); // Тривалість анімації в мілісекундах
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), color, Color.RED);
        colorAnimation.setDuration(300); // Тривалість анімації в мілісекундах
        colorAnimation.addUpdateListener(animation -> view.setBackgroundColor((int) animation.getAnimatedValue()));

        // Зворотна анімація зміни кольору
        //ObjectAnimator reverseColorAnimation = ObjectAnimator.ofObject(
        //        view, "backgroundColor", new ArgbEvaluator(),
        //        Color.RED, initialColor
        //);
        //reverseColorAnimation.setDuration(300); // Тривалість анімації в мілісекундах
        ValueAnimator reverseColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED, color);
        reverseColorAnimation.setDuration(300); // Тривалість анімації в мілісекундах
        reverseColorAnimation.addUpdateListener(animation -> view.setBackgroundColor((int) animation.getAnimatedValue()));


        // Комбінування анімацій
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f).setDuration(700),
                ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f).setDuration(700),
                colorAnimation
        );

        // Додати слухача, щоб запустити зворотну анімацію після завершення
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Зворотня анімація після завершення початкової анімації
                AnimatorSet reverseAnimatorSet = new AnimatorSet();
                reverseAnimatorSet.playTogether(
                        ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1.0f).setDuration(300),
                        ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1.0f).setDuration(300),
                        reverseColorAnimation
                );
                reverseAnimatorSet.start();
            }
        });
        animatorSet.start();
    }

    // кнопки пам'яті
    private void onMCClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_bg));

        memory = 0;
    }

    private void onMRClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_bg));

        tvResult.setText(String.valueOf(memory));
        isActClicked = true;
    }

    private void onMSClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_bg));

        String result = tvResult.getText().toString();
        memory = Double.parseDouble(result);
        isActClicked = true;
    }

    private void onMPlusClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_bg));

        String result = tvResult.getText().toString();
        memory += Double.parseDouble(result);
        isActClicked = true;
    }

    private void onMMinusClick(View view) {
        // анімація кнопок
        view.startAnimation(clickAnimation);

        String result = tvResult.getText().toString();
        memory -= Double.parseDouble(result);
        isActClicked = true;
    }

    public static ArrayList<String> parseForComma(String expression) {
        ArrayList<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("[.^]|[0-9]+");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    public static ArrayList<String> parseExpression(String expression) {
       ArrayList<String> tokens = new ArrayList<>();
       Pattern pattern = Pattern.compile("[-+*/%^]|[0-9]*\\.?[0-9]+");
       Matcher matcher = pattern.matcher(expression);

       while (matcher.find()) {
           tokens.add(matcher.group());
       }
        return tokens;
    }

    private void onBackspaceClick(View view) {
        String result = tvResult.getText().toString();         // Отримуємо поточний результат зображений на екрані калькулятора
        if (!result.isEmpty()) {                               // перевіряємо, чи є в строкі щось для видалення
            result = result.substring(0, result.length() - 1); // вирізаємо останній символ
        }
        tvResult.setText(result);
    }

    void onCalcSwipeLeft() {
        onBackspaceClick(null); //  null або view
        Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show();
    }

    private void onCommaClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_digit_bg));

        String result = tvResult.getText().toString();        // Отримуємо поточний результат зображений на екрані калькулятора
        String str = result ;
        ArrayList<String> tokens = parseForComma(str);
        for (String token : tokens) {
            if (token.equals(".")) {
                return;
            }
        }
        result += ((Button) view).getText();
        tvResult.setText( result );
    }

    private void onMathActionClick(View view) {               // +, -
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

        String result = tvResult.getText().toString();        // Отримуємо поточний результат зображений на екрані калькулятора
        String historyText = tvHistory.getText().toString();  // Отримуємо поточний текст історії зображений на екрані калькулятора
        String action = ((Button) view).getText().toString(); // Отримуємо текст кнопки, на яку натиснули (дія)

        // Формуємо новий текст історії з дією, додавши його до існуючого тексту історії
        String str = historyText + result + action;
        //String str = "78+12-5*2-30/3";
        ArrayList<String> tokens = parseExpression(str);
        String num_token1 = null;
        res = Double.parseDouble(tokens.get(0));

             for (int i = 0; i < tokens.size() - 1; i++) {
                 if (tokens.size() >= 3) {
                     switch(tokens.get(i)) {
                         case "*": {
                             //    // Перетворюємо значення змінних у тип double
                             //    double sum1 = Double.parseDouble(tokens.get(i - 1));
                             res = Double.parseDouble(tokens.get(i - 1));
                             double sum2 = Double.parseDouble(tokens.get(i + 1));
                             res *= sum2;
                             //    double result_sum = sum1 * sum2;
                             tokens.remove(i - 1); // видаляємо лівий операнд
                             tokens.remove(i - 1); // видаляємо оператор
                             tokens.remove(i - 1); // видаляємо правий операнд
                             tokens.add(i - 1, Double.toString(res)); // замінюємо на результат
                             if (i > 0) i--;
                             break;
                             //    if(tokens.size() == 1 ) res = Double.parseDouble(tokens.get(i));
                         }
                         case "/":  {
                             //    double sum1 = Double.parseDouble(tokens.get(i - 1));
                             res = Double.parseDouble(tokens.get(i - 1));
                             double sum2 = Double.parseDouble(tokens.get(i + 1));
                             res /= sum2;
                             //    double result_sum = sum1 / sum2;
                             tokens.remove(i - 1); // видаляємо лівий операнд
                             tokens.remove(i - 1); // видаляємо оператор
                             tokens.remove(i - 1); // видаляємо правий операнд
                             tokens.add(i - 1, Double.toString(res)); // замінюємо на результат
                             if (i > 0) i--;
                             break;
                             //    if(tokens.size() == 1 ) res = result_sum;
                         }
                     }
                 }
             }

             if(tokens.size() >= 3 ) {
                 res = Double.parseDouble(tokens.get(0));
                    for (int j = 0; j < tokens.size() - 1; j++) {
                        if (tokens.get(j).equals("+")) {
                            num_token1 = tokens.get(j + 1);
                            // Перетворюємо значення змінних у тип double
                            double num1 = Double.parseDouble(num_token1);
                            // Виконуємо математичний вираз
                            res += num1;
                        }
                        if (tokens.get(j).equals("-")) {
                            num_token1 = tokens.get(j + 1);
                            // Перетворюємо значення змінних у тип double
                            double num1 = Double.parseDouble(num_token1);
                            // Виконуємо математичний вираз
                            res -= num1;
                        }
                    }
            }
        String str_final = (res == (int)res) ? String.valueOf((int)res) : String.valueOf(res);
        tvResult.setText(str_final);
        sum = res;
        isActClicked = true;  // означає що спрацювала одна із кнопок  +, -
        tvHistory.setText( str );
    }

    private void onEqualClick(View view) { // =
        onMathActionClick(view);

        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_equal_bg));

        tvHistory.setText("");
        String str = (sum == (int)sum) ? String.valueOf((int)sum) : String.valueOf(sum);
        tvResult.setText(str);
    }

    private void onCClick(View view ) {   // C
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

        tvResult.setText("0");
        tvHistory.setText("");
    }

    private void onCEClick(View view) {   // CE
        // анімація кнопок
        view.startAnimation(clickAnimation);

        tvResult.setText("0");
    }

    private void onPercentClick(View view) {
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

        String result = tvResult.getText().toString();        // Отримуємо поточний результат зображений на екрані калькулятора
        String historyText = tvHistory.getText().toString();  // Отримуємо поточний текст історії зображений на екрані калькулятора
        String action = ((Button) view).getText().toString(); // Отримуємо текст кнопки, на яку натиснули (дія)
        double sum1 = Double.parseDouble(result);
        double sum2 = 0;
        String str = result + action + historyText;
        String sumStr = "";
        if(!historyText.isEmpty()) {
        sum2 = Double.parseDouble(historyText); }
        ArrayList<String> tokens = parseExpression(str);
        if(tokens.size() < 3) {
            sum1 /= 100;
            sumStr = String.valueOf(sum1);
        }
        else {
                sum1 *= sum2;
                sumStr = "";
        }
        isActClicked = true;
        result = (sum1 == (int)sum1) ? String.valueOf((int)sum1) : String.valueOf(sum1);
        tvResult.setText( str );
        tvResult.setText( result );
        tvHistory.setText( sumStr );
    }

    private void onPlusMinusClick(View view) {  // плюс мінус знак
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_digit_bg));

        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x = -1 * x;
        String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);
        tvResult.setText( str );
    }

    private void onSqrtClick(View view) {   // корінь квадратний
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x = Math.sqrt(x);
        String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);

        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText( str );
    }
    private void onSquareClick(View view) {   //  число в квадраті
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x = x * x;
        String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);

        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText( str );
    }

    private void onInverseClick(View view) {  //  1/х
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_func_bg));

         String result = tvResult.getText().toString();
         double x = Double.parseDouble(result);
         if(x == 0) {
             Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
             return;
         }
         x = 1.0 / x;
         String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);

        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText( str );
    }

    /* При зміні конфігурації пристрою (поворотах, змінах налаштуань)
     відбувається перезапуск активності
     при цьому подаються події життєвого циклу
     onSaveInstanceState - при виході з активності перед перезапуском
     та
     onRestoreInstanceState - при відновленні активності після перезапуску
     До обробників додається Bundle,  що є сховищем для збереження та
     відновлення даних
     Також збережений Bundle передається до onCreate,що дозволяє визначити
     чи це перший запуск, чи перезапуск через зміну конфігурації
     */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tvResult", tvResult.getText()); // текст вьюшки в CharSequence
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
    }

    private void onDigitButtonClick(View view) {  // цифрові кнопки
        // анімація кнопок
        //view.startAnimation(clickAnimation);
        // Виклик авторського методу анімації кнопок
        animateButton(view, getResources().getColor(R.color.calc_digit_bg));

         if (isActClicked) {
             tvResult.setText("0");
             isActClicked = false;
         }
         String result = tvResult.getText().toString();
         if(result.length() >= 10) {
             Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
             return;
         }
         if(result.equals("0")) {
             result = "";
         }
         result += ((Button) view).getText();
         tvResult.setText( result );
    }
}