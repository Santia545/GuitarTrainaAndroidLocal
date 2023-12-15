package com.example.guitartraina.activities.bendtrainer;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;

public class SilenceProcessor implements AudioProcessor {
    private final SilenceDetector silenceDetector;
    private final BendListener bendListener;
    private double threshold = -100;

    public SilenceProcessor(SilenceDetector silenceDetector, BendListener bendListener) {
        this.silenceDetector = silenceDetector;
        this.bendListener = bendListener;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        System.out.println();
        double volumen = silenceDetector.currentSPL();
        if (volumen > threshold + 10) {
            bendListener.onListen(volumen);
        }
        threshold = volumen;
        return true;
    }

    @Override
    public void processingFinished() {

    }
}
