package com.example.guitartrainalocal.activities;

import static com.example.guitartrainalocal.util.Config.getDarkModeFromPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.guitartrainalocal.R;


public class SplashScreen extends AppCompatActivity {
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if(getDarkModeFromPreferences(SplashScreen.this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        runnable = () -> {
            Intent mainActivity = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(mainActivity);
            finishAfterTransition();
        };
        handler.postDelayed(runnable, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

}
