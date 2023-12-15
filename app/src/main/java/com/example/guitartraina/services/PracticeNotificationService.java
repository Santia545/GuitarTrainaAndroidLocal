package com.example.guitartraina.services;

import static android.content.ContentValues.TAG;
import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.app.Notification;
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

import com.example.guitartraina.R;
import com.example.guitartraina.activities.MainActivity;
import com.example.guitartraina.activities.NotificationsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class PracticeNotificationService extends Service {
    private static final String CHANNEL_ID = "practice_notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int INTERVAL = 3600000;//1 hour in miliseconds
    private SharedPreferences archivo;

    private final Handler handler= new Handler();
    private Runnable runnable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        archivo=getEncryptedSharedPreferences(this);
        startForeground(2, createNotification());
        createNotificationChannel();
        runnable = ()->{
            Date date = new Date();
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            int currentHour=calendar.get(Calendar.HOUR_OF_DAY);
            if(currentHour>11&&currentHour<22) { // gets hour in 24h format)
                int reminderTime=getPracticeReminderTime();
                int secondsPracticed = getSecondsPracticed();
                if (secondsPracticed < reminderTime) {
                    sendNotification();
                }
            }
            handler.postDelayed(runnable, INTERVAL);
        };
        handler.postDelayed(runnable, INTERVAL);
        return START_STICKY;
    }

    private int getSecondsPracticed() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int currentDay=calendar.get(Calendar.DAY_OF_YEAR);
        int lastPractice=archivo.getInt("practiceDay",currentDay);
        if(currentDay!=lastPractice){
            SharedPreferences.Editor editor = archivo.edit();
            editor.putInt("secondsPracticed",0);
            editor.putInt("practiceDay",currentDay);
            editor.apply();
        }
        return archivo.getInt("secondsPracticed",0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private int getPracticeReminderTime() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String practiceTime = sharedPreferences.getString("practice_notifications_time", null);
        return stringToSeconds(practiceTime);
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
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SELECTED_TAB", 2); // add extra data to the intent
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // add flags to clear the activity stack
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            pendingIntent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(), intent, -1);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(getString(R.string.practice_notification_service_title))
                .setContentText(getString(R.string.practice_notification_service_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Practice Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return builder.build();
    }

    private void sendNotification() {
        List<com.example.guitartraina.services.Notification> notifications =    getPrevNotifications();
        Intent resultIntent = new Intent(this, NotificationsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(getString(R.string.practice_reminder))
                .setContentText(getString(R.string.notification_practice_reminder_desc))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        notifications.add(0, new com.example.guitartraina.services.Notification(
                getString(R.string.practice_reminder),
                getString(R.string.notification_practice_reminder_desc),
                new Date(),
                2
        ));
        saveNotification(notifications);
    }

    private void saveNotification(List<com.example.guitartraina.services.Notification> notifications){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String serializedList = gson.toJson(notifications);
        editor.putString("notifications", serializedList);
        editor.apply();
        Log.d(TAG, "saveNotification: " + notifications.toString());
    }

    private List<com.example.guitartraina.services.Notification> getPrevNotifications() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serializedList = sharedPreferences.getString("notifications", null);
        if(serializedList != null){
            Gson gson = new Gson();
            return gson.fromJson(serializedList, new TypeToken<List<com.example.guitartraina.services.Notification>>(){}.getType());
        }
        else{
            return new ArrayList<>();
        }
    }
}
