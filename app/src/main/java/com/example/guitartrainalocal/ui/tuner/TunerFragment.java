package com.example.guitartrainalocal.ui.tuner;

import static com.example.guitartrainalocal.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;
import static com.example.guitartrainalocal.util.InfoLayout.createInfoLayout;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.activities.tuner.Tuning;
import com.example.guitartrainalocal.activities.tuner.TuningsActivity;
import com.example.guitartrainalocal.databinding.FragmentAfinadorBinding;
import com.example.guitartrainalocal.ui.views.GuitarTunerView;
import com.example.guitartrainalocal.util.DialogInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.resample.RateTransposer;

public class TunerFragment extends Fragment {
    private FragmentAfinadorBinding binding;
    private GuitarTuner guitarTuner;
    private Button btnSwitchTunerMode;
    private Button btnSwitchTuning;
    private GuitarTunerView guitarTunerView;
    private SharedPreferences archivo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }else{
            archivo=getEncryptedSharedPreferences(requireContext());
            guitarTuner = new GuitarTuner(requireActivity());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAfinadorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        btnSwitchTunerMode = root.findViewById(R.id.switch_tunermode_button);
        btnSwitchTuning = root.findViewById(R.id.switch_tuning_button);
        guitarTunerView = root.findViewById(R.id.guitar_tuner);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            LinearLayout linearLayout = createInfoLayout(getActivity(),R.string.info_mic_permiso);
            return LayoutInflater.from(requireContext()).inflate(R.layout.fragment_afinador, linearLayout, true);
        }
        Tuning defaultTuning = getDefaultTuningFromSharedPreferences();
        guitarTunerView.setNoteNames(defaultTuning.getNoteNames());
        guitarTunerView.setHz(defaultTuning.getFrequencies());
        if(guitarTuner!=null){
            guitarTuner.setFrequencies(defaultTuning.getFrequencies());
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            guitarTuner = null;
            return;
        }
        if (requireActivity().getIntent().hasExtra("tuning")) {
            Tuning tuning = (Tuning) requireActivity().getIntent().getSerializableExtra("tuning");
            guitarTuner.setFrequencies(tuning.getFrequencies());
            guitarTunerView.setNoteNames(tuning.getNoteNames());
            guitarTunerView.setHz(tuning.getFrequencies());
        }
        btnSwitchTunerMode.setOnClickListener(view -> {
            int index = guitarTunerView.getTuningMode();
            if (index == 1) {
                guitarTuner.stop();
            } else if (index == 2) {
                guitarTuner.stop();
                guitarTuner.run();
            }
            index = (index + 1) % 3;
            guitarTunerView.setTuningMode(index);
        });
        btnSwitchTuning.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), TuningsActivity.class);
            startActivity(intent);
        });
        guitarTunerView.setStringOnClickListener(view -> playSound((GuitarTunerView) view));

        if (guitarTunerView.getTuningMode() != 2)
            guitarTuner.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (guitarTuner != null) {
            guitarTuner.stop();
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private Tuning getDefaultTuningFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String defaultTuning = sharedPreferences.getString("tuner_default", null);
        List<Tuning> tunings;
        String jsonTunings = getResources().getString(R.string.default_tunings);
        Type type = new TypeToken<List<Tuning>>() {
        }.getType();
        tunings = new Gson().fromJson(jsonTunings, type);
        if(defaultTuning !=null){
            String jsonArrayLocalTunings = archivo.getString("custom_tunings", null);
            if (jsonArrayLocalTunings != null) {
                tunings.addAll(new Gson().fromJson(jsonArrayLocalTunings, type));
            }
            for (int i=0; i<tunings.size();i++) {
                Tuning t = tunings.get(i);
                if((t.getId()+"").equals(defaultTuning)){
                    return t;
                }
            }
        }
        return tunings.get(0);
    }
    private void playSound(GuitarTunerView view) {
        double[] STANDAR_TUNING_FREQ = new double[]{82.41, 110.00, 146.83, 196.00, 246.94, 329.63, 440};
        int stringIndex = view.getSelectedString();
        double factor = 1 / (guitarTuner.getFrequencies()[stringIndex] / STANDAR_TUNING_FREQ[stringIndex]);
        int CHANNELS = 1;
        int BIT_DEPTH = 16;
        boolean BIG_ENDIAN = false;
        int SAMPLE_RATE = 44100;
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, BIT_DEPTH, CHANNELS, 2, 1, BIG_ENDIAN);
        InputStream wavStream;
        wavStream = getResources().openRawResource(getResources().getIdentifier(requireActivity().getPackageName() + ":raw/string" + stringIndex, null, null));
        UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream, audioFormat);
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(factor, SAMPLE_RATE));
        wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(factor, SAMPLE_RATE));
        AudioDispatcher dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(), wsola.getOverlap());
        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        if (factor != 1.0) {
            RateTransposer rateTransposer = new RateTransposer(factor);
            dispatcher.addAudioProcessor(rateTransposer);
        }
        AndroidAudioPlayer player = new AndroidAudioPlayer(audioFormat);
        dispatcher.addAudioProcessor(player);
        dispatcher.skip(0.1);
        Thread audioDispatcher = new Thread(dispatcher, "Audio Dispatcher");
        audioDispatcher.start();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    guitarTuner = new GuitarTuner(requireActivity());
                    requireActivity().recreate();
                } else {
                    DialogInfo.dialogInfoBuilder(requireContext(), "", getString(R.string.permission_required_x) + getString(R.string.microfono));
                }
            }
    );
}