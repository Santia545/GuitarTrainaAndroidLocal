package com.example.guitartraina.activities.exercises;

public class Note {
    public String getName() {
        return name;
    }

    public double getCentsOff() {
        return centsOff;
    }

    public Note(String name, double centsOff) {
        this.name = name;
        this.centsOff = centsOff;
    }

    private final String name;
    private final double centsOff;

}