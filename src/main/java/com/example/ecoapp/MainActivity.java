package com.example.ecoapp;

import android.content.Intent; // Added this import
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize UI elements
        EditText etName = findViewById(R.id.etName);
        SeekBar footprintSlider = findViewById(R.id.footprintSlider);
        Spinner goalSpinner = findViewById(R.id.goalSpinner);
        Button btnSave = findViewById(R.id.btnSave);

        // 2. Setup the Dropdown (Spinner)
        String[] goals = {"The Commuter (Travel)", "The Foodie (Diet)", "The Minimalist (Waste)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        // 3. Handle the "Start My Journey" click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                int ecoScore = footprintSlider.getProgress();
                String goal = goalSpinner.getSelectedItem().toString();

                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                } else {
                    // Show a quick success message
                    String msg = "Welcome " + name + "! Starting your journey...";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                    // 4. Move to the DashboardActivity
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

                    // Pass the data to the next screen so the Dashboard can say "Hello [Name]"
                    intent.putExtra("USER_NAME", name);
                    intent.putExtra("USER_GOAL", goal);
                    intent.putExtra("USER_SCORE", ecoScore);

                    startActivity(intent);

                    // Optional: Close MainActivity so user doesn't go back to onboarding
                    finish();
                }
            }
        });
    }
}