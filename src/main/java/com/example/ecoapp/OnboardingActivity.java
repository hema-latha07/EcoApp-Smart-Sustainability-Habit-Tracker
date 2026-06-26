package com.example.ecoapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etUserName, etUserEmail;
    private RadioGroup rgArchetype;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // 1. Initialize views
        etUserName = findViewById(R.id.etUserName);
        etUserEmail = findViewById(R.id.etUserEmail);
        rgArchetype = findViewById(R.id.rgArchetype);
        btnContinue = findViewById(R.id.btnContinue);

        // 2. Button Logic
        btnContinue.setOnClickListener(v -> {
            String name = etUserName.getText().toString().trim();
            String email = etUserEmail.getText().toString().trim();
            int selectedId = rgArchetype.getCheckedRadioButtonId();

            // Validation: Ensure nothing is empty
            if (name.isEmpty()) {
                etUserName.setError("Name is required");
                return;
            }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etUserEmail.setError("Enter a valid email");
                return;
            }
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an Eco-Archetype!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the text of the selected RadioButton (Goal)
            RadioButton rb = findViewById(selectedId);
            String goal = rb.getText().toString();

            // Request Permissions (Modern practice)
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
            }, 1);

            // 3. PASS DATA TO DASHBOARD
            // We use 'Intent' to send the user's specific info to the next screen
            Intent intent = new Intent(OnboardingActivity.this, DashboardActivity.class);

            intent.putExtra("USER_NAME", name);
            intent.putExtra("USER_EMAIL", email);
            intent.putExtra("USER_GOAL", goal);
            intent.putExtra("IS_NEW_USER", true); // Tells Dashboard to show 0 points or bonus

            Toast.makeText(this, "Welcome to the green side, " + name + "!", Toast.LENGTH_LONG).show();

            startActivity(intent);
            finish(); // This destroys Onboarding so the user can't "Go Back" to it
        });
    }
}