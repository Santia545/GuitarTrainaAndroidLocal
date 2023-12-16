package com.example.guitartrainalocal.activities;

import static com.example.guitartrainalocal.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.android.volley.VolleyError;
import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.activities.tuner.YoutubePlayerActivity;
import com.example.guitartrainalocal.api.IResult;
import com.example.guitartrainalocal.api.VolleyService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;


public class EarTrainerActivity extends AppCompatActivity {
    private String[] tilesNotes;
    private final FloatingActionButton[] tile = new FloatingActionButton[12];
    private final Button[] options = new Button[4];
    private Button repeatSound;
    private SwitchCompat swAutoDifficulty;
    private View.OnClickListener right;
    private View.OnClickListener wrong;
    private TextView TVprogress, TVdifficulty;
    private int counter = 1;
    private int rightAnswers = 0;
    private int wrongAnswers = 0;
    private IResult resultCallback = null;
    private VolleyService volleyService;
    private SharedPreferences archivo;
    private int difficulty;
    private Double averageScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ear_trainer);
        initVolleyCallback();
        archivo=getEncryptedSharedPreferences(this);
        volleyService = new VolleyService(resultCallback, this);
        swAutoDifficulty = findViewById(R.id.switch1);
        tilesNotes = getResources().getStringArray(R.array.piano_notes);
        repeatSound = findViewById(R.id.repeat);
        TVdifficulty = findViewById(R.id.difficultyTV);
        TVprogress = findViewById(R.id.progressTV);
        TVprogress.setText(String.format(Locale.getDefault(), getString(R.string.pregunta_x_15), counter));
        tile[0] = findViewById(R.id.tile1);
        tile[1] = findViewById(R.id.tile2);
        tile[2] = findViewById(R.id.tile3);
        tile[3] = findViewById(R.id.tile4);
        tile[4] = findViewById(R.id.tile5);
        tile[5] = findViewById(R.id.tile6);
        tile[6] = findViewById(R.id.tile7);
        tile[7] = findViewById(R.id.tile8);
        tile[8] = findViewById(R.id.tile9);
        tile[9] = findViewById(R.id.tile10);
        tile[10] = findViewById(R.id.tile11);
        tile[11] = findViewById(R.id.tile12);
        options[0] = findViewById(R.id.opc1);
        options[1] = findViewById(R.id.opc2);
        options[2] = findViewById(R.id.opc3);
        options[3] = findViewById(R.id.opc4);

        for (int i = 0; i < 12; i++) {
            int finalI = i;
            tile[i].setOnClickListener(view -> playSound(finalI + 1));
        }
        right = view -> {
            counter++;
            rightAnswers++;
            nextQuestionDialog(true);
        };
        wrong = view -> {
            counter++;
            wrongAnswers++;
            nextQuestionDialog(false);
        };
        swAutoDifficulty.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!compoundButton.isPressed())
                return;
            if (compoundButton.isChecked()) {
                recreate();
            } else {
                dialogBuilder3().show();
            }
        });
        volleyService.getStringDataVolley("/Scores/average?email=" + "&module=1");
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
                    .putExtra("video", "\"https://www.youtube.com/embed/tKgN1-Y7cKk\"")
                    .putExtra("titulo", R.string.entrenador_de_oido)
                    .putExtra("cuerpo", R.string.ear_trainer));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextQuestionDialog(boolean answer) {
        dialogBuilder2(answer).show();
    }

    private AlertDialog dialogBuilder2(boolean answer) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.siguiente, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (counter < 16) {
                        nextQuestion();
                    } else {
                        endProcess();
                    }
                })
                .setOnCancelListener(dialogInterface -> {
                    if (counter < 16) {
                        nextQuestion();
                    } else {
                        endProcess();
                    }
                })
                .create();
        if (answer) {
            dialog.setTitle(getString(R.string.respuesta_correcta));
        } else {
            dialog.setTitle(getString(R.string.respuesta_incorrecta));
        }
        return dialog;
    }

    private void endProcess() {
        double score = ((double) rightAnswers / 15.) * 100;
        saveProgress(score);
        AlertDialog dialog = dialogBuilder(score);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener((View v) -> {
            Intent progress = new Intent(EarTrainerActivity.this,ProgressActivity.class);
            progress.putExtra("module",1);
            startActivity(progress);
        });

        if (swAutoDifficulty.isChecked()) {
            checkScore(score);
        }
    }

    private void checkScore(double score) {
        int fails = archivo.getInt("failCounter", 0);

        if (score == 100.) {
            if (difficulty < 7) {
                difficulty++;
                setAutoDifficulty();
            }
            fails = 0;
        } else {
            fails++;
            if (fails == 4) {
                if (difficulty > 1) {
                    difficulty--;
                    setAutoDifficulty();
                }
                fails = 0;
            }
        }
        SharedPreferences.Editor editor = archivo.edit();
        editor.putInt("failCounter", fails);
        editor.apply();
    }

    private AlertDialog dialogBuilder3() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.cantidad_de_notas);
        int minValue = 1;
        int maxValue = 7;
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            try {
                int inputVal = Integer.parseInt(dest.toString() + source.toString());
                if (inputVal >= minValue && inputVal <= maxValue)
                    return null;
            } catch (NumberFormatException ignored) {
            }
            return "";
        };
        input.setFilters(new InputFilter[]{inputFilter});
        AlertDialog dialog = new AlertDialog.Builder(EarTrainerActivity.this)
                .setTitle(getString(R.string.ingresa_la_dificultad))
                .setMessage(getString(R.string.difficulty_change_description))
                .setView(input)
                .setPositiveButton(getString(R.string.save), (dialogInterface, i) -> {
                    String difficulty = input.getText().toString();
                    if (difficulty.equals("")) {
                        Toast.makeText(EarTrainerActivity.this, getString(R.string.empty_difficult_error), Toast.LENGTH_SHORT).show();
                        dialogInterface.cancel();
                        return;
                    }
                    this.difficulty = Integer.parseInt(difficulty);
                    setManualDifficulty();
                    recreate();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog1, which) -> dialog1.cancel())
                .setOnCancelListener(dialogInterface -> swAutoDifficulty.setChecked(true))
                .create();
        float dpi = this.getResources().getDisplayMetrics().density;
        dialog.setView(input, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
        return dialog;
    }

    private AlertDialog dialogBuilder(double score) {
        String message=getString(R.string.puntuacion) + String.format(Locale.getDefault(), "%.2f", score) + getString(R.string.aciertos) + rightAnswers + getString(R.string.fallos) + wrongAnswers;
        if(averageScore !=null) {
            message+="\n"+getString(R.string.historic_average_score)+ String.format(Locale.getDefault(), " %.2f",averageScore) ;
        }
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sesion_finalizada))
                .setMessage(message)
                .setPositiveButton(getString(R.string.nueva_sesion), (dialogInterface, i) -> {
                    recreate();
                    dialogInterface.dismiss();
                })
                .setNeutralButton(getString(R.string.progreso), null)
                .setNegativeButton(getString(R.string.salir), (dialog1, which) -> dialog1.cancel())
                .setOnCancelListener(dialogInterface ->
                        finish()
                )
                .create();
    }

    private void saveProgress(double score) {
        String url = "/Scores?module=1&score=" + score + "&difficulty=" + difficulty + "&email=" ;
        volleyService.postStringDataVolley(url);
    }


    private void nextQuestion() {
        TVprogress.setText(String.format(Locale.getDefault(), getString(R.string.pregunta_x_15), counter));
        genQuestion();
    }

    private void playSound(int tileIndex) {
        int BIT_DEPTH = 16;
        boolean BIG_ENDIAN = false;
        int SAMPLE_RATE = 44100;
        int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, BIT_DEPTH, 1, 2, 1, BIG_ENDIAN);
        InputStream wavStream;
        wavStream = getResources().openRawResource(getResources().getIdentifier(EarTrainerActivity.this.getPackageName() + ":raw/tile" + tileIndex, null, null));
        UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream, audioFormat);
        AudioDispatcher dispatcher = new AudioDispatcher(audioStream, BUFFER_SIZE, BUFFER_SIZE / 2);

        AndroidAudioPlayer player = new AndroidAudioPlayer(audioFormat);
        dispatcher.addAudioProcessor(player);
        dispatcher.skip(0.1);
        Thread audioDispatcher = new Thread(dispatcher, "Audio Dispatcher");
        audioDispatcher.start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        difficulty = getDifficulty();
        TVdifficulty.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.difficulty), difficulty));
        genQuestion();
    }

    private void genQuestion() {
        int counter = 1;
        int[][] randomNumbersMatrix;
        randomNumbersMatrix = genRandomUniqueBidimentionalArray();

        long seed = System.currentTimeMillis();
        Random random = new Random(seed);

        for (int randomNumber : randomNumbersMatrix[0]) {
            playSound(randomNumber + 1);
        }
        int randomOption = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            if (i == randomOption) {
                options[i].setOnClickListener(right);
                //options[i].setBackgroundColor(Color.GREEN);
                String opc = randomArrayToNoteString(randomNumbersMatrix[0]);
                options[i].setText(String.format("%s", opc));
            } else {
                options[i].setOnClickListener(wrong);
                String opc = randomArrayToNoteString(randomNumbersMatrix[counter]);
                //options[i].setBackgroundColor(Color.RED);
                options[i].setText(String.format("%s", opc));
                counter++;
            }
        }
        repeatSound.setOnClickListener(view -> {
            for (int randomNumber : randomNumbersMatrix[0]) {
                playSound(randomNumber + 1);
            }
        });
    }

    private String randomArrayToNoteString(int[] row) {
        StringBuilder opc = new StringBuilder();
        for (int j = 0; j < row.length; j++) {
            if (j == row.length - 1) {
                opc.append(tilesNotes[row[j]]);
            } else {
                opc.append(tilesNotes[row[j]]).append(",");
            }
        }
        return opc.toString();
    }

    private int[][] genRandomUniqueBidimentionalArray() {
        int[][] randomNumbersMatrix = new int[4][];
        int count = 0;
        while (count < 4) {
            int[] row = genRandomUniqueArray();
            if (isRowUnique(row, randomNumbersMatrix)) {
                randomNumbersMatrix[count] = row;
                count++;
            }
        }
        return randomNumbersMatrix;
    }

    private boolean isRowUnique(int[] newRow, int[][] bidimensionalArray) {
        for (int[] row : bidimensionalArray) {
            if (Arrays.equals(row, newRow)) {
                return false;
            }
        }
        return true;
    }

    private int[] genRandomUniqueArray() {
        int[] randomNumbersArray = new int[difficulty];
        int count = 0;
        while (count < difficulty) {
            long seed = System.currentTimeMillis();
            Random random = new Random(seed);
            int randomNumber = random.nextInt(12);
            // Check if the generated random number is already in the randomNumbersArray
            boolean isDuplicate = false;
            for (int i = 0; i < count; i++) {
                if (randomNumbersArray[i] == randomNumber) {
                    isDuplicate = true;
                    break;
                }
            }
            // If the number is unique, add it to the array
            if (!isDuplicate) {
                randomNumbersArray[count] = randomNumber;
                count++;
            }
        }
        return randomNumbersArray;
    }

    void initVolleyCallback() {
        resultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d("notifySuccess", "Volley requester " + requestType);
                Log.d("notifySuccess", "Volley JSON post" + response);
                if (requestType.equals("GET")) {
                    averageScore = Double.parseDouble((String) response);
                }
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                error.printStackTrace();
                String body = "";
                String errorCode = "";
                try {
                    errorCode = "" + error.networkResponse.statusCode;
                    body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String cause = "";
                if (error.getCause() != null) {
                    cause = error.getCause().getMessage();
                }
                Toast.makeText(EarTrainerActivity.this, getString(R.string.fallido) + cause + " " + body + " ", Toast.LENGTH_LONG).show();
                Log.d("notifyError", "Volley requester " + requestType);
                Log.d("notifyError", "Volley JSON post" + "That didn't work!" + error + " " + errorCode);
                Log.d("notifyError", "Error: " + error
                        + "\nStatus Code " + errorCode
                        + "\nResponse Data " + body
                        + "\nCause " + error.getCause()
                        + "\nmessage " + error.getMessage());
                if (requestType.equals("GET")) {
                    averageScore = null;
                }
            }
        };
    }


    private void setManualDifficulty() {
        SharedPreferences.Editor editor = archivo.edit();
        editor.putInt("earManualDifficulty", difficulty);
        editor.apply();
    }

    private void setAutoDifficulty() {
        SharedPreferences.Editor editor = archivo.edit();
        editor.putInt("earAutoDifficulty", difficulty);
        editor.apply();
    }



    private int getDifficulty() {
        if (this.swAutoDifficulty.isChecked()) {
            return archivo.getInt("earAutoDifficulty", 1);
        } else {
            return archivo.getInt("earManualDifficulty", 1);
        }
    }
}