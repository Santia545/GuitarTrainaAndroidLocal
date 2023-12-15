package com.example.guitartraina.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class Countdown {
    private TextView textView;
    private View dimOverlay;
    private final ViewGroup rootView;
    private int countdown = 3;
    private final Context context;
    private Timer timer;

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public Countdown(Context context, ViewGroup rootView) {
        this.rootView = rootView;
        this.context = context;

    }

    public void startCountdown(ICallback callback) {

        AtomicInteger auxCountdown = new AtomicInteger(this.countdown);
        dimOverlay = new View(context);
        dimOverlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dimOverlay.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black background
        textView = new TextView(context);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD); // Set the text to bold
        float textSizeSp = 100; // Your desired text size in sp
        float textSizePixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                textSizeSp,
                context.getResources().getDisplayMetrics()
        );
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePixels);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        FrameLayout container = new FrameLayout(context);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(dimOverlay);
        container.addView(textView);
        rootView.addView(container);
        dimOverlay.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            return true;
        });
        timer = new Timer(() -> {
            textView.setText(String.valueOf(auxCountdown.get()));
            auxCountdown.getAndDecrement();
            if (auxCountdown.get() < 0) {
                callback.callback();
                stopCountdown();
            }
        });
        timer.countSeconds();
    }

    public void stopCountdown() {
        if (timer != null) {
            timer.stopCounting();
        }
        if (rootView != null && dimOverlay != null) {
            rootView.setClickable(true);
            ViewGroup container = (ViewGroup) dimOverlay.getParent();
            if (container != null) {
                container.removeView(dimOverlay);
                container.removeView(textView);
            }
        }
    }
}
