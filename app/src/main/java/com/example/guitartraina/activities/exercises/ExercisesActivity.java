package com.example.guitartraina.activities.exercises;

import static com.example.guitartraina.util.InfoLayout.createInfoLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.guitartraina.R;
import com.example.guitartraina.util.EncryptedSharedPreferences;

public class ExercisesActivity extends AppCompatActivity {
    private SharedPreferences archivo;
    private String exerciseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);
        archivo = EncryptedSharedPreferences.getEncryptedSharedPreferences(this);
        Spinner spinModule = findViewById(R.id.exercise_select_spin);
        Button btnNextMonth = findViewById(R.id.button);
        btnNextMonth.setOnClickListener(view -> {
            Intent intent = new Intent(this, ExerciseActivity.class);
            intent.putExtra("exercise",exerciseType);
            startActivity(intent);
        });
        spinModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                switch (itemPosition) {
                    case 0:
                        exerciseType = "spider";
                        break;
                    case 1:
                        exerciseType = "sweep";

                        break;
                    case 2:
                        exerciseType = "arpeggios";

                        break;
                    case 3:
                        exerciseType = "bends";

                        break;
                    case 4:
                        exerciseType = "scales";
                        break;
                    default:
                        exerciseType = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinModule.setSelection(0);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(ExercisesActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ViewGroup parentView = findViewById(android.R.id.content);
            LinearLayout linearLayout = createInfoLayout(ExercisesActivity.this, R.string.info_mic_permiso);
            parentView.removeAllViews();
            parentView.addView(linearLayout);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Toast.makeText(ExercisesActivity.this, "Concedido", Toast.LENGTH_SHORT).show();
                    ExercisesActivity.this.recreate();
                } else {
                    Toast.makeText(ExercisesActivity.this, getString(R.string.info_mic_permiso), Toast.LENGTH_SHORT).show();
                }
            }
    );
}