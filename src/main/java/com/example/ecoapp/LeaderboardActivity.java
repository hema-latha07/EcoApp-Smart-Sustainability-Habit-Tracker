package com.example.ecoapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView lvLeaderboard;
    private DatabaseHelper db;
    private ArrayList<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialization
        lvLeaderboard = findViewById(R.id.lvLeaderboard);
        Button btnBack = findViewById(R.id.btnBackLeaderboard);
        db = new DatabaseHelper(this);
        userList = new ArrayList<>();

        // Load and display sorted rankings
        loadLeaderboardData();

        // Navigation back to Dashboard
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLeaderboardData() {
        userList.clear();
        Cursor cursor = db.getLeaderboard(); // Ensure this method uses "ORDER BY points DESC"
        int rank = 1;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Fetching Name (Index 0) and Points (Index 1)
                String name = cursor.getString(0);
                int points = cursor.getInt(1);

                // Determining Emoji based on Rank
                String medal = "";
                if (rank == 1) medal = " 🥇";
                else if (rank == 2) medal = " 🥈";
                else if (rank == 3) medal = " 🥉";

                // Formatted Output: "1. K. Hemalatha - 150 pts 🥇"
                userList.add(rank + ". " + name + " - " + points + " pts" + medal);
                rank++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Simple adapter to fill the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, userList);
        lvLeaderboard.setAdapter(adapter);
    }
}