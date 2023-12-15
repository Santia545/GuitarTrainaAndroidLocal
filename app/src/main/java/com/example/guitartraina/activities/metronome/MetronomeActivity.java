package com.example.guitartraina.activities.metronome;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.YoutubePlayerActivity;

public class MetronomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tutorial_btn) {
            startActivity(new Intent(this, YoutubePlayerActivity.class)
                    .putExtra("video", "\"https://www.youtube.com/embed/R3WWNvSKJJg\"")
                    .putExtra("titulo", R.string.metronomo)
                    .putExtra("cuerpo", R.string.metronoam));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}