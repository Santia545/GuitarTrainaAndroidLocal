package com.example.guitartrainalocal.activities.exercises;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static com.example.guitartrainalocal.util.Config.getGainFromPreferences;
import static com.example.guitartrainalocal.util.Config.getSensibilityFromPreferences;
import static com.example.guitartrainalocal.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.ui.views.GuitarTabView;
import com.example.guitartrainalocal.util.Countdown;
import com.example.guitartrainalocal.util.DialogInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class ExerciseActivity extends AppCompatActivity {
    private Handler handler;
    private GuitarTabView guitarTabView;
    private List<Tab> sequence;
    private long initialPlayerDelay;
    private int bpm = 0;
    private ExercisesManager sequencesGenerator;
    private String exerciseType;
    private boolean pause = false;
    private Countdown countdown;
    private final int SAMPLE_RATE = 44100;
    private AudioDispatcher dispatcher = null;
    private final int RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_MONO, ENCODING_PCM_16BIT);
    private SharedPreferences archivo;
    private long starTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        archivo = getEncryptedSharedPreferences(this);
        ViewGroup rootView = findViewById(android.R.id.content);
        countdown = new Countdown(this, rootView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        guitarTabView = findViewById(R.id.guitarTabView);
        Button btnPause = findViewById(R.id.button2);

        bpm = getIntent().getIntExtra("bpm", 60);
        startDialogBuilder().show();
        handler = new Handler();
        sequencesGenerator = new ExercisesManager(this);
        exerciseType = getIntent().getStringExtra("exercise");
        if (exerciseType == null) {
            exerciseType = "sweep";
        }

        btnPause.setOnClickListener(view -> {
            Button btn = ((Button) view);
            if (!pause) {
                pause = true;
                btn.setText(R.string.play);
            } else {
                countdown.startCountdown(() -> {
                    btn.setText(R.string.pause);
                    pause = false;
                });
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (countdown != null) {
            countdown.stopCountdown();
        }

        if (dispatcher != null) {
            if (!dispatcher.isStopped()) {
                dispatcher.stop();
            }
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void recordAudio() {
        double gain = getGainFromPreferences(ExerciseActivity.this);
        int threshold = getSensibilityFromPreferences(ExerciseActivity.this);
        AudioProcessor gainProcessor = new GainProcessor(gain);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, RECORD_BUFFER_SIZE, 0);
        IOnGuitarStringPluckedListener onStringPlucked = (String note) -> runOnUiThread(()->guitarTabView.onListenEvent(note.replaceAll("\\d+", "")));
        GuitarStringsProcessor guitarStringsProcessor = new GuitarStringsProcessor(onStringPlucked);
        guitarStringsProcessor.setThreshold(threshold);
        dispatcher.addAudioProcessor(gainProcessor);
        dispatcher.addAudioProcessor(guitarStringsProcessor);
        Thread recorderThread = new Thread(dispatcher, "Audio Dispatcher");
        recorderThread.start();
    }


    private AlertDialog startDialogBuilder() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        input.setHint("BPM");
        input.setText(String.format(Locale.getDefault(), "%d", bpm));
        InputFilter precisionInputFilter = (source, start, end, dest, dstart, dend) -> {
            try {
                int inputVal = Integer.parseInt(dest.toString() + source.toString());
                if (inputVal >= 1 && inputVal <= 300) return null;
            } catch (NumberFormatException ignored) {
            }
            return "";
        };
        input.setFilters(new InputFilter[]{precisionInputFilter});
        AlertDialog dialog = new AlertDialog.Builder(ExerciseActivity.this).setTitle(R.string.ejercicios).setPositiveButton(R.string.start, (dialogInterface, i) -> {
            int velocidad = Integer.parseInt(input.getText().toString());
            if (velocidad < 30 || velocidad > 300) {
                AlertDialog dialogInfo = DialogInfo.dialogInfoBuilder(ExerciseActivity.this, "Error", getString(R.string.exercises_invalid_bpm_error));
                dialogInfo.setOnShowListener(dialogInterface1 -> dialogInfo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    startDialogBuilder().show();
                    dialogInfo.dismiss();
                }));
                dialogInfo.show();
                input.setText(String.format(Locale.getDefault(), "%d", 100));
            } else {
                bpm = velocidad;
                switch (exerciseType) {
                    case "spider":
                        sequence = sequencesGenerator.getSpiderSequence(bpm);
                        break;
                    case "sweep":
                        sequence = sequencesGenerator.getSweepSequence(bpm);
                        break;
                    case "arpeggios":
                        sequence = sequencesGenerator.getArpeggioSequence(bpm);
                        break;
                    case "scales":
                        sequence = sequencesGenerator.getScaleSequence(bpm);
                        break;
                    default:
                        finish();
                        break;
                }
                starTime= System.currentTimeMillis();
                recordAudio();
                handler.post(moveNotesRunnable);
                initialPlayerDelay = (long) guitarTabView.getTimePerView();
                startAddingNotes(0L);
            }
        }).setNegativeButton(R.string.salir, (dialog1, which) -> finish()).create();
        dialog.setCanceledOnTouchOutside(false);
        float dpi = this.getResources().getDisplayMetrics().density;
        dialog.setView(input, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
        return dialog;
    }

    private AlertDialog finalDialogBuilder(int rights, int wrongs) {
        double score = (double) rights / (rights + wrongs);
        String message = getString(R.string.puntuacion) + String.format(Locale.getDefault(), "%.2f", score * 100) + getString(R.string.aciertos) + rights + getString(R.string.fallos) + wrongs;
        AlertDialog dialog = new AlertDialog.Builder(ExerciseActivity.this).setTitle(R.string.sesion_finalizada).setMessage(message).setPositiveButton(R.string.next, (dialog1, which) -> {
            sequencesGenerator.increaseLevel(exerciseType);
            Intent nextLevel = new Intent(ExerciseActivity.this, ExerciseActivity.class);
            nextLevel.putExtra("exercise", exerciseType);
            nextLevel.putExtra("bpm", bpm);
            startActivity(nextLevel);
            finishAfterTransition();
        }).setNegativeButton(R.string.salir, (dialog1, which) -> dialog1.cancel()).setNeutralButton(R.string.repetir, (dialogInterface, i) -> recreate()).setOnCancelListener(dialogInterface -> finish()).create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (score <= 0.70) {
                dialog.setMessage(message + "\n" + getString(R.string.low_score_info));
                positiveButton.setEnabled(false);
            }
        });
        return dialog;
    }


    private final Runnable moveNotesRunnable = new Runnable() {
        @Override
        public void run() {
            if (!pause) {
                guitarTabView.moveNotes();
            }
            handler.postDelayed(moveNotesRunnable, 16); // 100 frames per second (1000ms / 60fps)
        }
    };

    private void startAddingNotes(Long time) {
        handler.postDelayed(() -> {
            if (pause) {
                startAddingNotes(time);
                return;
            }
            if (!sequence.isEmpty()) {
                guitarTabView.addNote(sequence.get(0));
                long timeAux = sequence.get(0).getTime();
                sequence.remove(0);
                startAddingNotes(timeAux);
            } else {
                handler.postDelayed(() -> {
                    if (!dispatcher.isStopped()) {
                        dispatcher.stop();
                    }
                    handler.removeCallbacksAndMessages(null);
                    saveSecondsPracticed();
                    finalDialogBuilder(guitarTabView.getRights(), guitarTabView.getWrongs()).show();
                    guitarTabView.clean();
                }, initialPlayerDelay);
            }
        }, time);
    }

    private void saveSecondsPracticed() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int secondsPracticedToday = archivo.getInt("secondsPracticed",0);
        secondsPracticedToday+=(System.currentTimeMillis()-starTime)/1000;
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
            SharedPreferences.Editor editor = archivo.edit();
            editor.putInt("secondsPracticed", secondsPracticedToday);
            editor.putInt("practiceDay", currentDay);
            editor.apply();
    }

}