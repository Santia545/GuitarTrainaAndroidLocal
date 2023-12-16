package com.example.guitartrainalocal.activities.exercises;

import java.util.ArrayList;
import java.util.List;

public class SequencesGenerator {
    public static final String[][] notes = {{"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#"}, {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"}, {"D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#"}, {"G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"}, {"B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#"}, {"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#"}};

    public static List<Tab> genSweepSequence(int time, String note, int type) {
        int root = findElementIndex(notes[0], note);
        List<Tab> sequence = new ArrayList<>();
        switch (type) {
            // E shape
            case 0:
                sequence.add(new Tab(5, root, time));
                sequence.add(new Tab(4, root + 2, time));
                sequence.add(new Tab(3, root + 2, time));
                sequence.add(new Tab(2, root + 1, time));
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(0, root, time));
                break;
            //  Am shape back and forth
            case 1:
                sequence.add(new Tab(4, root, time));
                sequence.add(new Tab(3, root + 2, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(1, root + 1, time));
                sequence.add(new Tab(0, root, time));
                sequence.add(new Tab(1, root + 1, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(3, root + 2, time));
                break;
            //Dm shape back and forth
            case 2:
                sequence.add(new Tab(3, root, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(1, root + 3, time));
                sequence.add(new Tab(0, root + 1, time));
                sequence.add(new Tab(1, root + 3, time));
                sequence.add(new Tab(2, root + 2, time));
                break;

            // D7 shape full
            case 3:
                sequence.add(new Tab(3, root, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(1, root + 1, time));
                sequence.add(new Tab(0, root + 2, time));
                sequence.add(new Tab(0, root + 2, time));
                sequence.add(new Tab(1, root + 1, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(3, root, time));
                break;
        }
        return sequence;
    }

    public static List<Tab> genArpeggioSequence(int time, String note, int type) {
        int root = findElementIndex(notes[0], note);
        List<Tab> sequence = new ArrayList<>();
        switch (type) {
            // P I M A M I on Em shape
            case 0:
                sequence.add(new Tab(5, root, time));
                sequence.add(new Tab(2, root, time));
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(0, root, time));
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(2, root, time));
                break;
            //  P M I A on Am shape
            case 1:
                sequence.add(new Tab(4, root, time));
                sequence.add(new Tab(1, root + 1, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(0, root, time));
                break;

            // M I A M P on D shape
            case 2:
                for (int i = 0; i < 6; i++) {
                    sequence.add(new Tab(1, root + 3, time));      // E string
                    sequence.add(new Tab(2, root + 2, time));
                    sequence.add(new Tab(0, root + 2, time));
                    sequence.add(new Tab(1, root + 3, time));
                    sequence.add(new Tab(3, root, time));
                }
                break;

            // UNFORGIVEN
            case 3:
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(2, root + 2, time * 2L));
                sequence.add(new Tab(3, root + 2, time));
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(3, root + 2, time));
                sequence.add(new Tab(1, root, time));
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(3, root + 2, time));
                break;
        }
        return sequence;
    }

    public static List<Tab> genSpiderSequence(int time, String note, int type) {
        int root = findElementIndex(notes[0], note);
        List<Tab> sequence = new ArrayList<>();
        switch (type) {
            // 0-1-2-3
            case 0:
                for (int i = 0; i < 6; i++) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 3, time));
                }
                for (int i = 5; i >= 0; i--) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 3, time));
                }
                break;
            // 0-3-2-1
            case 1:
                for (int i = 0; i < 6; i++) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 3, time));
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 1, time));
                }
                for (int i = 5; i >= 0; i--) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 3, time));
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 1, time));
                }
                break;

            // 0-3-1-2
            case 2:
                for (int i = 0; i < 6; i++) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 3, time));
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 2, time));
                }
                for (int i = 5; i >= 0; i--) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 3, time));
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 2, time));
                }
                break;

            // 0-2-1-3
            case 3:
                for (int i = 0; i < 6; i++) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 3, time));
                }
                for (int i = 5; i >= 0; i--) {
                    sequence.add(new Tab(i, root, time));      // E string
                    sequence.add(new Tab(i, root + 2, time));
                    sequence.add(new Tab(i, root + 1, time));
                    sequence.add(new Tab(i, root + 3, time));
                }
                break;
        }
        return sequence;
    }

    public static List<Tab> genScaleSequence(int time, String note, int type) {
        List<Tab> sequence = new ArrayList<>();
        int root = findElementIndex(notes[0], note);

        switch (type) {
            // major scale shape
            case 0:
                sequence.add(new Tab(5, root, time));      // E string
                sequence.add(new Tab(5, root + 2, time));
                sequence.add(new Tab(5, root + 4, time));
                sequence.add(new Tab(4, root, time));      // A string
                sequence.add(new Tab(4, root + 2, time));
                sequence.add(new Tab(4, root + 4, time));
                sequence.add(new Tab(3, root + 1, time));  // D string
                sequence.add(new Tab(3, root + 2, time));
                break;

            // minor scale shape
            case 1:
                sequence.add(new Tab(5, root, time));       // E string
                sequence.add(new Tab(5, root + 2, time));
                sequence.add(new Tab(5, root + 3, time));
                sequence.add(new Tab(4, root, time));       // A string
                sequence.add(new Tab(4, root + 2, time));
                sequence.add(new Tab(4, root + 3, time));
                sequence.add(new Tab(3, root, time));       // D string
                sequence.add(new Tab(3, root + 2, time));
                break;

            // pentatonic major scale shape
            case 2:
                sequence.add(new Tab(5, root, time));           // E string
                sequence.add(new Tab(5, root + 2, time));   // E string
                sequence.add(new Tab(5, root + 4, time));   // E string
                sequence.add(new Tab(4, root + 2, time));   // A string
                sequence.add(new Tab(4, root + 4, time));   // A string
                sequence.add(new Tab(3, root + 2, time));   // D string
                sequence.add(new Tab(3, root + 4, time));   // D string
                sequence.add(new Tab(2, root + 1, time));   // G string
                sequence.add(new Tab(1, root, time));           // B string
                sequence.add(new Tab(1, root + 2, time));   // B string
                sequence.add(new Tab(0, root, time));           // e string (GO BACK)
                sequence.add(new Tab(0, root + 2, time));   // e string
                sequence.add(new Tab(0, root, time));           // e string
                sequence.add(new Tab(1, root, time));           // B string
                sequence.add(new Tab(2, root + 1, time));   // G string
                sequence.add(new Tab(3, root + 4, time));   // D string
                sequence.add(new Tab(3, root + 2, time));   // D string
                sequence.add(new Tab(4, root + 4, time));   // A string
                sequence.add(new Tab(4, root + 2, time));   // A string
                sequence.add(new Tab(5, root + 4, time));   // E string
                sequence.add(new Tab(5, root + 2, time));   // E string
                sequence.add(new Tab(5, root, time));           // E string

                break;

            // pentatonic minor scale shape
            case 3:
                sequence.add(new Tab(5, root, time));           // E string
                sequence.add(new Tab(5, root + 3, time));
                sequence.add(new Tab(4, root, time));           // A string
                sequence.add(new Tab(4, root + 2, time));
                sequence.add(new Tab(3, root, time));           // D string
                sequence.add(new Tab(3, root + 2, time));
                sequence.add(new Tab(2, root, time));           // G string
                sequence.add(new Tab(2, root + 2, time));
                sequence.add(new Tab(1, root, time));           // B string
                sequence.add(new Tab(1, root + 3, time));
                sequence.add(new Tab(0, root, time));           // E string (GO BACK)
                sequence.add(new Tab(0, root + 3, time));
                sequence.add(new Tab(0, root, time));
                sequence.add(new Tab(1, root + 3, time));   // B string
                sequence.add(new Tab(1, root, time));           // B string
                sequence.add(new Tab(2, root + 2, time));   // G string
                sequence.add(new Tab(2, root, time));           // G string
                sequence.add(new Tab(3, root + 2, time));   // D string
                sequence.add(new Tab(3, root, time));           // D string
                sequence.add(new Tab(4, root + 2, time));   // A string
                sequence.add(new Tab(4, root, time));           // A string
                sequence.add(new Tab(5, root + 3, time));   // E string
                sequence.add(new Tab(5, root, time));           // E string
                break;
        }
        return sequence;
    }

    public static int findElementIndex(String[] array, String element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }
}
