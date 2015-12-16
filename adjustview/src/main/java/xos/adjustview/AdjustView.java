package xos.adjustview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import xos.adjustview.R;

/**
 * Created by ykj on 15-9-15.
 */
public class AdjustView extends View {
    private final static String TAG = AdjustView.class.getSimpleName();

    private static float VALUE_TEXT_SIZE;
    private static float BACK_VARIABLE_STROKE_WIDTH;
    private static float VARIABLE_STROKE_WIDTH;
    private static float VARIABLE_RADIUS;
    private static float BASIC_OUTSIDE_STROKE_WIDTH;
    private static float BASIC_OUTSIDE_RADIUS;
    private static float SHADOW_OUTSIDE_STROKE_WIDTH;
    private static float BASIC_INSIDE_STROKE_WIDTH;
    private static float BASIC_INSIDE_RADIUS;
    private static float TEXT_TOP_MARGIN;
    private static float TEXT_MIDDLE_MARGIN;
    private static float TOP_TEXT_SIZE;
    private static float BOTTOM_TEXT_SIZE;

    /**
     * 数值变化监听器
     */
    private AdjustValueListener adjustValueListener;

    private Point centerPoint; //中心点

    /**
     * 可变化弧形进度条
     */
    private Paint variablePaint;
    private Paint backVariablePaint;
    private float variableRadius; //半径
    private RectF variableOval; //形状和大小

    /**
     * 内环与外环基线
     */
    private Paint basicOutsidePaint;
    private float basicOutsideRadius;
    private RectF basicOutsideOval; //形状和大小

    private Paint shadowOutsidePaint; //模拟外环阴影

    private Paint basicInsidePaint;
    private float basicInsideRadius;
    private RectF basicInsideOval;

    /**
     * 进度值文本
     */
    private TextPaint valueTextPaint;
    private TextPaint topTextPaint;
    private TextPaint bottomTextPaint;


    /**
     * 圆弧值
     */
    protected int value = 0;
    protected int minValue = 0;
    protected int maxValue = 100;

    protected boolean isPercentValue = true; //是否以百分比显示
    protected int totalTimes = 10; //转盘可转动次数，每次转动最小变化为1

    /**
     * 调节转盘提示文本
     */
    protected String tipTextCN = "转盘"; //中文
    protected String tipTextEN = "TURNTABLE"; //英文

    public AdjustView(Context context, String tipTextCN, String tipTextEN, int maxValue) {
        this(context, tipTextCN, tipTextEN, 0, maxValue);
    }

    public AdjustView(Context context, String tipTextCN, String tipTextEN, int minValue, int maxValue) {
        this(context);
        this.tipTextCN = tipTextCN;
        this.tipTextEN = tipTextEN;
        this.minValue = minValue;
        this.value = minValue;
        this.maxValue = maxValue;
    }

    public AdjustView(Context context) {
        this(context, null);
    }

    public AdjustView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdjustView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDimens();

        Paint paint = new Paint();
        paint.setStrokeJoin(Paint.Join.ROUND); //平滑效果
        paint.setDither(true); //防抖动
        paint.setAntiAlias(true);  //消除锯齿
        paint.setColor(Color.WHITE);  //设置进度的颜色
        paint.setStyle(Paint.Style.STROKE);  //设置样式

        backVariablePaint = new Paint(paint);
        backVariablePaint.setStrokeWidth(BACK_VARIABLE_STROKE_WIDTH); //设置宽度
        backVariablePaint.setColor(Color.parseColor("#321AFFFF"));
        variablePaint = new Paint(paint);
        variablePaint.setStrokeWidth(VARIABLE_STROKE_WIDTH); //设置宽度
        variableRadius = VARIABLE_RADIUS; //实际半径 ＝ 标注半径 + 环宽度/2

        basicOutsidePaint = new Paint(paint);
        basicOutsidePaint.setStrokeWidth(BASIC_OUTSIDE_STROKE_WIDTH); //设置宽度
        basicOutsidePaint.setColor(Color.parseColor("#1AFFFF"));
        basicOutsideRadius = BASIC_OUTSIDE_RADIUS; //实际半径 ＝ 标注半径 + 环宽度/2

