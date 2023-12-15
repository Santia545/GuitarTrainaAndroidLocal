package com.example.guitartraina.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.YoutubePlayerActivity;
import com.example.guitartraina.databinding.ActivityMainBinding;
import com.example.guitartraina.services.PostureNotificationService;
import com.example.guitartraina.services.PracticeNotificationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.guitartraina.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navView= findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_afinador, R.id.navigation_dashboard, R.id.navigation_configuration)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);



        // retrieve the selected tab from the intent's extras (default to the first tab)

        if (arePracticeNotificationsEnabled() && isServiceNotRunning(PracticeNotificationService.class)) {
            Intent intent = new Intent(this, PracticeNotificationService.class);
            startService(intent);
        }
        if (arePostureNotificationsEnabled() && isServiceNotRunning(PostureNotificationService.class)) {
            Intent intent = new Intent(this, PostureNotificationService.class);
            startService(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int selectedTab = intent.getIntExtra("SELECTED_TAB", 0);
        // set the selected tab using the index
        navView.setSelectedItemId(navView.getMenu().getItem(selectedTab).getItemId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (arePostureNotificationsEnabled() && !isServiceNotRunning(PostureNotificationService.class)) {
            this.stopService(new Intent(this, PostureNotificationService.class));
        }

    }

    private boolean arePostureNotificationsEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("posture_notifications", false);
    }

    private boolean arePracticeNotificationsEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("practice_notifications", false);
    }

    private boolean isServiceNotRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
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
                    .putExtra("video", "\"https://www.youtube.com/embed/nuunHaOHdMY\"")
                    .putExtra("titulo", R.string.main_page)
                    .putExtra("cuerpo", R.string.main));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}