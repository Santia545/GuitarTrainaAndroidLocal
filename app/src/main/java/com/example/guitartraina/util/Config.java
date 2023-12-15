package com.example.guitartraina.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Config {
    public static int getSensibilityFromPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int microphoneSensibility = sharedPreferences.getInt("tuner_sensibility", -100);
        return -microphoneSensibility;
    }

    public static double getGainFromPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int microphoneGain = sharedPreferences.getInt("microphone_gain", 10);
        return microphoneGain / 10.d;
    }
}
