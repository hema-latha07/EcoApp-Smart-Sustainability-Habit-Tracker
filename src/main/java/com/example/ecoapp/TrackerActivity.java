package com.example.ecoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackerActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;

    private TextView tvDistance, tvPoints, tvStatus;
    private Button btnToggleTracking;
    private ImageButton btnBack;

    private boolean isTracking = false;
    private float totalDistance = 0;
    private String userEmail;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvDistance = findViewById(R.id.tvDistance);
        tvPoints = findViewById(R.id.tvPointsGained);
        tvStatus = findViewById(R.id.tvTrackingStatus);
        btnToggleTracking = findViewById(R.id.btnToggleTracking);
        btnBack = findViewById(R.id.btnBackTracker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadDailyProgress();

        btnBack.setOnClickListener(v -> finish());

        btnToggleTracking.setOnClickListener(v -> {
            if (isTracking) {
                stopTracking();
            } else {
                checkPermissionsAndStart();
            }
        });
    }

    private void loadDailyProgress() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = db.getLastTrackingDate(userEmail);

        if (today.equals(lastDate)) {
            totalDistance = db.getDailyDistance(userEmail);
        } else {
            totalDistance = 0;
        }
        updateUI();
    }

    private void checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        isTracking = true;
        lastLocation = null;
        loadDailyProgress();

        btnToggleTracking.setText("Stop Journey");
        btnToggleTracking.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        tvStatus.setText("Status: Tracking your green commute...");

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // 1. Accuracy Filter: Skip locations with poor signal (> 20 meters error)
                    if (location.getAccuracy() > 20) {
                        continue;
                    }

                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(location);

                        // 2. Threshold Check: Only add distance if it's over 7 meters
                        // This stops the meter from increasing while sitting still (GPS Drift)
                        if (distance > 3.0) {
                            totalDistance += distance;
                            lastLocation = location; // Only update lastLocation on real moves
                            updateUI();
                        }
                    } else {
                        lastLocation = location;
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Toast.makeText(this, "Journey Started!", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        isTracking = false;
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        btnToggleTracking.setText("Start Green Journey");
        btnToggleTracking.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        tvStatus.setText("Status: Journey Completed");

        db.saveDailyProgress(userEmail, totalDistance);

        // Award points: 10 points for every 200m
        int earnedPoints = (int) (totalDistance / 200) * 10;

        if (earnedPoints > 0) {
            String desc = String.format(Locale.getDefault(), "Green Commute (%.1f m)", totalDistance);
            db.addEcoPoints(userEmail, earnedPoints, desc);
            Toast.makeText(this, "Earned " + earnedPoints + " Eco Points!", Toast.LENGTH_LONG).show();
        }

        checkMilestones();
        updateUI();
    }

    private void checkMilestones() {
        int totalPoints = db.getUserPoints(userEmail);
        if (totalPoints >= 100) {
            Toast.makeText(this, "Eco Hero Milestone: Certificate Unlocked in Profile!", Toast.LENGTH_SHORT).show();
        }
        if (totalPoints >= 500) {
            Toast.makeText(this, "Eco Leader Milestone reached!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f m", totalDistance));
        int currentSessionPoints = (int) (totalDistance / 200) * 10;
        tvPoints.setText(String.valueOf(currentSessionPoints));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}