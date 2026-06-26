package com.example.ecoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNewName;
    private Button btnSave;
    private TextView tvCancel;
    private DatabaseHelper db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Initialize
        etNewName = findViewById(R.id.etEditName);
        btnSave = findViewById(R.id.btnSaveName);
        tvCancel = findViewById(R.id.tvCancelEdit);
        db = new DatabaseHelper(this);

        // 2. Get the current email passed from ProfileActivity
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // 3. Pre-fill the current name from database
        if (userEmail != null) {
            String currentName = db.getUserName(userEmail);
            etNewName.setText(currentName);
            // Move cursor to end of name
            etNewName.setSelection(etNewName.getText().length());
        }

        // 4. Save Logic
        btnSave.setOnClickListener(v -> {
            String updatedName = etNewName.getText().toString().trim();

            if (!updatedName.isEmpty()) {
                // Calls the updateUserName method in your DatabaseHelper
                boolean isUpdated = db.updateUserName(userEmail, updatedName);

                if (isUpdated) {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Close and return to ProfileActivity
                } else {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                etNewName.setError("Name cannot be empty");
            }
        });

        // 5. Cancel Logic
        tvCancel.setOnClickListener(v -> finish());
    }
}