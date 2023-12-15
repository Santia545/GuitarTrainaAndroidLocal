package com.example.guitartraina.util;

import android.os.Handler;

public class Timer {
    private final Handler handler;
    private boolean isCounting;
    private final Runnable runnable;
    private ICallback callback;

    public Timer(ICallback callback) {
        this.callback=callback;
        handler = new Handler();
        isCounting = false;
        runnable = new Runnable() {
            @Override
            public void run() {
                Timer.this.callback.callback();
                if (isCounting) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    public void countSeconds() {
        if (!isCounting) {
            isCounting = true;
            handler.postDelayed(runnable,1000);
        }
    }

    public void stopCounting() {
        if (isCounting) {
            handler.removeCallbacks(runnable);
            isCounting = false;
        }
    }


    public void setCallback(ICallback callback) {
        this.callback = callback;
    }
}
