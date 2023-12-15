package com.example.guitartraina.activities.exercises;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.guitartraina.util.EncryptedSharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExercisesManager {
    private final SharedPreferences archivo;

    public ExercisesManager(Context context) {
        archivo = EncryptedSharedPreferences.getEncryptedSharedPreferences(context);
    }

    public int getLevel(String key) {
        return archivo.getInt(key, 0);
    }

    public void increaseLevel(String key) {
        int top;
        top = 3;
        SharedPreferences.Editor editor = archivo.edit();
        int level = getLevel(key) + 1;
        if (level == top) {
            level = 0;
        }
        editor.putInt(key, level);
        editor.apply();
    }

    public List<Tab> getSweepSequence(int bpm) {
        Random random = new Random();
        // Generate a random integer between 0 and 11 (inclusive)
        int randomNumber = random.nextInt(12);
        String note = SequencesGenerator.notes[0][randomNumber];
        int type = getLevel("sweep");
        int time = (60 * 1000) / bpm;
        List<Tab> fullLenghtSequence = new ArrayList<>();
        while (fullLenghtSequence.size() * time < 60000) {
            fullLenghtSequence.addAll(SequencesGenerator.genSweepSequence(time, note, type));

        }
        return fullLenghtSequence;
    }
    public List<Tab> getArpeggioSequence(int bpm) {
        Random random = new Random();
        // Generate a random integer between 0 and 11 (inclusive)
        int randomNumber = random.nextInt(12);
        String note = SequencesGenerator.notes[0][randomNumber];
        int type = getLevel("arpeggios");
        int time = (60 * 1000) / bpm;
        List<Tab> fullLenghtSequence = new ArrayList<>();
        while (fullLenghtSequence.size() * time < 60000) {
            fullLenghtSequence.addAll(SequencesGenerator.genArpeggioSequence(time, note, type));

        }
        return fullLenghtSequence;
    }
    public List<Tab> getSpiderSequence(int bpm) {
        Random random = new Random();
        // Generate a random integer between 0 and 11 (inclusive)
        int randomNumber = random.nextInt(12);
        String note = SequencesGenerator.notes[0][randomNumber];
        int type = getLevel("spider");
        int time = (60 * 1000) / bpm;
        List<Tab> fullLenghtSequence = new ArrayList<>();
        while (fullLenghtSequence.size() * time < 60000) {
            fullLenghtSequence.addAll(SequencesGenerator.genSpiderSequence(time, note, type));

        }
        return fullLenghtSequence;
    }

    public List<Tab> getScaleSequence(int bpm) {
        Random random = new Random();
        // Generate a random integer between 0 and 11 (inclusive)
        int randomNumber = random.nextInt(12);
        String note = SequencesGenerator.notes[0][randomNumber];
        int type = getLevel("scales");
        int time = (60 * 1000) / bpm;
        List<Tab> fullLenghtSequence = new ArrayList<>();
        while (fullLenghtSequence.size() * time < 60000) {
            fullLenghtSequence.addAll(SequencesGenerator.genScaleSequence(time, note, type));

        }
        return fullLenghtSequence;
    }
}
