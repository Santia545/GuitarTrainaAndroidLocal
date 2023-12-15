package com.example.guitartraina.activities.exercises;

public class Tab {
    private final int string;
    private final int fret;
    private long time;

    public Tab(int string, int fret, long time) {
        this.string = string;
        this.fret = fret;
        this.time=time;
    }

    public int getFret() {
        return fret;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public long getTime() {
        return time;
    }

    public int getString() {
        return string;
    }
}
