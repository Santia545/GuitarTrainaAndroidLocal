package com.example.guitartraina.ui.metronome;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.metronome.Metronome;
import com.example.guitartraina.ui.views.MetronomeView;
import com.example.guitartraina.util.DialogInfo;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class MetronomeFragment extends Fragment {
    private Button btnPlay;

    public EditText getEditText() {
        return etBPM.getEditText();
    }

    private TextInputLayout etBPM;
    private Spinner timeSignature;
    private int noteType;
    private int noteNumber;
    private MetronomeView metronomeView;

    public Metronome getMetronome() {
        return metronome;
    }

    private Metronome metronome;
    private SwitchCompat swAccent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         metronome = new Metronome(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_metronome, container, false);
        btnPlay = view.findViewById(R.id.metronome_play_btn);
        etBPM = view.findViewById(R.id.beats_per_minute);
        timeSignature = view.findViewById(R.id.time_signature);
        metronomeView = view.findViewById(R.id.metronomeView);
        swAccent = view.findViewById(R.id.switch1);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            try {
                int inputVal = Integer.parseInt(dest.toString() + source.toString());
                if (inputVal >= 1 && inputVal <= 500)
                    return null;
            } catch (NumberFormatException ignored) {
            }
            return "";
        };
        Objects.requireNonNull(etBPM.getEditText()).setFilters(new InputFilter[]{inputFilter});
        addViewOnClickListeners();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(metronome.isRunning()){
            metronome.pause();
            btnPlay.setText(R.string.play);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(metronome!=null){
            metronome.pause();
        }
    }

    private void addViewOnClickListeners() {
        Objects.requireNonNull(etBPM.getEditText()).setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                String text = textView.getText().toString();
                if (text.equals("")) {
                    DialogInfo.dialogInfoBuilder(requireContext(), "", getString(R.string.metronome_empty_bpm_error));
                    etBPM.getEditText().setText(R.string.defalut_bpm);
                    return true; // Consume the event
                }
                int bpm = Integer.parseInt(text);
                metronome.setBpm(bpm);
                return true;
            }
            return false;
        });
        timeSignature.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                String[] timeSignature = adapterView.getItemAtPosition(itemPosition).toString().split("/");
                noteNumber = Integer.parseInt(timeSignature[0]);
                noteType = Integer.parseInt(timeSignature[1]);
                metronomeView.setNotesNumber(noteNumber);
                metronome.setNotesNumber(noteNumber);
                metronome.setNoteType(noteType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btnPlay.setOnClickListener(view -> {
            etBPM.getEditText().onEditorAction(EditorInfo.IME_ACTION_DONE);
            if(metronome.isRunning()){
                btnPlay.setText(R.string.play);
                metronome.pause();
            }else{
                btnPlay.setText(R.string.pause);
                metronome.run();
            }
        });
        swAccent.setOnCheckedChangeListener((compoundButton, b) -> {
            if(compoundButton.isChecked()){
                metronome.setNoteAccent(0);
            }else{
                metronome.setNoteAccent(-1);
            }
        });
    }
}