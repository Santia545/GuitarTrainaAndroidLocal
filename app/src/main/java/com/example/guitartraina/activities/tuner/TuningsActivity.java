package com.example.guitartraina.activities.tuner;

import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.guitartraina.R;
import com.example.guitartraina.activities.MainActivity;
import com.example.guitartraina.api.IResult;
import com.example.guitartraina.api.VolleyService;
import com.example.guitartraina.ui.views.adapter.TuningsRVAdapter;
import com.example.guitartraina.util.DialogInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TuningsActivity extends AppCompatActivity {
    List<Tuning> tunings;
    RecyclerView recyclerView;
    private SharedPreferences archivo;
    private VolleyService volleyService;
    private IResult resultCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunings);
        recyclerView = findViewById(R.id.recyclerView);
        archivo = getEncryptedSharedPreferences(this);
        initVolleyCallback();
        volleyService = new VolleyService(resultCallback, this);
        Button createTuning = findViewById(R.id.tuning_create_btn);
        createTuning.setOnClickListener(view -> {
            Intent toTuningCreation = new Intent(TuningsActivity.this, TuningCreationActivity.class);
            startActivity(toTuningCreation);
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);
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
                    .putExtra("video", "\"https://www.youtube.com/embed/IZba31McknM\"")
                    .putExtra("titulo", R.string.afinaciones)
                    .putExtra("cuerpo", R.string.tunas));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTuningData();
    }

    private void initializeAdapter() {
        TuningsRVAdapter tuningsRVAdapter = new TuningsRVAdapter(tunings, true);
        tuningsRVAdapter.setOnChangeTuningClickListener(view -> {
            int position = tuningsRVAdapter.getItem();
            Tuning tuning = tuningsRVAdapter.getTuningList().get(position);
            Toast.makeText(TuningsActivity.this, getString(R.string.selected_tuning) + tuning.getTitle(), Toast.LENGTH_SHORT).show();
            Intent switchTuning = new Intent(TuningsActivity.this, MainActivity.class);
            switchTuning.putExtra("tuning", tuning);
            switchTuning.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(switchTuning);
            finishAfterTransition();
        });
        tuningsRVAdapter.setOnDeleteTuningClickListener(view -> {
            int position = tuningsRVAdapter.getItem();
            if (tunings.get(position).getId() < 0) {
                DialogInfo.dialogInfoBuilder(this, "", getString(R.string.delete_tuning_error)).show();
                return;
            }
            String url = "/Tunings?tuningId=" + tunings.get(position).getId();
            volleyService.deleteStringDataVolley(url);
            String jsonArrayLocalTunings = archivo.getString("custom_tunings", null);
            if (jsonArrayLocalTunings != null) {
                Type type = new TypeToken<List<Tuning>>() {
                }.getType();
                List<Tuning> aux = new Gson().fromJson(jsonArrayLocalTunings, type);
                for (int i = 0; i < aux.size(); i++) {
                    Tuning t = aux.get(i);
                    if (t.getId().equals(tunings.get(position).getId())) {
                        aux.remove(t);
                        break;
                    }
                }
                saveTuningsInLocalPreferences(new Gson().toJson(aux));
            }
            tunings.remove(position);
            tuningsRVAdapter.notifyItemRemoved(position);
        });
        recyclerView.setAdapter(tuningsRVAdapter);
    }

    private void getTuningData() {
        String jsonTunings = getResources().getString(R.string.default_tunings);
        Type type = new TypeToken<List<Tuning>>() {
        }.getType();
        tunings = new Gson().fromJson(jsonTunings, type);
        String url = "/Tunings?email=" + getCurrentUser();
        volleyService.getStringDataVolley(url);

    }

    private String getCurrentUser() {
        return archivo.getString("email", "");
    }

    private void initVolleyCallback() {
        resultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d("notifySuccess", "Volley requester " + requestType);
                Log.d("notifySuccess", "Volley JSON post" + response);
                if (requestType.equals("GET")) {
                    Type type = new TypeToken<List<Tuning>>() {
                    }.getType();
                    tunings.addAll(new Gson().fromJson((String) response, type));
                    saveTuningsInLocalPreferences((String) response);
                    initializeAdapter();
                }
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                error.printStackTrace();
                String body = "";
                String errorCode = "";
                if (error.networkResponse != null) {
                    body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    errorCode = "" + error.networkResponse.statusCode;
                }
                Log.d("notifyError", "Volley requester " + requestType);
                Log.d("notifyError", "Volley JSON post" + "That didn't work!" + error + " " + errorCode);
                Log.d("notifyError", "Error: " + error
                        + "\nStatus Code " + errorCode
                        + "\nResponse Data " + body
                        + "\nCause " + error.getCause()
                        + "\nmessage " + error.getMessage());
                if (requestType.equals("GET")) {
                    Toast.makeText(TuningsActivity.this, R.string.working_with_local_tuning_file, Toast.LENGTH_SHORT).show();
                    String jsonArrayLocalTunings = archivo.getString("custom_tunings", null);
                    if (jsonArrayLocalTunings != null) {
                        Type type = new TypeToken<List<Tuning>>() {
                        }.getType();
                        tunings.addAll(new Gson().fromJson(jsonArrayLocalTunings, type));
                    }
                    initializeAdapter();
                    return;
                } else if (requestType.equals("DELETE")) {
                    Toast.makeText(TuningsActivity.this, R.string.tuning_not_deleted_from_cloud_storage, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(TuningsActivity.this, "failed: " + body + " " + errorCode, Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void saveTuningsInLocalPreferences(String response) {
        SharedPreferences.Editor editor = archivo.edit();
        editor.putString("custom_tunings", response);
        editor.apply();
    }
}