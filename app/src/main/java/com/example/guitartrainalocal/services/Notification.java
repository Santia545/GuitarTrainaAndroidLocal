package com.example.guitartrainalocal.services;

import java.util.Date;

public class Notification {
    private final Date date;
    private final int type;

    public Notification(String title, String body, Date date, int type) {
        this.date = date;
        this.type = type;
    }
    public Date getDate() {
        return date;
    }
    public int getType() {
        return type;
    }
}
