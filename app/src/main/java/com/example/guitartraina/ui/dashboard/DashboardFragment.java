package com.example.guitartraina.ui.dashboard;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.ChordLibraryActivity;
import com.example.guitartraina.activities.EarTrainerActivity;
import com.example.guitartraina.activities.LooperActivity;
import com.example.guitartraina.activities.NotificationsActivity;
import com.example.guitartraina.activities.ProgressActivity;
import com.example.guitartraina.activities.bendtrainer.BendTrainerActivity;
import com.example.guitartraina.activities.exercises.ExercisesActivity;
import com.example.guitartraina.activities.metronome.MetronomeActivity;
import com.example.guitartraina.databinding.FragmentDashboardBinding;
import com.example.guitartraina.services.Notification;
import com.example.guitartraina.util.DialogInfo;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private NavigationView dashnavView;
    private SharedPreferences archivo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        archivo = getEncryptedSharedPreferences(requireContext());
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashnavView = requireView().findViewById(R.id.dash_nav_view);
        dashnavView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_ejercicios) {
                Intent exercises = new Intent(requireContext(), ExercisesActivity.class);
                startActivity(exercises);
            } else if (itemId == R.id.navigation_entrenador_de_bends) {
                Intent bendTrainer = new Intent(requireContext(), BendTrainerActivity.class);
                startActivity(bendTrainer);
            } else if (itemId == R.id.navigation_metronomo) {
                Intent metronome = new Intent(requireContext(), MetronomeActivity.class);
                startActivity(metronome);
            } else if (itemId == R.id.navigation_entrenador_de_oido) {
                if (checkLogIn()) {
                    Intent earTrainer = new Intent(requireContext(), EarTrainerActivity.class);
                    startActivity(earTrainer);
                } else {
                    DialogInfo.dialogInfoBuilder(requireContext(), "", getString(R.string.guest_user_prohibited)).show();
                }
            } else if (itemId == R.id.navigation_rythm_looper) {
                if (!checkInternetConnection()) {
                    DialogInfo.dialogInfoBuilder(requireContext(), "", getString(R.string.no_internet_info)).show();
                } else {
                    Intent rythmLooper = new Intent(requireContext(), LooperActivity.class);
                    startActivity(rythmLooper);
                }
            } else if (itemId == R.id.navigation_chord_library) {
                Intent chordLibrary = new Intent(requireContext(), ChordLibraryActivity.class);
                startActivity(chordLibrary);
            } else if (itemId == R.id.navigation_progreso) {
                if (checkLogIn()) {
                    Intent progress = new Intent(requireContext(), ProgressActivity.class);
                    startActivity(progress);
                } else {
                    DialogInfo.dialogInfoBuilder(requireContext(), "", getString(R.string.guest_user_prohibited)).show();
                }
            } else if (itemId == R.id.notifications) {
                Intent notifs = new Intent(requireContext(), NotificationsActivity.class);
                startActivity(notifs);
            }
            return true;
        });
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private boolean checkLogIn() {
        if (archivo.contains("idUsuario")) {
            return !archivo.getString("idUsuario", "notlogged").equals("0");
        }
        return false;
    }





    @Override
    public void onResume() {
        super.onResume();
        List<Notification> notifications = getNotifications();
        if (notifications.size() < 1) {
            return;
        }
        if (notifications.size() > 99) {
            dashnavView.getMenu().findItem(R.id.notifications).setTitle(getString(R.string.notifications_count, 99));
            return;
        }
        dashnavView.getMenu().findItem(R.id.notifications).setTitle(getString(R.string.notifications_count, notifications.size()));
    }

    private List<Notification> getNotifications() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String serializedList = sharedPreferences.getString("notifications", null);
        if (serializedList != null) {
            Gson gson = new Gson();
            return gson.fromJson(serializedList, new TypeToken<List<Notification>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}