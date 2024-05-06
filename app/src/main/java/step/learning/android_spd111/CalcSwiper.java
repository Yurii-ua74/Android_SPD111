package step.learning.android_spd111;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CalcSwiper implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private final CalcActivity calcActivity;

    public CalcSwiper(Context context, CalcActivity calcActivity) {
        this.calcActivity = calcActivity;
        this.gestureDetector = new GestureDetector(calcActivity, new SwipeGestureListener());
    }

    // оголошення інтерфейсного методу для переозначення у класах-слухачах
    //public void onCalcSwipeLeft() {}

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class SwipeGestureListener
            extends GestureDetector.SimpleOnGestureListener {
        private static final int MIN_DISTANCE = 70;
        private static final int MIN_VELOCITY = 80;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;  // ознака оброблення - запобігаємо Click
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isDispatched = false;
            if(e1 != null) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                // відстань (без урахування знаку) по горизонталі більша, ніж
                // по вертикалі, значить це або лівий, або правий свайп
                if(Math.abs(distanceX) > Math.abs(distanceY)) {
                    // горизонтальний свайп - цікавить тільки горизонтальні
                    // відстань та швидкість
                    if(Math.abs(distanceX) > MIN_DISTANCE &&
                            Math.abs(velocityX) > MIN_VELOCITY ) {
                        if(distanceX < 0) {
                            calcActivity.onCalcSwipeLeft();
                            isDispatched = true;
                        }
                    }
                }
            }
            return isDispatched;
        }
    }
}
