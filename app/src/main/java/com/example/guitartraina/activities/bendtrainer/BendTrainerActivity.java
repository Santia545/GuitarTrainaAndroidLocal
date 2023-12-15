package com.example.guitartraina.activities.bendtrainer;

import static com.example.guitartraina.util.InfoLayout.createInfoLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.YoutubePlayerActivity;
import com.example.guitartraina.ui.views.FrequencyView;
import com.example.guitartraina.util.DialogInfo;


public class BendTrainerActivity extends AppCompatActivity {
    private BendTrainer bendTrainer;
    private Spinner bendHeight;
    private FrequencyView frequencyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bend_trainer);
        frequencyView = findViewById(R.id.frequencyView);
        bendHeight = findViewById(R.id.bend_height);
        bendHeight.setSelection(3);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            bendTrainer = new BendTrainer(BendTrainerActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(BendTrainerActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            bendTrainer = null;
            ViewGroup parentView = findViewById(android.R.id.content);
            LinearLayout linearLayout = createInfoLayout(BendTrainerActivity.this,R.string.info_mic_permiso);
            parentView.removeAllViews();
            parentView.addView(linearLayout);
            return;
        }
        bendHeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                int cents = 0;
                if (text.length() > 1) {
                    cents=200*Integer.parseInt(""+text.charAt(0));
                }
                switch (i%4) {
                    case 0:
                        cents += 50;
                        break;
                    case 1:
                        cents += 100;
                        break;
                    case 2:
                        cents += 150;
                        break;
                    case 3:
                        cents += 200;
                        break;

                }
                switch (i) {
                    case 7:
                        cents= 400;
                        break;
                    case 11:
                        cents = 600;
                        break;

                }
                bendTrainer.setBendHeight(cents);
                frequencyView.setTargetNoteDiff(cents+0.);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        bendTrainer.run();

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
                    .putExtra("video", "\"https://www.youtube.com/embed/xyb74jO1QkA\"")
                    .putExtra("titulo", R.string.entrenador_de_bends)
                    .putExtra("cuerpo", R.string.bend_trainer));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bendTrainer != null) {
            bendTrainer.stop();
        }
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    bendTrainer = new BendTrainer(BendTrainerActivity.this);
                    BendTrainerActivity.this.recreate();
                } else {
                    DialogInfo.dialogInfoBuilder(this, "", getString(R.string.permission_required_x) + getString(R.string.microfono)).show();
                }
            }
    );
}