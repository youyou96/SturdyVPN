package com.four.yy.vpn.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomProgressBar extends View {

    private int progress = 0;
    private int max = 100;

    public CustomProgressBar(Context context) {
        super(context);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取View的宽度和高度
        int width = getWidth();
        int height = getHeight();

        // 计算进度条的宽度
        int progressWidth = (int) (((float) progress / (float) max) * width);

        // 绘制进度条的背景矩形
        @SuppressLint("DrawAllocation") Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#25FFFFFF"));
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // 绘制进度条的前景矩形
        @SuppressLint("DrawAllocation") Paint foregroundPaint = new Paint();
        foregroundPaint.setColor(Color.parseColor("#FF2B4CBA"));
        canvas.drawRect(0, 0, progressWidth, height, foregroundPaint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

}