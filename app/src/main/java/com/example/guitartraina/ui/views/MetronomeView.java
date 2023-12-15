package com.example.guitartraina.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class MetronomeView extends View {
    private int circleRadius;
    private static final int TEXT_SIZE = 50;
    private int mNotesNumber;
    private float[] circleCenterx;
    private float[] circleCentery;
    private Paint paintCircles;
    private Paint paintText;
    private Integer noteIndex;

    public MetronomeView(Context context) {
        super(context);
        init();
    }

    public MetronomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MetronomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setNoteIndex(int noteIndex) {
        if (noteIndex > mNotesNumber - 1 || noteIndex < 0) {
            this.noteIndex = null;
            invalidate();
        } else {
            this.noteIndex = noteIndex;
            invalidate();
        }
    }

    public void setNotesNumber(int notesNumber) {
        this.mNotesNumber = notesNumber;
        if (notesNumber > 6) {
            circleRadius = 40;
        } else {
            circleRadius = 60;
        }
        circleCenterx = new float[notesNumber];
        circleCentery = new float[notesNumber];
        invalidate();
    }

    private void init() {
        paintCircles = new Paint();
        paintText = new Paint();
        // Get the current theme of the activity
        Resources.Theme currentTheme = getContext().getTheme();
        // Create a new TypedValue object to hold the color value
        TypedValue typedValue = new TypedValue();

        // Retrieve the color value of the text color attribute from the current theme
        currentTheme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        // Get the color value as an integer
        int textColor = typedValue.data;
        paintText.setColor(textColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(TEXT_SIZE);
        noteIndex = null;
        mNotesNumber = 4;
        circleCenterx = new float[mNotesNumber];
        circleCentery = new float[mNotesNumber];
        circleRadius = 60;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int NOTE_OFFSET = getWidth() / mNotesNumber;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        // draw tuning circles
        if (noteIndex != null) {
            int circleX = (int) (centerX + ((double) noteIndex - (mNotesNumber - 1.) / 2) * NOTE_OFFSET);
            canvas.drawCircle(circleX, centerY, circleRadius, paintCircles);
            paintCircles.setStyle(Paint.Style.FILL);
            paintCircles.setColor(Color.GRAY);
            canvas.drawCircle(circleX, centerY, circleRadius, paintCircles);
            //dont draw cents in ear mode
        }
        for (int i = 0; i < mNotesNumber; i++) {
            circleCenterx[i] = (int) (centerX + ((double) i - (mNotesNumber - 1.) / 2) * NOTE_OFFSET);
            circleCentery[i] = centerY;
            paintCircles.setColor(paintText.getColor());
            paintCircles.setStyle(Paint.Style.STROKE);
            paintCircles.setStrokeWidth(10);
            canvas.drawText("" + (i + 1), circleCenterx[i], circleCentery[i] + TEXT_SIZE / 3.f, paintText);
            canvas.drawCircle(circleCenterx[i], circleCentery[i], circleRadius, paintCircles);
        }


    }
}