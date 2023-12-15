package com.example.guitartraina.ui.configuration;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static com.example.guitartraina.util.Config.getGainFromPreferences;
import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;
import static com.example.guitartraina.util.InfoLayout.createInfoLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.nfc.FormatException;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.Tuning;
import com.example.guitartraina.services.PostureNotificationService;
import com.example.guitartraina.services.PracticeNotificationService;
import com.example.guitartraina.ui.views.VolumeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;


public class ConfigurationFragment extends PreferenceFragmentCompat {
    private SharedPreferences archivo;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.config, rootKey);
        archivo = getEncryptedSharedPreferences(requireContext());
        SwitchPreferenceCompat practiceNotifications = findPreference("practice_notifications");
        if (practiceNotifications != null) {
            practiceNotifications.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    Intent intent = new Intent(requireContext(), PracticeNotificationService.class);
                    requireActivity().startService(intent);
                } else {
                    requireContext().stopService(new Intent(requireContext(), PracticeNotificationService.class));
                }
                return true;
            });
            practiceNotifications.setSummary(getString(R.string.minutes_practiced_today) + String.format(Locale.getDefault(), " %.2f", getSecondsPracticed() / 60f));
        }
        SwitchPreferenceCompat postureNotifications = findPreference("posture_notifications");
        if (postureNotifications != null) {
            postureNotifications.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    Intent intent = new Intent(requireContext(), PostureNotificationService.class);
                    requireActivity().startService(intent);
                } else {
                    requireContext().stopService(new Intent(requireContext(), PostureNotificationService.class));
                }
                return true;
            });
        }
        EditTextPreference practiceTime = findPreference("practice_notifications_time");
        if (practiceTime != null) {
            practiceTime.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                editText.setHint("HH:MM:SS");
                editText.setText(R.string.notification_time_5min);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            });
            practiceTime.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    validateDate(newValue.toString());
                    validateTotalTime(newValue.toString(), 300, 7200);
                    return true;
                } catch (FormatException ex) {
                    Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
        DropDownPreference tuning = findPreference("tuner_default");
        setDropDownPreferenceData(tuning);
        SeekBarPreference microphoneGain = findPreference("microphone_gain");
        if (microphoneGain != null) {
            microphoneGain.setOnPreferenceChangeListener((preference, newValue) -> {
                double value = Integer.parseInt(newValue.toString()) / 10.;
                if (value < 1.d) {
                    return false;
                }
                preference.setSummary("X" + value);
                return true;
            });
            microphoneGain.setOnPreferenceClickListener(preference -> {
                dialogBuilder().show();
                return true;
            });
        }
        SeekBarPreference microphoneSensibility = findPreference("tuner_sensibility");
        if (microphoneSensibility != null) {
            microphoneSensibility.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = newValue.toString();
                if (!value.equals("0")) {
                    value = "-" + value;
                } else {
                    Toast.makeText(requireContext(), R.string.mic_sens_info, Toast.LENGTH_SHORT).show();
                }
                value = value + "dB";
                preference.setSummary(value);
                return true;
            });
            microphoneSensibility.setOnPreferenceClickListener(preference -> {
                dialogBuilder().show();
                return true;
            });
        }

        Preference language = findPreference("change_language");
        if (language != null) {
            language.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(intent);
                return true;
            });
        }
        EditTextPreference postureTime = findPreference("posture_notifications_time");
        if (postureTime != null) {
            postureTime.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                editText.setHint("HH:MM:SS");
                editText.setText(R.string.notification_time_2min);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            });
            postureTime.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    validateDate(newValue.toString());
                    validateTotalTime(newValue.toString(), 120, 3600);
                    return true;
                } catch (FormatException ex) {
                    Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
    }
    private void setDropDownPreferenceData(DropDownPreference tuning) {
        List<Tuning> tunings;
        String jsonTunings = getResources().getString(R.string.default_tunings);
        Type type = new TypeToken<List<Tuning>>() {
        }.getType();
        tunings = new Gson().fromJson(jsonTunings, type);
        String jsonArrayLocalTunings = archivo.getString("custom_tunings", null);
        if (jsonArrayLocalTunings != null) {
            tunings.addAll(new Gson().fromJson(jsonArrayLocalTunings, type));
        }
        CharSequence[] id = new CharSequence[tunings.size()];
        CharSequence[] title = new CharSequence[tunings.size()];
        for (int i = 0; i < tunings.size(); i++) {
            id[i] = "" + tunings.get(i).getId();
            title[i] = tunings.get(i).getTitle();
        }
        tuning.setDefaultValue(id);
        tuning.setEntries(title);
        tuning.setEntryValues(id);

    }


    private void validateTotalTime(String Date, int minSeconds, int maxSeconds) throws FormatException {
        int seconds = 0;
        String[] date = Date.split(":");
        int[] intDate = stringArrayToIntArray(date);
        seconds += intDate[2];
        seconds += intDate[1] * 60;
        seconds += intDate[0] * 3600;
        if (seconds > maxSeconds) {
            throw new FormatException(getString(R.string.time_cant_be_more_than) + String.format("%.2f", (double) maxSeconds / 60) + getString(R.string.minutos));
        }
        if (seconds < minSeconds) {
            throw new FormatException(getString(R.string.time_cant_be_less_than) + String.format("%.2f", (double) minSeconds / 60) + getString(R.string.minutos));
        }


    }

    private void validateDate(String date) throws FormatException {
        if (date.length() != 8) {
            throw new FormatException(getString(R.string.time_format_lenght_error));
        }
        String[] splittedDate = date.split(":");
        if (splittedDate.length != 3) {
            throw new FormatException(getString(R.string.time_format_HHMMSS_error));
        }
        int[] numbers = stringArrayToIntArray(splittedDate);
        if (numbers[0] > 24) {
            throw new FormatException(getString(R.string.time_format_hh_error));
        }
        if (numbers[1] > 60) {
            throw new FormatException(getString(R.string.time_format_mm_error));
        }
        if (numbers[2] > 60) {
            throw new FormatException(getString(R.string.time_format_ss_error));
        }
    }

    private int[] stringArrayToIntArray(String[] splittedDate) {
        int[] numbers = new int[splittedDate.length];
        for (int i = 0; i < splittedDate.length; i++) {
            numbers[i] = Integer.parseInt(splittedDate[i]);
        }
        return numbers;
    }

    private int getSecondsPracticed() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int lastPractice = archivo.getInt("practiceDay", currentDay);
        if (currentDay != lastPractice) {
            SharedPreferences.Editor editor = archivo.edit();
            editor.putInt("secondsPracticed", 0);
            editor.putInt("practiceDay", currentDay);
            editor.apply();
        }
        return archivo.getInt("secondsPracticed", 0);
    }

    private AlertDialog dialogBuilder() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setPositiveButton(getString(R.string.tuner_ok), (dialogInterface, i) -> dialogInterface.dismiss()).create();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            LinearLayout linearLayout = createInfoLayout(getActivity(),R.string.info_mic_permiso);
            dialog.setView(linearLayout);
        } else {
            final AudioDispatcher[] dispatcher = new AudioDispatcher[1];
            View view = createVolumeLayout();
            dialog.setView(view);
            dialog.setOnShowListener(dialogInterface -> {
                VolumeView volumeView = view.findViewById(R.id.volumeView);
                TextView tvVolume = view.findViewById(R.id.tv_loudness);
                dispatcher[0] = recordAudio(volumeView, tvVolume);
            });
            dialog.setOnCancelListener(DialogInterface::dismiss);
            dialog.setOnDismissListener(dialogInterface -> {
                if (dispatcher[0] != null) {
                    dispatcher[0].stop();
                }
            });

        }
        return dialog;
    }

    private AudioDispatcher recordAudio(VolumeView volumeView, TextView tvVolume) {
        AudioDispatcher dispatcher;
        double gain = getGainFromPreferences(requireContext());
        AudioProcessor gainProcessor = new GainProcessor(gain);
        int RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(44100, CHANNEL_IN_MONO, ENCODING_PCM_16BIT);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, RECORD_BUFFER_SIZE, 0);
        dispatcher.addAudioProcessor(gainProcessor);
        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                double dB = audioEvent.getdBSPL();
                requireActivity().runOnUiThread(() -> {
                    volumeView.setVolume(dB);
                    tvVolume.setText(String.format(Locale.getDefault(),"%.2fdb", dB));
                });
                return true;
            }

            @Override
            public void processingFinished() {
            }
        });
        Thread recorderThread = new Thread(dispatcher, "Audio Dispatcher");
        recorderThread.start();
        return dispatcher;
    }

    private View createVolumeLayout() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        return inflater.inflate(R.layout.volume_layout, null);
    }
}