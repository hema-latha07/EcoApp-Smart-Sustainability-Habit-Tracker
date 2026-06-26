package com.example.ecoapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private DatabaseHelper db;
    private String userEmail;
    private ArrayList<String> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.historyToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        lvHistory = findViewById(R.id.lvHistory);
        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        historyList = new ArrayList<>();

        loadHistory();
    }

    private void loadHistory() {
        Cursor cursor = db.getUserActivityHistory(userEmail);

        // Indices based on our DatabaseHelper columns
        // 2 = ACTIVITY_DONE, 3 = POINTS_GAINED, 4 = DATE_TIME
        if (cursor.moveToFirst()) {
            do {
                String action = cursor.getString(2);
                int points = cursor.getInt(3);
                String date = cursor.getString(4);

                String sign = (points >= 0) ? "+" : ""; // Show + for gains, - for redemptions
                historyList.add(action + "\n" + date + " | " + sign + points + " pts");
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, historyList);
        lvHistory.setAdapter(adapter);
    }
}