package com.hx.recyclerviewdemo.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Author dzl on 2016/7/7.
 */
public class CircleView extends View {

    private int color;
    private Paint paint;
    private Context context;
    private float stroke = 2;

    private int progress = 90;//进度

    public CircleView(Context context) {
        this(context, null);
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true); //消除锯齿
        paint.setStyle(Paint.Style.STROKE); //绘制空心圆
        paint.setColor(0xffaaaaaa);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        this.context = context;
    }

    public int getColor() {
        return color;
    }

    public float getStroke() {
        return stroke;
    }

    public void setStroke(float stroke) {
        this.stroke = stroke;
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
        postInvalidate();
    }

    public synchronized int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {

        if (this.progress == progress){
            return;
        }

        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }

        this.progress = progress;
        postInvalidate();

    }

    public Paint getPaint() {
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int center = getWidth() / 2; //获取圆心的x坐标
        int radius = (int) (center - stroke / 2); //圆环的半径

        paint.setStrokeWidth(stroke);
        RectF oval = new RectF(center - radius, center - radius, center + radius, center + radius);
//        float start = 270f;
        canvas.drawArc(oval, 270f, progress * -1, false, paint);

        super.onDraw(canvas);
    }

}
