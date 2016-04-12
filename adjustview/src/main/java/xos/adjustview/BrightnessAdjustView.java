package xos.adjustview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by ykj on 16/4/8.
 */
public class BrightnessAdjustView extends AdjustView {
    private static final String TAG = "BrightnessAdjustView";

    protected static float CURRENT_VALUE_STROKE_WIDTH;

    protected RectF currentValueOval;
    protected float currentValueRadius;
    protected Paint currentValuePaint;

    protected Drawable addDrawable;
    protected Drawable subDrawable;

    public BrightnessAdjustView(Context context, String tipTextCN, String tipTextEN, int maxValue) {
        this(context, tipTextCN, tipTextEN, 0, maxValue);
    }

    public BrightnessAdjustView(Context context, String tipTextCN, String tipTextEN, int minValue, int maxValue) {
        super(context, tipTextCN, tipTextEN, minValue, maxValue);
        addDrawable = getResources().getDrawable(R.drawable.icn_add);
        subDrawable = getResources().getDrawable(R.drawable.icn_minus);
        CURRENT_VALUE_STROKE_WIDTH = VARIABLE_STROKE_WIDTH + VARIABLE_STROKE_WIDTH / 5;
        currentValuePaint = new Paint(variablePaint);
        currentValuePaint.setColor(Color.parseColor("#1AFFFF"));
        currentValuePaint.setStrokeWidth(CURRENT_VALUE_STROKE_WIDTH); //设置宽度
        currentValueRadius = (basicOutsideRadius + basicInsideRadius) / 2;
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
    }

    protected void drawIcn(Canvas canvas){
        addDrawable.draw(canvas);
        subDrawable.draw(canvas);
    }

    @Override
    protected void drawVariableLine(Canvas canvas) {
        canvas.drawArc(variableOval, 150, 240, false, backVariablePaint);
        variablePaint.setColor(Color.parseColor("#00F1EF"));
        canvas.drawArc(variableOval, 270, getCurrentScale() * 120, false, variablePaint);
        canvas.drawArc(currentValueOval, 270 + getCurrentScale() * 120 - 1, 2, false, currentValuePaint);
    }

    @Override
    public float getCurrentScale() {
        int total = Math.abs(maxValue - minValue) / 2;
        int numerator = (value - total) - minValue;
        return (float) numerator / total;
    }
}
