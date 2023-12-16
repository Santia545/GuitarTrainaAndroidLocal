package com.example.guitartrainalocal.services;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.activities.NotificationsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostureNotificationService extends Service {

    private static final int NOTIFICATION_ID = 3;
    private static final String CHANNEL_ID = "posture_notification_channel";
    private static int interval; // 5 minutes in milliseconds
    private final Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        interval = getNotificationTimeFromPreferences();
        //interval = 5000;
        createNotificationChannel();

        runnable = () -> {
            sendNotification();
            handler.postDelayed(runnable, interval);
        };
        handler.postDelayed(runnable, interval);
    }

    private int getNotificationTimeFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultTuning = sharedPreferences.getString("posture_notifications_time", null);
        return stringToSeconds(defaultTuning) * 1000;
    }

    private int stringToSeconds(String practiceTime) {
        int seconds = 0;
        String[] date = practiceTime.split(":");
        int[] intDate = stringArrayToIntArray(date);
        seconds += intDate[2];
        seconds += intDate[1] * 60;
        seconds += intDate[0] * 3600;
        return seconds;
    }

    private int[] stringArrayToIntArray(String[] splittedDate) {
        int[] numbers = new int[splittedDate.length];
        for (int i = 0; i < splittedDate.length; i++) {
            numbers[i] = Integer.parseInt(splittedDate[i]);
        }
        return numbers;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Posture Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        List<Notification> notifications = getPrevNotifications();
        Intent resultIntent = new Intent(this, NotificationsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(getString(R.string.posture_reminder))
                .setContentText(getString(R.string.notification_posture_reminder_desc))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        notifications.add(0, new Notification(getString(R.string.posture_reminder), getString(R.string.notification_posture_reminder_desc), new Date(), 1));
        saveNotification(notifications);
    }

    private void saveNotification(List<Notification> notifications) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String serializedList = gson.toJson(notifications);
        editor.putString("notifications", serializedList);
        editor.apply();
        Log.d(TAG, "saveNotification: " + notifications.toString());
    }

    private List<Notification> getPrevNotifications() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serializedList = sharedPreferences.getString("notifications", null);
        if (serializedList != null) {
            Gson gson = new Gson();
            return gson.fromJson(serializedList, new TypeToken<List<Notification>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }
    }

}
