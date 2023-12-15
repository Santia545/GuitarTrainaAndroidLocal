package com.example.guitartraina.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.guitartraina.R;


public class SplashScreen extends AppCompatActivity {
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        runnable = () -> {
            Intent mainActivity = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        };
        handler.postDelayed(runnable, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

}
