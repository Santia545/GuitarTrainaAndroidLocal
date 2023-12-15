package com.example.guitartraina.activities.tuner;

import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.guitartraina.R;
import com.example.guitartraina.api.IResult;
import com.example.guitartraina.api.VolleyService;
import com.example.guitartraina.util.DialogInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TuningCreationActivity extends AppCompatActivity {
    private final double[] frequencies = new double[7];
    private final String[] noteNames = new String[7];
    private String[] allNotes = new String[]{};
    private EditText referenceNote;
    private Spinner[] strings;
    private VolleyService volleyService;
    private SharedPreferences archivo;

    private IResult resultCallback = null;
    private int[] standarTuningPosition;
    private final double[] STANDAR_TUNING_FREQ = new double[]{82.41, 110.00, 146.83, 196.00, 246.94, 329.63, 440};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuning_creation);
        archivo=getEncryptedSharedPreferences(this);
        initVolleyCallback();
        volleyService = new VolleyService(resultCallback, this);
        strings = new Spinner[6];
        standarTuningPosition = new int[]{28, 33, 38, 43, 47, 52};
        strings[5] = findViewById(R.id.spinner);
        strings[4] = findViewById(R.id.spinner2);
        strings[3] = findViewById(R.id.spinner3);
        strings[2] = findViewById(R.id.spinner4);
        strings[1] = findViewById(R.id.spinner5);
        strings[0] = findViewById(R.id.spinner6);
        referenceNote = findViewById(R.id.editText);
        Button btnResetTuning = findViewById(R.id.tuning_reset);
        Button btnSaveTuning = findViewById(R.id.tuning_save);
        initItems();

        referenceNote.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                String text = textView.getText().toString();
                if (text.equals("")) {
                    DialogInfo.dialogInfoBuilder(this, "", getString(R.string.empty_note_error)).show();
                    referenceNote.setText(R.string.la4_freq);
                    return true; // Consume the event
                }
                double frequence = Double.parseDouble(text);
                if (frequence > 500. || frequence < 400.) {
                    DialogInfo.dialogInfoBuilder(this, "", getString(R.string.invalid_note_error)).show();
                    referenceNote.setText(R.string.la4_freq);
                    return true; // Consume the event
                }
                return true; // Consume the event
            }
            return false;
        });
        referenceNote.setText(R.string.la4_freq);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allNotes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < 6; i++) {
            strings[i].setAdapter(adapter);
        }
        resetTuning();
        noteNames[6] = "A";
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            strings[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                    noteNames[finalI] = adapterView.getItemAtPosition(itemPosition).toString().replaceAll("\\d", "");
                    if (itemPosition  > standarTuningPosition[finalI]+6) {
                        DialogInfo.dialogInfoBuilder(TuningCreationActivity.this, "", getString(R.string.la_afinacion_de_la_cuerda_no_puede_ser_mas_de_3_tonos_por_arriba_de_la_afinacion_estandar)).show();
                        strings[finalI].setSelection(standarTuningPosition[finalI]);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        btnResetTuning.setOnClickListener(view -> resetTuning());
        btnSaveTuning.setOnClickListener(view -> {
            referenceNote.onEditorAction(EditorInfo.IME_ACTION_DONE);
            recalculateTuning();
            boolean isSharperThan10Cents = false;
            for (int i = 0; i < strings.length; i++) {
                if (getCentsOff(frequencies[i], STANDAR_TUNING_FREQ[i]) > 10) {
                    isSharperThan10Cents = true;
                    break;
                }
            }
            if (isSharperThan10Cents) {
                AlertDialog sharpTuningDialog = dialogBuilder2();
                sharpTuningDialog.show();
            } else {
                AlertDialog dialog = dialogBuilder();
                dialog.show();
            }
        });
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
                    .putExtra("video", "\"https://www.youtube.com/embed/CrGxNihSeZc\"")
                    .putExtra("titulo", R.string.crear_afinacion_personalizada)
                    .putExtra("cuerpo", R.string.create_tuning));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private double getCentsOff(double pitchInHz, double expectedFrequency) {
        //Math.log(2.0) = 0.6931471805599453;
        //12*100
        return 1200 * Math.log(pitchInHz / expectedFrequency) / 0.6931471805599453;
    }

    private AlertDialog dialogBuilder2() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.high_tuning_alert))
                .setMessage(getString(R.string.high_tuning_alert_message))
                .setPositiveButton(getString(R.string.continuar), (dialogInterface, i) -> {
                    AlertDialog saveDialog = dialogBuilder();
                    saveDialog.show();
                })
                .setNegativeButton(R.string.cancel, (dialog1, which) -> dialog1.cancel())
                .setOnCancelListener(DialogInterface::cancel)
                .create();
    }

    private AlertDialog dialogBuilder() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Titulo de la afinación");
        AlertDialog dialog = new AlertDialog.Builder(TuningCreationActivity.this)
                .setTitle(getString(R.string.ingresa_el_titulo_de_la_afinaci_n))
                .setMessage(getString(R.string.es_necesaria_una_conexion_a_internet_para_guardar_la_afinacion))
                .setView(input)
                .setPositiveButton(getString(R.string.guardar_afinacion), (dialogInterface, i) -> {
                    String title = input.getText().toString();
                    if (title.equals("")) {
                        DialogInfo.dialogInfoBuilder(this, "", getString(R.string.el_titulo_no_puede_estar_vac_o)).show();
                        return;
                    }
                    String url = "/Tunings?title=" + title + "&frequencies=" + getFormattedStrings() + "&email="+getCurrentUser();
                    volleyService.postStringDataVolley(url);
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.cancelar), (dialog1, which) -> dialog1.cancel())
                .setOnCancelListener(DialogInterface::cancel)
                .create();
        float dpi = this.getResources().getDisplayMetrics().density;
        dialog.setView(input, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
        return dialog;
    }

    private String getFormattedStrings() {
        int length = frequencies.length;
        String[] formattedStrings = new String[length];
        for (int i = 0; i < length; i++) {
            formattedStrings[i] = noteNames[i] + " " + frequencies[i];
        }
        return Arrays.toString(formattedStrings).replace("#", "%23");
        //return ;
    }

    private String getCurrentUser() {
        return archivo.getString("email", "");
    }

    private void initVolleyCallback() {
        resultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d("notifySuccess", "Volley requester " + requestType);
                Log.d("notifySuccess", "Volley JSON post" + response);
                finish();
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                error.printStackTrace();
                String body = "";
                String errorCode = "";
                if (error.networkResponse != null) {
                    body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    errorCode = "" + error.networkResponse.statusCode;
                }
                Toast.makeText(TuningCreationActivity.this, "failed: " + body + " " + errorCode, Toast.LENGTH_LONG).show();
                Log.d("notifyError", "Volley requester " + requestType);
                Log.d("notifyError", "Volley JSON post" + "That didn't work!" + error + " " + errorCode);
                Log.d("notifyError", "Error: " + error
                        + "\nStatus Code " + errorCode
                        + "\nResponse Data " + body
                        + "\nCause " + error.getCause()
                        + "\nmessage " + error.getMessage());
            }
        };
    }

    private void recalculateTuning() {
        //Selection on same item doesnt call on item listener
        double A4 = Double.parseDouble(referenceNote.getText().toString());
        //0.03716272234383503 resultado de math.pow(raizDuoDecimade2, -20??)
        double C0 = A4 * 0.03716272234383503;
        double raizDuodecimaDe2 = 1.05946309435929;
        for (int i = 0; i < 6; i++) {
            frequencies[i] = C0 * Math.pow(raizDuodecimaDe2, strings[i].getSelectedItemPosition());
        }
        frequencies[6] = Double.parseDouble(referenceNote.getText().toString());
    }


    private void resetTuning() {
        for (int i = 0; i < strings.length; i++) {
            strings[i].setSelection(standarTuningPosition[i]);
        }
        double referenceNote = Double.parseDouble(this.referenceNote.getText().toString());
        if (referenceNote != 440.) {
            this.referenceNote.setText(R.string.la4_freq);
        }
        frequencies[6] = referenceNote;
    }


    private void initItems() {
        allNotes = new String[72];
        String[] name = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int k = 0;
        for (int i = 1; i <= 6; i++) {
            for (int j = 0; j < 12; j++) {
                allNotes[k] = name[j] + (i - 1);
                k++;
            }
        }
    }
}