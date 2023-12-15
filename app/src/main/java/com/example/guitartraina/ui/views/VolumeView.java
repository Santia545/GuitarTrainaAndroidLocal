package com.example.guitartraina.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class VolumeView extends View {


    private Paint stringPaint;

    private double volume=-100;

    public VolumeView(Context context) {
        super(context);
        init();
    }

    public VolumeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        stringPaint = new Paint();
        stringPaint.setColor(Color.RED);
        stringPaint.setStrokeWidth(20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2f;
        // Draw the string
        canvas.drawLine(0, centerY, map(volume), centerY, stringPaint);
    }

    public void setVolume(double volume) {
        this.volume = volume;
        invalidate();
    }

    public int map(double volume) {
        int input_end = 0;
        int input_start = -100;
        float output_end = getWidth();
        int input_range = input_end - input_start;
        float output_range = output_end - 0;
        double output = (volume - input_end) * output_range / input_range + output_end;
        return (int) output;
    }



}
