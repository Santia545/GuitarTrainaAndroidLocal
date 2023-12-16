package com.example.guitartrainalocal.activities.exercises;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

import android.media.AudioRecord;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;

public class GuitarStringsProcessor implements AudioProcessor {
    private boolean justPlucked=false;
    private long timePluck=0;

    public List<Note> getStringPluckTimes() {
        return stringPluckTimes;
    }
    private final List<Note> stringPluckTimes;
    private double previousdB = 0.0f;
    private int threshold=-100;
    private final PitchDetector detector;
    private final IOnGuitarStringPluckedListener onGuitarStringPluckedListener;

    public GuitarStringsProcessor(IOnGuitarStringPluckedListener onGuitarStringPluckedListener){
        stringPluckTimes = new ArrayList<>();
        int SAMPLE_RATE = 44100;
        int RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_MONO, ENCODING_PCM_16BIT);
        detector= new FastYin(SAMPLE_RATE, RECORD_BUFFER_SIZE);
        this.onGuitarStringPluckedListener = onGuitarStringPluckedListener;
    }
    @Override
    public boolean process(AudioEvent audioEvent) {
        double currentdB = audioEvent.getdBSPL();
        if (isAmplitudeRising(previousdB, currentdB) && audioEvent.getdBSPL() > threshold && !justPlucked) {
            Log.d("Sound", "pluck: "+ currentdB +">"+previousdB);
            justPlucked=true;
            timePluck = System.currentTimeMillis();
        }else{
            justPlucked=false;
        }
        float[] audioBuffer = audioEvent.getFloatBuffer();
        PitchDetectionResult result = detector.getPitch(audioBuffer);
        if(result.getProbability() > 0.85f && (System.currentTimeMillis()-timePluck)<200L) {
            //the algorithm can detect the pitch with a 90% accuracy
            Note note= processPitch(result.getPitch());
            if(Math.abs(note.getCentsOff())<20.){
                onGuitarStringPluckedListener.listen(note.getName());
                timePluck=0;
                Log.d("Note", note.getName());
                stringPluckTimes.add(note);
            }
        }
        previousdB = currentdB;
        return true;
    }

    @Override
    public void processingFinished() {

    }
    private boolean isAmplitudeRising(double previousAmplitude, double currentAmplitude) {
        double amplitudeDifference = currentAmplitude - previousAmplitude;
        return amplitudeDifference > 0;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    public Note processPitch(float pitchInHz) {
        double A4 = 440.0;
        double C0 = A4 * 0.03716272234383503;
        //Math.pow(2.0, -4.75) = 0.03716272234383503
        //Notation of pitches
        String[] name = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        //get how many steps pitch is above C0
        double r = 12.0 * Math.log(pitchInHz / C0) / 0.6931471805599453;
        //Math.log(2.0) = 0.6931471805599453;
        //get how many full half steps pitch is above C0
        int h = (int)Math.round(r);
        //get how far the actual pitch its from the closest full halfstep
        double diff = r - h;
        //turn steps into cents
        double cents = 100 * diff;
        String display;

        //Get in which octave the pitch its
        int octave = (int) Math.floor(h / 12.0);
        //Get which of the 12 notes the pitch is
        int n = h % 12;
        display=name[n] + octave;
        return new Note(display, cents);
    }

}
