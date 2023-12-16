package com.example.guitartrainalocal.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.MasterKeys;


import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedSharedPreferences {

    public static SharedPreferences getEncryptedSharedPreferences(Context context) {
        String masterKeyAlias;
        SharedPreferences archivo = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            archivo = androidx.security.crypto.EncryptedSharedPreferences.create(
                    "archivo",
                    masterKeyAlias,
                    context,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return archivo;
    }
}
