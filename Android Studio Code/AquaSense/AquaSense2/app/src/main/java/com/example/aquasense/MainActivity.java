package com.example.aquasense;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import cjh.WaveProgressBarlibrary.WaveProgressBar;

public class MainActivity extends AppCompatActivity {

    int progress = 0;
    TextView progressTextView;
    CardView statusCardView;
    TextView statusTextView;
    private static int LOADING_TIME = 2000; // Adjust loading time as needed
    private WaveProgressBar waveProgressBar;
    private ListView logListView;
    private ArrayAdapter<String> logAdapter;
    private ArrayList<String> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Loading Screen
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(ProgressBar.GONE);
                initializeMainActivity();
            }
        }, LOADING_TIME);
    }

    private void initializeMainActivity() {
        setContentView(R.layout.activity_main);

        waveProgressBar = findViewById(R.id.waveProgressBar);
        statusCardView = findViewById(R.id.statusCardView);
        statusTextView = findViewById(R.id.statusTextView);
        progressTextView = findViewById(R.id.progressTextView);
        logListView = findViewById(R.id.logListView);
        logList = new ArrayList<>();
        logAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logList);
        logListView.setAdapter(logAdapter);

        // New CardView for displaying volume
        CardView volumeCardView = findViewById(R.id.volumeCardView);
        TextView volumeTextView = volumeCardView.findViewById(R.id.volumeTextView);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                progress++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        waveProgressBar.setProgress(progress);
                        updateStatus(progress);
                        updateProgressTextView(progress);
                        updateVolumeTextView(progress, volumeTextView); // Update volume TextView
                        logWaterLevelUpdate(); // Log water level update
                    }
                });

                if (progress == 100) {
                    progress = 0;
                }
            }
        };

        timer.schedule(timerTask, 0, 80);
    }

    private void updateStatus(int progress) {
        String status;
        if (progress >= 0 && progress <= 39) {
            status = "Low";
            statusCardView.setCardBackgroundColor(getResources().getColor(R.color.pastelgreen));
        } else if (progress >= 40 && progress <= 69) {
            status = "Med";
            statusCardView.setCardBackgroundColor(getResources().getColor(R.color.pastelorange));
        } else {
            status = "High";
            statusCardView.setCardBackgroundColor(getResources().getColor(R.color.pastelred));
        }
        statusTextView.setText("Liquid Level Status: " + status);
    }

    private void updateProgressTextView(int progress) {
        progressTextView.setText("Liquid Level Ratio: " + progress + "%");
    }

    private void updateVolumeTextView(int progress, TextView volumeTextView) {
        double volumeHeight; // Volume in feet
        if (progress >= 0 && progress <= 39) {
            volumeHeight = 1.0;
        } else if (progress >= 40 && progress <= 69) {
            volumeHeight = 1.3;
        } else {
            volumeHeight = 1.5;
        }
        volumeTextView.setText("Liquid Level Volume: " + volumeHeight + " ft");
    }

    private void logWaterLevelUpdate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        String status;
        if (progress >= 0 && progress <= 39) {
            status = "Low";
        } else if (progress >= 40 && progress <= 69) {
            status = "Medium";
        } else {
            status = "High";
        }
        String logMessage = "Water level updated at: " + currentDateandTime + ", Status: " + status;
        logList.add(logMessage);
        logAdapter.notifyDataSetChanged();
    }
}
