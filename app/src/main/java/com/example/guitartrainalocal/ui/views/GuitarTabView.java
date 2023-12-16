package com.example.guitartrainalocal.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.guitartrainalocal.activities.exercises.Tab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuitarTabView extends View {
    public static final String[][] guitarNotes = {{"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"}, {"B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#"}, {"G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A"}, {"D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E"}, {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}, {"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"}};
    private static final int NUM_STRINGS = 6;
    private static final float CALIBRATION = 60f;
    private static int STRING_PADDING;
    private static final int NOTE_SPEED = 6; //pixels per frame
    private static int NOTE_WIDTH = 100;
    private Paint paintText;
    private Paint stringPaint;
    private Paint cursorOutlinePaint;
    private Paint cursorFillPaint;
    private Paint notePaint;
    private List<CircleNote> notes;
    private final Cursor[] cursor = new Cursor[6];

    private float toleranceDistance = NOTE_WIDTH / 2f;
    private float width;
    private int rights = 0;
    private int topOffset;

    public int getRights() {
        return rights;
    }

    public int getWrongs() {
        return wrongs;
    }

    private int wrongs = 0;

    public float getTimePerView() {
        return msPerPixel * width;
    }

    private float msPerPixel;

    public GuitarTabView(Context context) {
        super(context);
        init();
    }

    public GuitarTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Resources.Theme currentTheme = getContext().getTheme();
        TypedValue typedValue = new TypedValue();

        currentTheme.resolveAttribute(android.R.attr.colorBackground, typedValue, true);
        int backgroundColor = typedValue.data;

        stringPaint = new Paint();
        stringPaint.setColor(Color.RED);
        stringPaint.setStrokeWidth(5);

        notePaint = new Paint();
        notePaint.setColor(Color.RED);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(80);

        cursorFillPaint = new Paint();
        cursorFillPaint.setColor(backgroundColor);

        cursorOutlinePaint = new Paint();
        cursorOutlinePaint.setStyle(Paint.Style.STROKE);
        cursorOutlinePaint.setColor(Color.RED);
        cursorOutlinePaint.setStrokeWidth(10);

        for (int i = 0; i < cursor.length; i++) {
            cursor[i] = new Cursor(120, NOTE_WIDTH / 2f);
        }
        notes = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        STRING_PADDING = getHeight() / 8;
        NOTE_WIDTH = getHeight() / 9;
        width = getWidth() - cursor[0].centerX;

        int frameRate = 60;
        msPerPixel = (1000.0f / (NOTE_SPEED * frameRate));
        float toleranceMiliSeconds = 150;
        toleranceDistance = (toleranceMiliSeconds / msPerPixel);

        int viewHeight = getHeight();
        int totalStringsHeight = ((NUM_STRINGS - 1) * STRING_PADDING);
        topOffset = (viewHeight - totalStringsHeight) / 2;


        // Draw the strings
        for (int i = 0; i < NUM_STRINGS; i++) {
            int y = topOffset + STRING_PADDING * i;
            canvas.drawLine(0, y, getWidth(), y, stringPaint);
            cursor[i].centerY = y;
            cursor[i].radius = NOTE_WIDTH / 2f;
            canvas.drawCircle(cursor[i].centerX, cursor[i].centerY, cursor[i].radius, cursorOutlinePaint);
            canvas.drawCircle(cursor[i].centerX, cursor[i].centerY, cursor[i].radius, cursorFillPaint);
        }

        // Draw the notes
        for (CircleNote note : notes) {
            notePaint.setColor(note.color);
            canvas.drawCircle(note.centerX, note.centerY, note.radius, notePaint);
            int textY = (int) (note.centerY - (paintText.descent() + paintText.ascent()) / 2);
            canvas.drawText("" + note.fret, note.centerX, textY, paintText);

        }
    }

    public void onListenEvent(String playedNote) {
        for (int i = 0; i < notes.size(); i++) {
            CircleNote note = notes.get(i);
            float distance = cursor[0].centerX-CALIBRATION - note.centerX;
            if (Math.abs(distance) <= toleranceDistance) {
                int string = 0;
                for (int j = 0; j < cursor.length; j++) {
                    if (cursor[j].centerY == note.centerY) {
                        string = j;
                        break;
                    }
                }
                if (playedNote.equals(guitarNotes[string][note.fret])) {
                    note.color = Color.GREEN;
                    rights++;
                    invalidate();
                }

            }
        }
    }

    public void addNote(Tab tab) {
        float centerX = getWidth();
        float centerY = topOffset + STRING_PADDING * tab.getString();
        float radius = NOTE_WIDTH / 2f;
        int color = Color.YELLOW;
        CircleNote note = new CircleNote(tab.getFret(), centerX, centerY, radius, color);
        notes.add(note);
        invalidate();
    }


    public void moveNotes() {
        // Move the notes horizontally from right to left
        Iterator<CircleNote> iterator = notes.iterator();
        while (iterator.hasNext()) {
            CircleNote note = iterator.next();
            note.centerX -= NOTE_SPEED;
            // Remove the note if it goes off the screen
            if (note.centerX + note.radius < 0) {
                iterator.remove();
            } else if (note.centerX < cursor[0].centerX-CALIBRATION - toleranceDistance && note.color == Color.YELLOW) {
                note.color = Color.RED;
                wrongs++;
            }
        }
        invalidate();
    }

    public void clean() {
        rights = 0;
        wrongs = 0;
        notes.clear();
        invalidate();
    }

    public static class CircleNote {
        private final int fret;
        private float centerX;
        private final float centerY;
        private final float radius;
        private int color;

        public CircleNote(int fret, float centerX, float centerY, float radius, int color) {
            this.fret = fret;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.color = color;
        }
    }

    private static class Cursor {
        private final float centerX;
        private float radius;
        private float centerY;

        public Cursor(float centerX, float radius) {
            this.centerX = centerX;
            this.radius = radius;
        }
    }
}
