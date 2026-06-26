package com.example.ecoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword;
    private Button btnReset;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 1. Initialize Views
        etEmail = findViewById(R.id.etForgotEmail);
        etNewPassword = findViewById(R.id.etNewPassword); // Ensure this ID is in your XML
        btnReset = findViewById(R.id.btnResetPassword);
        db = new DatabaseHelper(this);

        // 2. Reset Logic
        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (email.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (newPass.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
            } else {
                // updatePassword inside DatabaseHelper hashes the password and updates the DB
                boolean isUpdated = db.updatePassword(email, newPass);

                if (isUpdated) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Go back to LoginActivity
                } else {
                    Toast.makeText(this, "Error: Email not found in our system.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}