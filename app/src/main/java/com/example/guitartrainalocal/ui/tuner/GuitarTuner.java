package com.example.guitartrainalocal.ui.tuner;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

import static com.example.guitartrainalocal.util.Config.getGainFromPreferences;
import static com.example.guitartrainalocal.util.Config.getSensibilityFromPreferences;

import android.app.Activity;
import android.media.AudioRecord;
import android.util.Log;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.ui.views.GuitarTunerView;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;

public class GuitarTuner {
    private final Activity activity;
    private final double[] STANDAR_TUNING_FREQ = new double[]{82.41, 110.00, 146.83, 196.00, 246.94, 329.63, 440};
    private double[] stringArray = STANDAR_TUNING_FREQ;
    private Thread pitchDetectorThread = null;
    private AudioDispatcher dispatcher = null;
    private final int SAMPLE_RATE = 44100;
    private final int RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_MONO, ENCODING_PCM_16BIT);
    private final int RECORD_BUFFER_OVERLAP = RECORD_BUFFER_SIZE / 2;

    public GuitarTuner(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        double gain = getGainFromPreferences(activity);
        int sensibility = getSensibilityFromPreferences(activity);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, RECORD_BUFFER_SIZE, RECORD_BUFFER_OVERLAP);
        PitchDetectionHandler pdh = (result, e) -> {
            //gets amplitude of the audio event e.getdBSPL() for ignoring quiet sounds
            GuitarTunerView guitarTunerView = activity.findViewById(R.id.guitar_tuner);
            final float pitchInHz = result.getPitch();
            if (pitchInHz == -1 || result.getProbability() < 0.90f || e.getdBSPL()<sensibility) {
                return;
            }
            Log.d("Pitch", pitchInHz + "probability: " + result.getProbability() + " loudness: " + e.getdBSPL());
            int stringIndex;
            if (guitarTunerView.getTuningMode() == 0) {
                stringIndex = getClosestString(pitchInHz);
                guitarTunerView.setTuningString(stringIndex, getCentsOff(pitchInHz, stringArray[stringIndex]));
            } else if (guitarTunerView.getTuningMode() == 1) {
                stringIndex = guitarTunerView.getSelectedString();
                guitarTunerView.setTuningString(stringIndex, getCentsOff(pitchInHz, stringArray[stringIndex]));
            }

        };
        AudioProcessor gainProcessor = new GainProcessor(gain);
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, RECORD_BUFFER_SIZE, pdh);
        dispatcher.addAudioProcessor(gainProcessor);
        dispatcher.addAudioProcessor(pitchProcessor);
        pitchDetectorThread = new Thread(dispatcher, "Audio Dispatcher");
        pitchDetectorThread.setPriority(Thread.MAX_PRIORITY);
        pitchDetectorThread.start();
    }

    public void setFrequencies(double[] stringArray) {
        this.stringArray = stringArray;
    }
    public double[] getFrequencies() {
        return this.stringArray;
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

    private double getCentsOff(float pitchInHz, double expectedFrequency) {
        //Math.log(2.0) = 0.6931471805599453;
        //12*100
        return 1200 * Math.log(pitchInHz / expectedFrequency) / 0.6931471805599453;
    }

    private int getClosestString(double frequency) {
        int index = 0;
        double minDiff = Double.MAX_VALUE;
        for (int i = 0; i < stringArray.length - 1; i++) {
            double diff = Math.abs(stringArray[i] - frequency);
            if (diff < minDiff) {
                minDiff = diff;
                index = i;
            }
        }
        return index;
    }


}
