package com.example.guitartrainalocal.activities.metronome;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.ui.views.MetronomeView;

public class Metronome {
    private final Activity activity;
    private int index = 0;



    private int bpm = 120;
    private volatile boolean running = false;
    private int notesNumber = 4;
    private int noteType = 4;
    private int noteAccent = 0;
    private int time = 500;
    private final Handler handler;

    MetronomeView metronomeView;

    public Metronome(Activity activity) {
        this.activity = activity;
        handler = new Handler();
    }

    public boolean isRunning() {
        return running;
    }

    public void setNoteType(int noteType) {
        if (this.noteType != noteType) {
            this.noteType = noteType;
            calculateTime();
        }
    }
    public void setNotesNumber(int noteNumber) {
        this.notesNumber = noteNumber;
    }

    public void run() {
        running = true;
        index = 0;
        metronomeView = activity.findViewById(R.id.metronomeView);
        calculateTime();
        playNextNote();
    }

    private void playNextNote() {
        if (!running) {
            return;
        }
        if (!running) {
            return;
        }

        int noteNumber = index % notesNumber;
        metronomeView.setNoteIndex(noteNumber);
        if (noteNumber == noteAccent) {
            playSound("forte");
        } else {
            playSound("piano");
        }

        index++;
        if (index >= notesNumber) {
            index = 0;  // Reset index to start from the beginning
        }

        handler.postDelayed(this::playNextNote, time);
    }

    private void calculateTime() {
        if (noteType != 4) {
            float noteDuration = noteType / 4.0f;
            float noteTime = 60000.0f / bpm;
            this.time = Math.round(noteTime * noteDuration);
        } else {
            this.time = Math.round(60000.0f / bpm);
        }
    }
    private void playSound(String soundType) {
        MediaPlayer mediaPlayer = MediaPlayer.create(
                activity,
                activity.getResources().getIdentifier(activity.getPackageName() + ":raw/metronome_" + soundType, null, null));
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }
    public void pause() {
        running = false;
        index = 0;
        handler.removeCallbacksAndMessages(null);
    }
    public void setBpm(int bpm) throws IllegalArgumentException {
        if (bpm > 500 || bpm < 1) {
            throw new IllegalArgumentException("BPM out of bounds");
        }
        if (this.bpm != bpm) {
            this.bpm = bpm;
            calculateTime();
        }
    }

    public void setNoteAccent(int noteAccent) {
        this.noteAccent = noteAccent;
    }
}
