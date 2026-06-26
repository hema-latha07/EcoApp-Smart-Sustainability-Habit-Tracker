package com.example.ecoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private TextView tvUsers, tvPoints, tvCerts;
    private Button btnRefresh, btnSendAlert, btnResetMonthly;
    private EditText etCampusAlert;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);

        // 1. Initialize Stat Views
        tvUsers = findViewById(R.id.tvAdminTotalUsers);
        tvPoints = findViewById(R.id.tvAdminGlobalPoints);
        tvCerts = findViewById(R.id.tvAdminCertsCount);

        // 2. Initialize Control Elements
        btnRefresh = findViewById(R.id.btnRefreshAdmin);
        etCampusAlert = findViewById(R.id.etCampusAlert);
        btnSendAlert = findViewById(R.id.btnSendAlert);
        btnResetMonthly = findViewById(R.id.btnResetMonthly);

        // Initial Stats Load
        updateStats();

        // 3. Refresh Stats Logic
        btnRefresh.setOnClickListener(v -> {
            updateStats();
            Toast.makeText(this, "Stats Updated", Toast.LENGTH_SHORT).show();
        });

        // 4. Broadcast Alert Logic
        btnSendAlert.setOnClickListener(v -> {
            String alertMessage = etCampusAlert.getText().toString().trim();
            if (!alertMessage.isEmpty()) {
                db.updateCampusAlert(alertMessage);
                etCampusAlert.setText(""); // Clear input
                Toast.makeText(this, "Broadcast Sent to Students!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Reset Points Logic (Monthly Maintenance)
        btnResetMonthly.setOnClickListener(v -> {
            db.resetAllUserPoints();
            updateStats(); // Show the 0 values immediately
            Toast.makeText(this, "All Points have been Reset for the Month", Toast.LENGTH_LONG).show();
        });
    }

    private void updateStats() {
        tvUsers.setText(String.valueOf(db.getTotalUsersCount()));
        tvPoints.setText(db.getGlobalEcoPoints() + " pts");
        tvCerts.setText(String.valueOf(db.getCertificatesIssuedCount()));
    }
}