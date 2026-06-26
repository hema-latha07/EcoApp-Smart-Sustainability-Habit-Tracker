package com.example.ecoapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfilePoints, tvProfileRank;
    private Button btnDownloadCert;
    private DatabaseHelper db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePoints = findViewById(R.id.tvProfilePoints);
        tvProfileRank = findViewById(R.id.tvProfileRank);
        btnDownloadCert = findViewById(R.id.btnDownloadCertProfile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnEdit = findViewById(R.id.btnEditProfile);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        btnDownloadCert.setOnClickListener(v -> generateCertificate());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userEmail != null) {
            String name = db.getUserName(userEmail);
            int points = db.getUserPoints(userEmail);

            tvProfileName.setText(name);
            tvProfileEmail.setText(userEmail);
            tvProfilePoints.setText(points + " pts");

            // Dynamic Rank Logic
            if (points < 50) {
                tvProfileRank.setText("Eco Seed 🌱");
            } else if (points < 100) {
                tvProfileRank.setText("Eco Sprout 🌿");
            } else {
                tvProfileRank.setText("Eco Hero 🌳");
            }

            // Milestone: Enable download only after reaching 100 points
            if (points >= 100) {
                btnDownloadCert.setVisibility(View.VISIBLE);
                btnDownloadCert.setEnabled(true);
            } else {
                btnDownloadCert.setVisibility(View.GONE);
            }
        }
    }

    private void generateCertificate() {
        String name = db.getUserName(userEmail);
        PdfDocument document = new PdfDocument();

        // A4 Paper Size (595 x 842 points)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. ECO-THEMED BORDER
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);
        paint.setColor(Color.parseColor("#2D5A27")); // Forest Green
        canvas.drawRect(30, 30, 565, 812, paint);

        paint.setStrokeWidth(4);
        paint.setColor(Color.parseColor("#A5D6A7")); // Leaf Green
        canvas.drawRect(45, 45, 550, 797, paint);

        // 2. NATURE DECORATIONS (Trees/Sprouts)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawCircle(80, 750, 35, paint); // Decorative Tree Top
        paint.setColor(Color.parseColor("#795548")); // Wood Brown
        canvas.drawRect(75, 750, 85, 785, paint); // Trunk

        // 3. STRUCTURED TEXT CONTENT
        paint.setTextAlign(Paint.Align.CENTER);

        // Header
        paint.setColor(Color.parseColor("#1B3A1E"));
        paint.setTextSize(32);
        paint.setFakeBoldText(true);
        canvas.drawText("CERTIFICATE OF ACHIEVEMENT", 297, 160, paint);

        // Sub-header
        paint.setTextSize(18);
        paint.setFakeBoldText(false);
        canvas.drawText("This eco-honor is proudly presented to", 297, 230, paint);

        // User Name (Prominent)
        paint.setTextSize(42);
        paint.setColor(Color.parseColor("#2D5A27"));
        paint.setFakeBoldText(true);
        canvas.drawText(name.toUpperCase(), 297, 310, paint);

        // Description
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        canvas.drawText("For reaching the 100 Eco-Points milestone and contributing", 297, 390, paint);
        canvas.drawText("to a sustainable future through green actions.", 297, 415, paint);

        // Badge
        paint.setColor(Color.parseColor("#E8F5E9"));
        canvas.drawRoundRect(180, 470, 415, 540, 25, 25, paint);
        paint.setColor(Color.parseColor("#2D5A27"));
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("OFFICIAL ECO HERO 🌳", 297, 515, paint);

        // Institutional Footer
        paint.setTextSize(14);
        paint.setColor(Color.GRAY);
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("Awarded on: " + date, 297, 720, paint);
        canvas.drawText("EcoHero Project - Stella Maris College", 297, 745, paint);

        document.finishPage(page);

        // 4. FILE STORAGE LOGIC
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "EcoHero_Certificate_" + name.replace(" ", "_") + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Certificate Downloaded to Downloads!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("PDF_ERROR", "Error: " + e.getMessage());
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }

        document.close();
    }
}