package com.example.guitartrainalocal.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.activities.tuner.YoutubePlayerActivity;
import com.example.guitartrainalocal.databinding.ActivityNotificationsBinding;
import com.example.guitartrainalocal.services.Notification;
import com.example.guitartrainalocal.ui.views.adapter.NotificationsRVAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private ActivityNotificationsBinding binding;
    private List<Notification> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupRV();
    }

    private void setupRV() {
        notifications = getNotifications();
        binding.notificationsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.notificationsRv.setHasFixedSize(false);
        NotificationsRVAdapter notificationsRVAdapter = new NotificationsRVAdapter(notifications, this);
        notificationsRVAdapter.setOnDeleteTuningClickListener(view -> {
            int position = notificationsRVAdapter.getItem();
            notifications.remove(position);
            saveNotification(notifications);
            notificationsRVAdapter.notifyItemRemoved(position);
        });
        binding.notificationsRv.setAdapter(notificationsRVAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tutorial_btn) {
            startActivity(new Intent(this, YoutubePlayerActivity.class)
                    .putExtra("video", "\"https://www.youtube.com/embed/-c10xxq_7W0\"")
                    .putExtra("titulo", R.string.notifications)
                    .putExtra("cuerpo", R.string.notifiaca));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Notification> getNotifications() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serializedList = sharedPreferences.getString("notifications", null);
        if(serializedList != null){
            Gson gson = new Gson();
            return gson.fromJson(serializedList, new TypeToken<List<Notification>>(){}.getType());
        }
        else{
            return new ArrayList<>();
        }
    }

    private void saveNotification(List<com.example.guitartrainalocal.services.Notification> notifications){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String serializedList = gson.toJson(notifications);
        editor.putString("notifications", serializedList);
        editor.apply();
        Log.d(TAG, "saveNotification: " + notifications.toString());
    }
}