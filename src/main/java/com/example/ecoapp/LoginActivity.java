package com.example.ecoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgot;
    private CheckBox cbShowPassword;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgot = findViewById(R.id.tvForgotPassword);
        cbShowPassword = findViewById(R.id.cbShowPasswordLogin);

        db = new DatabaseHelper(this);

        // 2. Password Visibility Toggle Logic
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show Password
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide Password
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Keep cursor at the end
            etPassword.setSelection(etPassword.getText().length());
        });

        // 3. Forgot Password Navigation
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // 4. Login Logic
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- ADMIN BYPASS LOGIC ---
                if (email.equals("admin@ecohero.com") && password.equals("admin123")) {
                    Toast.makeText(LoginActivity.this, "Admin Access Granted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                // --- STANDARD USER LOGIN ---
                if (db.checkUser(email, password)) {
                    String userName = db.getUserName(email);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 5. Navigate to Signup
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}