        shadowOutsidePaint = new Paint(paint);
        shadowOutsidePaint.setStrokeWidth(SHADOW_OUTSIDE_STROKE_WIDTH); //设置宽度
        shadowOutsidePaint.setColor(Color.parseColor("#004343"));

        basicInsidePaint = new Paint(paint);
        basicInsidePaint.setStrokeWidth(BASIC_INSIDE_STROKE_WIDTH); //设置宽度
        basicInsidePaint.setColor(Color.parseColor("#501AFFFF"));
        basicInsideRadius = BASIC_INSIDE_RADIUS;

        valueTextPaint = new TextPaint(paint);
        valueTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setColor(Color.WHITE);
        valueTextPaint.setTextSize(VALUE_TEXT_SIZE);
        valueTextPaint.setStyle(Paint.Style.FILL);

        topTextPaint = new TextPaint(valueTextPaint);
        topTextPaint.setTextSize(TOP_TEXT_SIZE);
        topTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));

        bottomTextPaint = new TextPaint(valueTextPaint);
        bottomTextPaint.setTextSize(BOTTOM_TEXT_SIZE);
        bottomTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

    }

    private void initDimens() {
        VALUE_TEXT_SIZE = getResources().getDimension(R.dimen.value_text_size);
        BACK_VARIABLE_STROKE_WIDTH = getResources().getDimension(R.dimen.back_variable_stroke_width);
        VARIABLE_STROKE_WIDTH = getResources().getDimension(R.dimen.variable_stroke_width);
        VARIABLE_RADIUS = getResources().getDimension(R.dimen.variable_radius);
        BASIC_OUTSIDE_STROKE_WIDTH = getResources().getDimension(R.dimen.basic_outside_stroke_width);
        BASIC_OUTSIDE_RADIUS = getResources().getDimension(R.dimen.basic_outside_radius);
        SHADOW_OUTSIDE_STROKE_WIDTH = getResources().getDimension(R.dimen.shadow_outside_stroke_width);
        BASIC_INSIDE_STROKE_WIDTH = getResources().getDimension(R.dimen.basic_inside_stroke_width);
        BASIC_INSIDE_RADIUS = getResources().getDimension(R.dimen.basic_inside_radius);
        TEXT_TOP_MARGIN = getResources().getDimension(R.dimen.text_top_margin);
        TEXT_MIDDLE_MARGIN = getResources().getDimension(R.dimen.text_middle_margin);
        TOP_TEXT_SIZE = getResources().getDimension(R.dimen.top_text_size);
        BOTTOM_TEXT_SIZE = getResources().getDimension(R.dimen.bottom_text_size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawVariableLine(canvas);
        drawBasicLine(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        String val = isPercentValue ? String.valueOf((int) (getCurrentScale() * 100)) : String.valueOf(value); //百分比或实际值显示
        float valueHeight = getTextHeight(val, valueTextPaint);
        canvas.drawText(val, centerPoint.x, centerPoint.y + valueHeight / 2, valueTextPaint);
        String topText = tipTextCN;
        float topTextHeight = getTextHeight(topText, topTextPaint);
        canvas.drawText(topText, centerPoint.x, centerPoint.y + TEXT_TOP_MARGIN + topTextHeight, topTextPaint);
        String bottomText = tipTextEN;
        float bottomTextHeight = getTextHeight(bottomText, bottomTextPaint);
        canvas.drawText(bottomText, centerPoint.x, centerPoint.y + TEXT_TOP_MARGIN + topTextHeight + TEXT_MIDDLE_MARGIN + bottomTextHeight, bottomTextPaint);
    }

    private void drawBasicLine(Canvas canvas) {
        canvas.drawArc(basicOutsideOval, 150, 240, false, shadowOutsidePaint);
        canvas.drawArc(basicOutsideOval, 150, 240, false, basicOutsidePaint);
        canvas.drawArc(basicInsideOval, 150, 240, false, basicInsidePaint);
    }

    private void drawVariableLine(Canvas canvas) {
        canvas.drawArc(variableOval, 150, 240, false, backVariablePaint);

        int[] colors = {Color.parseColor("#EC1AFFFF"), Color.parseColor("#FF1AFFFF"),
                Color.parseColor("#641AFFFF"), Color.parseColor("#EC1AFFFF")};
        float[] positions = {0, 30f / 360f, 150f / 360f, 1};
        Shader nShader = new SweepGradient(centerPoint.x, centerPoint.y, colors, positions);
        variablePaint.setShader(nShader);
        canvas.drawArc(variableOval, 150, getCurrentScale() * 240, false, variablePaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (centerPoint == null) {
            int centreX = widthSize / 2;
            int centreY = heightSize / 2;
            centerPoint = new Point(centreX, centreY);
        }
        if (variableOval == null) {
            variableOval = new RectF(centerPoint.x - variableRadius, centerPoint.y - variableRadius,
                    centerPoint.x + variableRadius, centerPoint.y + variableRadius);  //用于定义的圆弧的形状和大小的界限
        }
        if (basicOutsideOval == null) {
            basicOutsideOval = new RectF(centerPoint.x - basicOutsideRadius, centerPoint.y - basicOutsideRadius,
                    centerPoint.x + basicOutsideRadius, centerPoint.y + basicOutsideRadius);  //用于定义的圆弧的形状和大小的界限
        }
        if (basicInsideOval == null) {
            basicInsideOval = new RectF(centerPoint.x - basicInsideRadius, centerPoint.y - basicInsideRadius,
                    centerPoint.x + basicInsideRadius, centerPoint.y + basicInsideRadius);  //用于定义的圆弧的形状和大小的界限
        }
    }

    //获取当前数值的百分比值
    public float getCurrentScale() {
        int total = Math.abs(maxValue - minValue);
        int numerator = Math.abs(value - minValue);
        return (float) numerator / total;
    }

    //获取文字的高度
    public float getTextHeight(String text, Paint paint) {
        Rect textBound = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBound);
        return textBound.bottom - textBound.top;
    }

    public void valueAdd(int tmpValue){
        valueAdd((float)tmpValue);
    }

    public void valueSubtract(int tmpValue){
        valueSubtract((float)tmpValue);
    }

    /**
     * 调节数值增加
     * 回调监听器传出变化前后的数值
     */
    public void valueAdd() {
        float tmpValue = (float) Math.abs(maxValue-minValue) / totalTimes;
        valueAdd(tmpValue);
    }

    /**
     * 调节数值减少
     * 回调监听器传出变化前后的数值
     */
    public void valueSubtract() {
        float tmpValue = (float) Math.abs(maxValue-minValue) / totalTimes;
        valueSubtract(tmpValue);
    }

    private void valueAdd(float tmpValue){
        if (tmpValue < 1) {
            tmpValue = 1;
        }
        int oldValue = getValue();
        int newValue = (int) (oldValue + tmpValue);
        setValue(newValue);
        newValue = value;
        this.postInvalidate();
        if (this.adjustValueListener != null) {
            adjustValueListener.valueChange(oldValue, newValue);
        }
    }

    private void valueSubtract(float tmpValue){
        if (tmpValue < 1) {
            tmpValue = 1;
        }
        int oldValue = getValue();
        int newValue = (int) (oldValue - tmpValue);
        setValue(newValue);
        newValue = value;
        this.postInvalidate();
        if (this.adjustValueListener != null) {
            adjustValueListener.valueChange(oldValue, newValue);
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value >= maxValue) {
            value = maxValue;
        }
        if (value <= minValue) {
            value = minValue;
        }
        this.value = value;
    }

    public int getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(int totalTimes) {
        if (totalTimes < 1) totalTimes = 1;
        this.totalTimes = totalTimes;
    }

    public boolean isPercentValue() {
        return isPercentValue;
    }

    public void setPercentValue(boolean percentValue) {
        isPercentValue = percentValue;
    }

    public void setAdjustValueListener(AdjustValueListener adjustValueListener) {
        this.adjustValueListener = adjustValueListener;
    }

    /**
     * adjustView数值变动回调接口，旋转后新的数值可以通过实现AdjustValueListener接口获取
     */
    public interface AdjustValueListener {
        void valueChange(int oldValue, int newValue);
    }
}
