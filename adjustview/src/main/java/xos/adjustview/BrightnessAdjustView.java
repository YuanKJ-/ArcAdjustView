package xos.adjustview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;

/**
 * Created by ykj on 16/4/8.
 */
public class BrightnessAdjustView extends AdjustView {
    private static final String TAG = "BrightnessAdjustView";

    protected static String OFF_STR = "OFF";
    protected static String CLOSE_STR = "关闭显示";

    protected static float CURRENT_VALUE_STROKE_WIDTH;
    protected static float BRIGHTNESS_TEXT_MARGIN_CENTER;
    protected static float BRIGHTNESS_TEXT_SIZE;
    protected static float BRIGHTNESS_CLOSE_TEXT_SIZE;
    protected static float BRIGHTNESS_CLOSE_TEXT_MARGIN_LEFT;

    protected RectF currentValueOval;
    protected float currentValueRadius;
    protected Paint currentValuePaint;

    protected TextPaint closeTextPaint;

    protected Drawable addDrawable;
    protected Drawable subDrawable;

    protected boolean closing = false; //准备关闭屏幕
    protected boolean closed = false; //已经关闭屏幕

    public BrightnessAdjustView(Context context, String tipTextCN, int maxValue) {
        this(context, tipTextCN, 0, maxValue);
    }

    public BrightnessAdjustView(Context context, String tipTextCN, int minValue, int maxValue) {
        super(context, tipTextCN, null, minValue, maxValue);
        addDrawable = getResources().getDrawable(R.drawable.icn_add);
        subDrawable = getResources().getDrawable(R.drawable.icn_minus);


        currentValuePaint = new Paint(variablePaint);
        currentValuePaint.setColor(Color.parseColor("#1AFFFF"));
        currentValuePaint.setStrokeWidth(CURRENT_VALUE_STROKE_WIDTH); //设置宽度
        currentValueRadius = (basicOutsideRadius + basicInsideRadius) / 2;

        variablePaint.setColor(Color.parseColor("#00F1EF"));

        topTextPaint.setTextSize(BRIGHTNESS_TEXT_SIZE);

        closeTextPaint = new TextPaint(topTextPaint);
        closeTextPaint.setTextSize(BRIGHTNESS_CLOSE_TEXT_SIZE);
        closeTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void initDimens() {
        super.initDimens();
        CURRENT_VALUE_STROKE_WIDTH = VARIABLE_STROKE_WIDTH + VARIABLE_STROKE_WIDTH / 5;
        BRIGHTNESS_TEXT_MARGIN_CENTER = getResources().getDimension(R.dimen.brightness_text_margin_center);
        BRIGHTNESS_TEXT_SIZE = getResources().getDimension(R.dimen.brightness_text_size);
        BRIGHTNESS_CLOSE_TEXT_SIZE = getResources().getDimension(R.dimen.brightness_close_text_size);
        BRIGHTNESS_CLOSE_TEXT_MARGIN_LEFT = getResources().getDimension(R.dimen.brightness_close_text_margin_left);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(centerPoint != null && addDrawable != null) {
            int left = (int)(centerPoint.x + basicOutsideRadius);
            int top = (int)(centerPoint.y - basicOutsideRadius + (basicOutsideRadius - basicInsideRadius) / 2);
            addDrawable.setBounds(left, top, left + addDrawable.getIntrinsicWidth(), top + addDrawable.getIntrinsicHeight());
        }
        if(centerPoint != null && subDrawable != null) {
            int left = (int)(centerPoint.x - basicOutsideRadius - subDrawable.getIntrinsicWidth());
            int top = (int)(centerPoint.y - basicOutsideRadius + (basicOutsideRadius - basicInsideRadius) / 2);
            subDrawable.setBounds(left, top, left + subDrawable.getIntrinsicWidth(), top + subDrawable.getIntrinsicHeight());
        }
        if (centerPoint != null && currentValueOval == null) {
            currentValueOval = new RectF(centerPoint.x - currentValueRadius, centerPoint.y - currentValueRadius,
                    centerPoint.x + currentValueRadius, centerPoint.y + currentValueRadius);  //用于定义的圆弧的形状和大小的界限
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIcn(canvas);
        drawCloseBlock(canvas);
    }

    protected void drawIcn(Canvas canvas){
        addDrawable.draw(canvas);
        subDrawable.draw(canvas);
    }

    protected void drawCloseBlock(Canvas canvas){
        // close block back variable
        canvas.drawArc(variableOval, 90, 57, false, backVariablePaint);
        // close block basic line
        canvas.drawArc(basicOutsideOval, 90, 57, false, shadowOutsidePaint);
        canvas.drawArc(basicOutsideOval, 90, 57, false, basicOutsidePaint);
        canvas.drawArc(basicInsideOval, 90, 57, false, basicInsidePaint);
        if(closing){
            canvas.drawArc(variableOval, 90, 57, false, variablePaint);
        }
    }

    @Override
    protected void drawVariableLine(Canvas canvas) {
        canvas.drawArc(variableOval, 150, 240, false, backVariablePaint);
        canvas.drawArc(variableOval, 270, getCurrentScale() * 120, false, variablePaint);
        canvas.drawArc(currentValueOval, 270 + getCurrentScale() * 120 - 1, 2, false, currentValuePaint);
    }

    @Override
    protected void drawText(Canvas canvas) {
        canvas.drawText(CLOSE_STR, centerPoint.x + BRIGHTNESS_CLOSE_TEXT_MARGIN_LEFT, centerPoint.y + BASIC_OUTSIDE_RADIUS, closeTextPaint);

        String val = isPercentValue ? String.valueOf((int) (getCurrentScale() * 100)) : String.valueOf(value); //百分比或实际值显示
        if(closing) val = OFF_STR;
        float valueHeight = getTextHeight(val, valueTextPaint);
        canvas.drawText(val, centerPoint.x, centerPoint.y + (int)(valueHeight / 2), valueTextPaint);
        if(!closing) {
            String topText = tipTextCN;
            float topTextHeight = getTextHeight(topText, topTextPaint);
            canvas.drawText(topText, centerPoint.x, centerPoint.y + BRIGHTNESS_TEXT_MARGIN_CENTER + (int) topTextHeight, topTextPaint);
        }
    }

    @Override
    public float getCurrentScale() {
        int total = Math.abs(maxValue - minValue) / 2;
        int numerator = (value - total) - minValue;
        return (float) numerator / total;
    }

    @Override
    public void valueAdd() {
        if (screenBright()) return;
        if (closing) {
            closing = false;
            this.postInvalidate();
            //取消熄屏回调
        } else {
            super.valueAdd();
        }
    }

    @Override
    public void valueSubtract() {
        if (screenBright()) return;
        if (!closing && value == minValue) {
            Log.d(TAG, "valueSubtract: 可以关闭显示了");
            closing = true;
            this.postInvalidate();
            //延迟1秒做熄屏回调
        } else {
            super.valueSubtract();
        }
    }

    protected boolean screenBright() {
        if (closed) {
            //亮屏回调
            closed = false;
            closing = false;
            return true;
        }
        return false;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
