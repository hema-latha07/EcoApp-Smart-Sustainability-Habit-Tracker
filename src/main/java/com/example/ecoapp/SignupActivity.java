package com.example.ecoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPass, etConfirmPass;
    private Spinner spinnerGoal;
    private Button btnSignup;
    private CheckBox cbShowPassword;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1. Initialize Views
        etName = findViewById(R.id.etSignupName);
        etEmail = findViewById(R.id.etSignupEmail);
        etPass = findViewById(R.id.etSignupPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        btnSignup = findViewById(R.id.btnSignup);
        cbShowPassword = findViewById(R.id.cbShowPassword);

        // 2. Initialize Database
        db = new DatabaseHelper(this);

        // 3. Setup Goal Spinner
        String[] ecoGoals = {
                "Reduce Plastic Waste",
                "Lower Carbon Footprint",
                "Conserve Water",
                "Plant More Trees",
                "Promote Renewable Energy"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ecoGoals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

        // 4. Password Visibility Toggle Logic
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPass.setSelection(etPass.getText().length());
            etConfirmPass.setSelection(etConfirmPass.getText().length());
        });

        // 5. Registration Logic
        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String confirm = etConfirmPass.getText().toString().trim();
            String selectedGoal = spinnerGoal.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                etConfirmPass.setError("Passwords do not match");
            } else if (pass.length() < 6) {
                etPass.setError("Password must be at least 6 characters");
            } else {
                // Now passing 4 parameters to match Version 11 DatabaseHelper
                boolean isInserted = db.insertUser(name, email, pass, selectedGoal);

                if (isInserted) {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard or Onboarding
                    Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_NAME", name);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed! Email might already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}