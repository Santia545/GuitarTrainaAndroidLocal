package com.example.guitartrainalocal.activities.bendtrainer;

import static com.example.guitartrainalocal.util.Config.getGainFromPreferences;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.ui.views.FrequencyView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class BendTrainer {
    private final Activity activity;
    private Thread pitchDetectorThread = null;
    private AudioDispatcher dispatcher = null;
    private final int SAMPLE_RATE = 44100;
    private final int RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private final int RECORD_BUFFER_OVERLAP = RECORD_BUFFER_SIZE / 2;
    private double initialFrequency = 0;
    private double bendHeight = 200.;
    private boolean isSoundDetected = false;
    private boolean isTrailUpdateScheduled = false;

    public BendTrainer(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        double gain = getGainFromPreferences(activity);
        double threshold = -100;
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, RECORD_BUFFER_SIZE, RECORD_BUFFER_OVERLAP);
        PitchDetectionHandler pdh = (PitchDetectionResult result, AudioEvent e) -> {
            FrequencyView frequencyView = activity.findViewById(R.id.frequencyView);
            final float pitchInHz = result.getPitch();
            if (pitchInHz == -1 || result.getProbability() < 0.90f) {
                return;
            }
            if (initialFrequency == 0) {
                String[] notes = {
                        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
                };
                double[] frequencies = {
                        16.35, 17.32, 18.35, 19.45, 20.6, 21.83, 23.12,
                        24.5, 25.96, 27.5, 29.14, 30.87, 32.7, 34.65,
                        36.71, 38.89, 41.2, 43.65, 46.25, 49.0, 51.91,
                        55.0, 58.27, 61.74, 65.41, 69.3, 73.42, 77.78,
                        82.41, 87.31, 92.5, 98.0, 103.83, 110.0, 116.54,
                        123.47, 130.81, 138.59, 146.83, 155.56, 164.81,
                        174.61, 185.0, 196.0, 207.65, 220.0, 233.08,
                        246.94, 261.63, 277.18, 293.66, 311.13, 329.63,
                        349.23, 369.99, 392.0, 415.3, 440.0, 466.16,
                        493.88, 523.25, 554.37, 587.33, 622.25, 659.25,
                        698.46, 739.99, 783.99, 830.61, 880.0, 932.33,
                        987.77, 1046.5, 1108.73, 1174.66, 1244.51, 1318.51,
                        1396.91, 1479.98, 1567.98, 1661.22, 1760.0, 1864.66,
                        1975.53, 2093.0, 2217.46, 2349.32, 2489.02, 2637.02,
                        2793.83, 2959.96, 3135.96, 3322.44, 3520.0, 3729.31,
                        3951.07, 4186.01, 4434.92, 4698.63, 4978.03, 5274.04,
                        5587.65, 5919.91, 6271.93, 6644.88, 7040.0, 7458.62,
                        7902.13
                };
                int closestIndex = findClosest(frequencies, pitchInHz);
                String note = notes[closestIndex % 12];
                initialFrequency = frequencies[closestIndex];
                frequencyView.setBaseNote(note);
            }
            frequencyView.setCents(getCentsOff(pitchInHz, initialFrequency) - bendHeight);
            Log.d("Pitch", pitchInHz + "probability: " + result.getProbability() + " loudness: " + e.getdBSPL());

            isSoundDetected = true;

            if (!isTrailUpdateScheduled) {
                scheduleTrailUpdate();
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, RECORD_BUFFER_SIZE, pdh);

        BendListener bendListener = volumen -> {
            System.out.println("Sound detected at:" + System.currentTimeMillis() + ", " + (int) (volumen) + "dB SPL\n");
            initialFrequency = 0;
            isSoundDetected = true;
            if (!isTrailUpdateScheduled) {
                scheduleTrailUpdate();
            }
            dispatcher.removeAudioProcessor(pitchProcessor);
            dispatcher.addAudioProcessor(pitchProcessor);
        };
        AudioProcessor gainProcessor = new GainProcessor(gain);
        SilenceDetector silenceDetector = new SilenceDetector(threshold, false);
        AudioProcessor silenceProcessor = new SilenceProcessor(silenceDetector, bendListener);
        dispatcher.addAudioProcessor(gainProcessor);
        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(silenceProcessor);
        pitchDetectorThread = new Thread(dispatcher, "Audio Dispatcher");
        pitchDetectorThread.setPriority(Thread.MAX_PRIORITY);
        pitchDetectorThread.start();
    }

    private void scheduleTrailUpdate() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updateTrail, 0, 100, TimeUnit.MILLISECONDS);
        isTrailUpdateScheduled = true;
    }

    private void updateTrail() {
        FrequencyView frequencyView = activity.findViewById(R.id.frequencyView);

        if (!isSoundDetected) {
            frequencyView.lowerTrailPoints();
        }

        isSoundDetected = false;
    }



    public void stop() {
        if (dispatcher != null && !dispatcher.isStopped()) {
            dispatcher.stop();
            dispatcher = null;
        }
        if (pitchDetectorThread != null) {
            try {
                pitchDetectorThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            pitchDetectorThread = null;
        }
    }

    public int findClosest(double[] array, double target) {
        int left = 0;
        int right = array.length - 1;

        while (left < right) {
            int mid = (left + right) / 2;

            if (array[mid] == target) {
                return mid;
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        // Check the closest values in the remaining two adjacent elements
        if (left > 0 && Math.abs(array[left - 1] - target) < Math.abs(array[left] - target)) {
            return left - 1;
        } else {
            return left;
        }
    }

    private double getCentsOff(float pitchInHz, double expectedFrequency) {
        //Math.log(2.0) = 0.6931471805599453;
        //12*100
        return 1200 * Math.log(pitchInHz / expectedFrequency) / 0.6931471805599453;
    }

    public void setBendHeight(double bendHeight) {
        this.bendHeight = bendHeight;
    }
}
