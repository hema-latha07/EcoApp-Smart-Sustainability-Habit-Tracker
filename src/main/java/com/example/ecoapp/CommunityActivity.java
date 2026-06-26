package com.example.ecoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageButton btnBack;
    private TextView tvCommunityStats;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // 1. Initialize UI Elements
        recyclerView = findViewById(R.id.recyclerCommunityFeed);
        btnBack = findViewById(R.id.btnBackCommunity);
        tvCommunityStats = findViewById(R.id.tvCommunityStats);
        db = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Fetch REAL Data from the Database
        // This pulls activities from ALL users across the app
        List<String> activities = db.getCommunityFeed();

        // 3. Fallback for Demo: If no one has done anything yet, show a welcome message
        if (activities.isEmpty()) {
            activities.add("Welcome to the community! Be the first to verify a task. 🌱");
        }

        // 4. Set the Adapter with the Dynamic List
        CommunityAdapter adapter = new CommunityAdapter(activities);
        recyclerView.setAdapter(adapter);

        // 5. Update Stats (For a professional look, we show the total count)
        // You can actually pull the real total count if you want:
        // int total = db.getGlobalActionCount();
        tvCommunityStats.setText("Together we've completed " + activities.size() + " actions today!");

        btnBack.setOnClickListener(v -> finish());
    }

    // --- INTERNAL ADAPTER CLASS ---
    private static class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
        private final List<String> mData;

        CommunityAdapter(List<String> data) {
            this.mData = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using the built-in simple list item for clean text display
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(mData.get(position));
            holder.textView.setTextColor(0xFF2D5A27); // Matching your dark green theme
            holder.textView.setTextSize(16);
            holder.textView.setPadding(30, 30, 30, 30);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(android.R.id.text1);
            }
        }
    }
}