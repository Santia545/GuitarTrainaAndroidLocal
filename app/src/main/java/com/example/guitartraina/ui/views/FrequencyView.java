package com.example.guitartraina.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.guitartraina.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FrequencyView extends View {
    private static final int TEXT_SIZE = 40;
    private static final int[] NOTE_COLORS = {Color.RED, Color.YELLOW, Color.GREEN};
    private Double cents;

    private Paint paintText;
    private Paint paintGraph;
    private String note;
    private double centsDiff = 200;
    private List<Double> centsTrail;
    private static final int TRAIL_SIZE = 100; // Number of trail points to keep


    public FrequencyView(Context context) {
        super(context);
        init();
    }

    public FrequencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrequencyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintText = new Paint();
        paintGraph = new Paint();
        paintGraph.setTextAlign(Paint.Align.CENTER);
        paintGraph.setTextSize(TEXT_SIZE * 2);
        centsTrail = new ArrayList<>();
        // Get the current theme of the activity
        Resources.Theme currentTheme = getContext().getTheme();
        // Create a new TypedValue object to hold the color value
        TypedValue typedValue = new TypedValue();
        // Retrieve the color value of the text color attribute from the current theme
        currentTheme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        // Get the color value as an integer
        int textColor = typedValue.data;
        paintText.setColor(textColor);
        paintText.setTextSize(TEXT_SIZE);
        paintText.setTextAlign(Paint.Align.CENTER);

    }
    public void lowerTrailPoints() {
        centsTrail.add(null);
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        if (note != null) {
            canvas.drawText(getContext().getString(R.string.target_note), centerX, centerY-10 - TEXT_SIZE*2, paintText);
            canvas.drawText(note + " + " + centsDiff/100 +" " +getContext().getString(R.string.semitones), centerX, centerY - TEXT_SIZE, paintText);
        }
        // Add the current cents value to the trail
        centsTrail.add(cents);
        // Remove old trail points
        while (centsTrail.size() > TRAIL_SIZE) {
            centsTrail.remove(0);
        }
        // Draw the trail points
        int trailSize = centsTrail.size();
        for (int i = 0; i < trailSize; i++) {
            Double trailCents = centsTrail.get(i);
            if(trailCents == null){
                continue;
            }
            float trailPosition = map(centerX, trailCents);
            int trailColorIndex = getCentsColor(trailCents);
            paintGraph.setColor(NOTE_COLORS[trailColorIndex]);
            // Calculate the Y-coordinate for each trail point
            float trailY = centerY + TEXT_SIZE + ((trailSize - i) * (TEXT_SIZE / 8f));
            // Draw the most recent indicator as an "L"
            if (i == trailSize - 1) {
                float graphPosition = map(centerX, cents);
                int colorIndex = getCentsColor(cents);
                paintGraph.setColor(NOTE_COLORS[colorIndex]);
                canvas.drawText("|", graphPosition, centerY + TEXT_SIZE, paintGraph);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f cents", cents), centerX, centerY + 80 + TEXT_SIZE, paintText);
            } else {
                // Draw other trail points as small circles or points
                canvas.drawCircle(trailPosition, trailY, 5, paintGraph);
            }
        }

    }

    public int map(int center, double mCentsDiff) {
        int borders = (int) (20 * this.getResources().getDisplayMetrics().density);
        if (mCentsDiff > 50) {
            return (center * 2) - borders;
        } else if (mCentsDiff < -50) {
            return borders;
        }
        int input_end = 50;
        int input_start = -50;
        float output_end = (center * 2) - borders;
        int input_range = input_end - input_start;
        float output_range = output_end - borders;
        double output = (mCentsDiff - input_start) * output_range / input_range + borders;
        return (int) output;
    }

    private int getCentsColor(double cents) {
        if (Math.abs(cents) > 10) {
            return 0; // red color
        } else if (Math.abs(cents) > 5) {
            return 1; // yellow color
        } else {
            return 2; // green color
        }
    }

    public void setBaseNote(String note) {
        this.note = note;
        invalidate();
    }

    public void setCents(double cents) {
        this.cents = cents;
        invalidate();
    }

    public void setTargetNoteDiff(Double centsDiff) {
        this.centsDiff = centsDiff;
        invalidate();
    }
}