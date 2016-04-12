package xos.adjustview;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;
    private BrightnessAdjustView adjustView;
    private GestureDetector mGestureDetector;
    private boolean isScroll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustView = new BrightnessAdjustView(this,"亮度",null,-5,5);
        adjustView.setPercentValue(false);
        adjustView.setValue(2);
        setContentView(adjustView);
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceY) < 20 && Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX < -10) {
                        adjustView.valueAdd();
                        isScroll = true;
                        return true;
                    } else if (distanceX > 10) {
                        adjustView.valueSubtract();
                        isScroll = true;
                        return true;
                    }
                }
                isScroll = false;
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityY) > Math.abs(velocityX) && !isScroll) {
                    if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE
                            && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                        adjustView.valueAdd();
                    } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE
                            && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                        adjustView.valueSubtract();
                    }
                }
                return false;
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        float density  = dm.density;      // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi;     // 屏幕密度（每寸像素：120/160/240/320）
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;

        Log.e(TAG, "xdpi=" + xdpi + "; ydpi=" + ydpi);
        Log.e( TAG, "density=" + density + "; densityDPI=" + densityDPI);

        int screenWidthDip = dm.widthPixels;        // 屏幕宽（dip，如：320dip）
        int screenHeightDip = dm.heightPixels;      // 屏幕宽（dip，如：533dip）

        Log.e(TAG, "screenWidthDip=" + screenWidthDip + "; screenHeightDip=" + screenHeightDip);

        int screenWidth  = (int)(dm.widthPixels * density + 0.5f);      // 屏幕宽（px，如：480px）
        int screenHeight = (int)(dm.heightPixels * density + 0.5f);     // 屏幕高（px，如：800px）

        Log.e(TAG, "screenWidth=" + screenWidth + "; screenHeight=" + screenHeight);

        Configuration config = getResources().getConfiguration();

        int  smallestScreenWidth = config.smallestScreenWidthDp;      // 屏幕最小宽
        int bestScreenWidth = config.screenWidthDp;

        Log.e(TAG, "smallestScreenWidth=" + smallestScreenWidth);
        Log.e(TAG, "bestScreenWidth=" + bestScreenWidth);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}
