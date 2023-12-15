package com.example.guitartraina.activities;

import static com.example.guitartraina.util.EncryptedSharedPreferences.getEncryptedSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.YoutubePlayerActivity;
import com.example.guitartraina.api.IResult;
import com.example.guitartraina.api.VolleyService;
import com.example.guitartraina.util.DialogInfo;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {
    private List<DateValue> dates = new ArrayList<>();
    private IResult resultCallback = null;
    private BarChart chart;
    private Spinner spinModule;
    private Button btnNextMonth, btnPreviousMonth;
    private TextView tvMonth;
    private int textColor; // Get the color from resources
    private SharedPreferences archivo;
    private VolleyService volleyService;
    private int month = 1;
    private String userName = "";
    private int module = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        archivo = getEncryptedSharedPreferences(this);
        initVolleyCallback();
        volleyService = new VolleyService(resultCallback, this);

        // Get the current theme of the activity
        Resources.Theme currentTheme = ProgressActivity.this.getTheme();
        // Create a new TypedValue object to hold the color value
        TypedValue typedValue = new TypedValue();
        // Retrieve the color value of the text color attribute from the current theme
        currentTheme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        // Get the color value as an integer
        textColor = typedValue.data;
        tvMonth = findViewById(R.id.month_tv);
        btnPreviousMonth = findViewById(R.id.ant_mes_button);
        btnNextMonth = findViewById(R.id.sig_mes_button);
        chart = findViewById(R.id.chartContainer);
        spinModule = findViewById(R.id.module_select_spin);
        if (ProgressActivity.this.getIntent().hasExtra("module")) {
            int module = ProgressActivity.this.getIntent().getIntExtra("module", 1);
            spinModule.setSelection(module - 1);
            this.module = module;
        }
        chart.setVisibility(View.GONE);
        tvMonth.setText(String.format(Locale.getDefault(), getString(R.string.info_charts), month));
        volleyService.getStringWithIdDataVolley("/Users?email=" + getCurrentUser(), "GET USER");
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
                    .putExtra("video", "\"https://www.youtube.com/embed/gp0wYX_6f_0\"")
                    .putExtra("titulo", R.string.progreso)
                    .putExtra("cuerpo", R.string.progoras));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private String getCurrentUser() {
        return archivo.getString("email", "");
    }

    void initVolleyCallback() {
        resultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d("notifySuccess", "Volley requester " + requestType);
                Log.d("notifySuccess", "Volley JSON post" + response);
                if (requestType.equals("GET USER")) {
                    spinModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                            module = itemPosition + 1;
                            volleyService.getStringDataVolley("/Scores/daily-averages?username=" + userName + "&module=" + module + "&month=" + month);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    btnPreviousMonth.setOnClickListener(view -> {
                        month++;
                        tvMonth.setText(String.format(Locale.getDefault(), getString(R.string.info_charts), month));
                        volleyService.getStringDataVolley("/Scores/daily-averages?username=" + userName + "&module=" + module + "&month=" + month);
                    });
                    btnNextMonth.setOnClickListener(view -> {
                        if (month > 1)
                            month--;
                        tvMonth.setText(String.format(Locale.getDefault(), getString(R.string.info_charts), month));
                        volleyService.getStringDataVolley("/Scores/daily-averages?username=" + userName + "&module=" + module + "&month=" + month);
                    });
                    userName = ((String) response).split(":")[2].replaceAll("\"", "").split(",")[0];
                    volleyService.getStringDataVolley("/Scores/daily-averages?username=" + userName + "&module=" + module + "&month=" + month);
                    return;
                }

                Type type = new TypeToken<List<DateValue>>() {
                }.getType();
                dates = new ArrayList<>();
                dates.addAll(new Gson().fromJson(((String) response).replaceAll("T00:00:00", ""), type));

                chart.setVisibility(View.VISIBLE);
                setupChart(chart);
                setData(chart, dates);
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {

                error.printStackTrace();
                String body = "";
                String errorCode = "";
                try {
                    errorCode = "" + error.networkResponse.statusCode;
                    body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (requestType.equals("GET")) {
                    chart.setVisibility(View.GONE);
                    if (errorCode.equals("404")) {
                        return;
                    }
                }
                String cause = "";
                if (error.getCause() != null) {
                    cause = error.getCause().getMessage();
                }
                Toast.makeText(ProgressActivity.this, "failed: " + cause + " " + body + " ", Toast.LENGTH_LONG).show();
                Log.d("notifyError", "Volley requester " + requestType);
                Log.d("notifyError", "Volley JSON post" + "That didn't work!" + error + " " + errorCode);
                Log.d("notifyError", "Error: " + error
                        + "\nStatus Code " + errorCode
                        + "\nResponse Data " + body
                        + "\nCause " + error.getCause()
                        + "\nmessage " + error.getMessage());
            }
        };
    }

    private void setupChart(BarChart chart) {
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(textColor);
        chart.getLegend().setTextSize(15f);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(textColor);
        yLAxis.setTextSize(15f);
        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setEnabled(false);
        // Customize the x-axis labels
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(25);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setTextSize(20f);
        xAxis.setTextColor(textColor);

    }

    private void setData(BarChart chart, List<DateValue> dateValues) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < dateValues.size(); i++) {
            DateValue dateValue = dateValues.get(i);
            entries.add(new BarEntry(i, dateValue.getAverageScore()));
            labels.add(dateValue.getDate());
        }
        String label;
        int barColor;
        barColor = Color.BLUE;
        label = userName;
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(barColor);
        dataSet.setValueTextSize(20f);
        dataSet.setValueTextColor(textColor);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Set the initial zoom level
        float visibleRange = 5f; // Set the number of visible bars on the chart
        chart.setVisibleXRangeMaximum(visibleRange);
        chart.invalidate();
        // Add a click listener to the chart bars
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int selectedIndex = (int) e.getX();
                if (selectedIndex >= 0 && selectedIndex < dateValues.size()) {
                    DateValue selectedDateValue = dateValues.get(selectedIndex);
                    DialogInfo.dialogInfoBuilder(ProgressActivity.this, getString(R.string.difficulty), getString(R.string.dificultad_promedio) + selectedDateValue.date + getString(R.string.is) + selectedDateValue.getAverageDifficulty()).show();
                }
            }

            @Override
            public void onNothingSelected() {
                // Handle when no bar is selected
            }
        });
    }


    // Example class representing date and value pair
    private static class DateValue {
        private final String date;
        private final float averageScore;
        private final float averageDifficulty;

        public DateValue(String date, float averageScore, float averageDifficulty) {
            this.date = date;
            this.averageScore = averageScore;
            this.averageDifficulty = averageDifficulty;
        }

        public String getDate() {
            return date;
        }

        public float getAverageScore() {
            return averageScore;
        }

        public float getAverageDifficulty() {
            return averageDifficulty;
        }

    }
}