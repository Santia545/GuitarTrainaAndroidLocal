package com.example.guitartraina.util;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.guitartraina.R;

public class DialogInfo {
    public static AlertDialog dialogInfoBuilder(Context context, String title, String message) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(R.string.tuner_ok, (dialogInterface, i) -> dialogInterface.dismiss()).create();
    }

    public static AlertDialog dialogInfoBuilderWithClickListener(Context context, String title, String message, View.OnClickListener listener) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(R.string.tuner_ok, (dialogInterface, i) -> dialogInterface.dismiss()).setOnDismissListener(dialogInterface -> {
            if (listener != null) {
                listener.onClick(null);
            }
        }).create();
    }
}
