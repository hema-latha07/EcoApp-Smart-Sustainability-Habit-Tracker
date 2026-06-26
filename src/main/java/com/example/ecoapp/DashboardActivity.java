package com.example.ecoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvName, tvDailyTip, tvEcoPoints, tvTotalActions, tvUserGoal, tvDashboardAlert; // Added tvDashboardAlert
    private ProgressBar goalProgressBar;
    private CardView cardTracker, cardCommunity, cardEcoLens, cardLeaderboard, cardDailyTip;
    private Button btnLogout, btnShareStatus;
    private ImageView ivProfileIcon, ivInfoIcon, ivHistoryIcon;
    private BottomNavigationView bottomNav;

    private DatabaseHelper db;
    private String userEmail;
    private boolean isRewardShown = false;

    private final String[] ecoTips = {
            "Turn off lights when leaving a room.",
            "Use a reusable bag for shopping.",
            "Plant a small sapling today!",
            "Reduce shower time by 2 minutes.",
            "Recycle old electronics safely.",
            "Switch to LED bulbs to save energy."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- 1. INITIALIZE DATABASE & DATA ---
        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // --- 2. INITIALIZE UI ELEMENTS ---
        tvName = findViewById(R.id.tvWelcomeName);
        tvDailyTip = findViewById(R.id.tvDailyTip);
        tvEcoPoints = findViewById(R.id.tvEcoPoints);
        tvTotalActions = findViewById(R.id.tvTotalActions);
        tvUserGoal = findViewById(R.id.tvUserGoal);
        tvDashboardAlert = findViewById(R.id.tvDashboardAlert); // Initialize the Alert TextView
        goalProgressBar = findViewById(R.id.goalProgressBar);

        cardTracker = findViewById(R.id.cardTracker);
        cardCommunity = findViewById(R.id.cardCommunity);
        cardEcoLens = findViewById(R.id.cardEcoLens);
        cardLeaderboard = findViewById(R.id.cardLeaderboard);
        cardDailyTip = findViewById(R.id.cardDailyTip);

        btnLogout = findViewById(R.id.btnLogout);
        btnShareStatus = findViewById(R.id.btnShareStatus);

        ivProfileIcon = findViewById(R.id.ivProfileButton);
        ivInfoIcon = findViewById(R.id.ivInfoIcon);
        ivHistoryIcon = findViewById(R.id.ivHistoryIcon);

        bottomNav = findViewById(R.id.bottomNavigation);

        // Set a random tip on start
        tvDailyTip.setText("Daily Tip: " + ecoTips[new Random().nextInt(ecoTips.length)]);

        // --- 3. CLICK LISTENERS ---

        if (ivProfileIcon != null) {
            ivProfileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            });
        }

        if (ivHistoryIcon != null) {
            ivHistoryIcon.setOnClickListener(v -> openHistory());
        }

        if (ivInfoIcon != null) ivInfoIcon.setOnClickListener(v -> openAboutPage());
        if (cardDailyTip != null) cardDailyTip.setOnClickListener(v -> openAboutPage());

        cardTracker.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrackerActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        cardCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        cardEcoLens.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcoQuizActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("TASK_TYPE", "general");
            startActivity(intent);
        });

        cardLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        btnShareStatus.setOnClickListener(v -> shareProgress());

        // --- 4. BOTTOM NAVIGATION LOGIC ---
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_tracker) {
                Intent intent = new Intent(this, TrackerActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
                return true;
            }

            if (id == R.id.nav_history) {
                openHistory();
                return true;
            }
            return false;
        });

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        startActivity(intent);
    }

    private void shareProgress() {
        int points = db.getUserPoints(userEmail);
        String message = "I've earned " + points + " Eco Points on EcoHero! Join the green movement.";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share Achievement"));
    }

    private void openAboutPage() {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardData();
    }

    private void refreshDashboardData() {
        if (userEmail != null) {
            // Fetch updated data from DatabaseHelper
            String latestName = db.getUserName(userEmail);
            int currentPoints = db.getUserPoints(userEmail);
            int totalActions = db.getTotalActionCount(userEmail);
            String primaryGoal = db.getUserGoal(userEmail);

            // --- FETCH CAMPUS ALERT ---
            String latestAlert = db.getLatestAlert();

            // Update UI
            tvName.setText("Hello, " + latestName + "!");
            tvEcoPoints.setText(String.valueOf(currentPoints));
            tvTotalActions.setText("Verified Actions: " + totalActions);

            // Display Alert Message
            if (tvDashboardAlert != null) {
                tvDashboardAlert.setText(latestAlert);
            }

            if (tvUserGoal != null) {
                tvUserGoal.setText("Goal: " + (primaryGoal != null ? primaryGoal : "Stay Green"));
            }

            if (goalProgressBar != null) {
                goalProgressBar.setMax(100);
                goalProgressBar.setProgress(Math.min(currentPoints, 100));
            }

            if (currentPoints >= 100 && !isRewardShown) {
                showRewardDialog();
                isRewardShown = true;
            }

            AlphaAnimation fadeIn = new AlphaAnimation(0.2f, 1.0f);
            fadeIn.setDuration(800);
            tvEcoPoints.startAnimation(fadeIn);
        }
    }

    private void showRewardDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 100 Points Reached!")
                .setMessage("You've become an Eco Hero! Check the About page to see your rewards.")
                .setPositiveButton("View Rewards", (dialog, which) -> openAboutPage())
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }
}