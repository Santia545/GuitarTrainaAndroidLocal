package com.example.guitartrainalocal.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.guitartrainalocal.R;

import java.util.Locale;


public class GuitarTunerView extends View {
    private static final int[] NOTE_COLORS = {Color.RED, Color.YELLOW, Color.GREEN};
    private static final int CIRCLE_RADIUS = 60;
    private static final int TEXT_SIZE = 50;
    private static final int STRINGS_NUMBER = 6;
    private static String[] TUNING_MODE;
    private OnClickListener stringListener;

    private int tuningMode; //0 auto, 1 manual, 2 ear tuning
    private String[] mNoteNames;
    private double[] mCentsDiffs;
    private double[] mHz=null;
    private final float[] circleCenterx = new float[STRINGS_NUMBER];
    private final float[] circleCentery = new float[STRINGS_NUMBER];
    private Paint paintCircles;
    private Paint paintText;
    private Paint paintGraph;

    private Integer stringIndex;

    public GuitarTunerView(Context context) {
        super(context);
        init();
    }

    public GuitarTunerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GuitarTunerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs,defStyleAttr);
        init();
    }

    private void init() {
        TUNING_MODE= new String[]{getContext().getString(R.string.tuner_mode_auto), getContext().getString(R.string.tuner_mode_manual), getContext().getString(R.string.tuner_mode_ear)};
        tuningMode = 0;
        paintCircles = new Paint();
        paintText = new Paint();
        paintGraph = new Paint();
        paintGraph.setTextAlign(Paint.Align.CENTER);
        paintGraph.setTextSize(TEXT_SIZE*2);
        Resources.Theme currentTheme = getContext().getTheme();
        TypedValue typedValue = new TypedValue();
        currentTheme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        int textColor = typedValue.data;
        paintText.setColor(textColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(TEXT_SIZE);
        mNoteNames = new String[]{"E", "A", "D", "G", "B", "E"};
        mCentsDiffs = new double[]{11, 11, 11, 11, 11, 11};
        stringIndex = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int NOTE_OFFSET = getWidth() / STRINGS_NUMBER;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        //draw tuning mode
        canvas.drawText(TUNING_MODE[tuningMode], centerX, 120 - TEXT_SIZE, paintText);
        // draw tuning circles
        for (int i = 0; i < STRINGS_NUMBER; i++) {
            circleCenterx[i] = (int) (centerX + ((double) i - (STRINGS_NUMBER-1.)/2) * NOTE_OFFSET);
            circleCentery[i] = centerY;
            int colorIndex = getCentsColor(mCentsDiffs[i]);
            paintCircles.setStyle(Paint.Style.STROKE);
            paintCircles.setColor(NOTE_COLORS[colorIndex]);
            paintCircles.setStrokeWidth(10);
            canvas.drawCircle(circleCenterx[i], circleCentery[i], CIRCLE_RADIUS, paintCircles);
        }

        // draw note names for each string
        for (int i = 0; i < STRINGS_NUMBER; i++) {
            int textX = (int) (centerX + ((double) i - (STRINGS_NUMBER-1.)/2) * NOTE_OFFSET);
            int textY = (int) (centerY - (paintText.descent() + paintText.ascent()) / 2);
            canvas.drawText(mNoteNames[i], textX, textY, paintText);
        }
        if (stringIndex != null) {
            double cents = mCentsDiffs[stringIndex];
            int colorIndex = getCentsColor(cents);
            int circleX = (int) (centerX + ((double) stringIndex - 2.5) * NOTE_OFFSET);
            paintCircles.setColor(NOTE_COLORS[colorIndex]);
            canvas.drawCircle(circleX, centerY, CIRCLE_RADIUS, paintCircles);
            paintCircles.setColor(paintText.getColor());
            canvas.drawCircle(circleX, centerY, CIRCLE_RADIUS + 15, paintCircles);
            canvas.drawText(String.format(Locale.getDefault(), "%.2fhz", mHz[stringIndex]), circleX, centerY - 150 + TEXT_SIZE, paintText);
            //dont draw cents in ear mode
            if (this.tuningMode == 2) {
                return;
            }
            paintGraph.setColor(NOTE_COLORS[colorIndex]);
            float graphPosition = map(centerX, mCentsDiffs[stringIndex]);
            canvas.drawText("|", graphPosition, 250 + TEXT_SIZE, paintGraph);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", mCentsDiffs[stringIndex]), circleX, centerY + 80 + TEXT_SIZE, paintText);
            canvas.drawText("cents", circleX, centerY + 120 + TEXT_SIZE, paintText);
            if (mCentsDiffs[stringIndex] < -5) {
                canvas.drawText(getContext().getString(R.string.tuner_tight), centerX, 120 + TEXT_SIZE, paintText);
            } else if (mCentsDiffs[stringIndex] > 5) {
                canvas.drawText(getContext().getString(R.string.tuner_loosen), centerX, 120 + TEXT_SIZE, paintText);
            } else {
                canvas.drawText(getContext().getString(R.string.tuner_ok), centerX, 120 + TEXT_SIZE, paintText);
            }
        }
    }
    public int map(int center, double mCentsDiff) {
        int borders= (int) (20*this.getResources().getDisplayMetrics().density);
        if (mCentsDiff > 50) {
            return (center*2)-borders;
        } else if (mCentsDiff<-50) {
            return borders;
        }
        int input_end= 50;
        int input_start =-50;
        float output_end = (center*2)-borders;
        int input_range = input_end - input_start;
        float output_range = output_end - borders;
        double output = (mCentsDiff - input_start)*output_range / input_range + borders;
        return (int)output;
    }
    public void setTuningString(int stringIndex, double cents) {
        this.stringIndex = stringIndex;
        mCentsDiffs[stringIndex] = cents;
        invalidate();
    }

    public int getTuningMode() {
        return tuningMode;
    }

    public void setTuningMode(int tuningMode) {
        this.tuningMode = tuningMode;
        invalidate();
    }
    public void setNoteNames(String [] noteNames){
        this.mNoteNames=noteNames;
        invalidate();
    }
    public void setHz(double[] Hz){
        this.mHz=Hz;
        invalidate();
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

    public int getSelectedString() {
        return this.stringIndex;
    }

    public void setStringOnClickListener(OnClickListener listener) {
        this.stringListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.performClick();
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        for (int i = 0; i < STRINGS_NUMBER; i++) {
            float centerX = circleCenterx[i];
            float centerY = circleCentery[i];
            float distance = (float) Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
            if (distance <= CIRCLE_RADIUS) {
                stringIndex = i;
                if (tuningMode == 0) {
                    setTuningMode(1);
                }else{
                    if (stringListener != null && tuningMode!=1) stringListener.onClick(this);
                }
                invalidate();
                return true;
            }
        }
        return false;
    }
}