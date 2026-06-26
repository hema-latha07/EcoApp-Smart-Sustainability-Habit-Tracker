package com.example.ecoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EcoQuizActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String userEmail;
    private TextView tvQuestion, tvTitle, tvProgress;
    private Button btnOption1, btnOption2;
    private ImageButton btnBack;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int totalScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_quiz);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // 1. Check if already played today
        if (!db.canUserPlayQuiz(userEmail)) {
            showSessionOverDialog();
            return;
        }

        // 2. Initialize UI
        tvTitle = findViewById(R.id.tvQuizTitle);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvQuizProgress); // Add a TextView in XML for "1/5"
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnBack = findViewById(R.id.btnBackQuiz);

        btnBack.setOnClickListener(v -> finish());

        // 3. Prepare 5 random questions
        prepareQuestions();
        displayQuestion();
    }

    private void prepareQuestions() {
        questionList = new ArrayList<>();
        questionList.add(new Question("You finished a plastic bottle. What next?", "Regular bin", "Recycle it", 2, "Recycled Plastic"));
        questionList.add(new Question("Market is 2km away. How to go?", "Petrol scooter", "Bicycle", 2, "Green Transport"));
        questionList.add(new Question("Old newspapers piling up?", "Scrap dealer", "Burn them", 1, "Recycled Paper"));
        questionList.add(new Question("Best spot for a sapling?", "Shady corner", "Well-lit soil", 2, "Planted Sapling"));
        questionList.add(new Question("Aluminum soda can. You should:", "Recycle it", "Toss on road", 1, "Recycled Metal"));
        questionList.add(new Question("Shopping time. Which bag?", "New plastic bag", "Reusable cloth bag", 2, "Eco Bag"));
        questionList.add(new Question("Need a light bulb?", "Incandescent", "LED bulb", 2, "Switched to LED"));

        Collections.shuffle(questionList); // Mix them up
        questionList = questionList.subList(0, 5); // Take only 5
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);
            tvQuestion.setText(q.getQuestionText());
            btnOption1.setText(q.getOption1());
            btnOption2.setText(q.getOption2());

            if (tvProgress != null) {
                tvProgress.setText((currentQuestionIndex + 1) + " / 5");
            }

            btnOption1.setOnClickListener(v -> handleAnswer(1));
            btnOption2.setOnClickListener(v -> handleAnswer(2));
        } else {
            finishQuiz();
        }
    }

    private void handleAnswer(int selected) {
        Question q = questionList.get(currentQuestionIndex);
        if (selected == q.getCorrectOption()) {
            totalScore += 10; // 10 points per correct answer
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
        }
        currentQuestionIndex++;
        displayQuestion();
    }

    private void finishQuiz() {
        db.addEcoPoints(userEmail, totalScore, "Completed Daily Quiz Session");
        db.markQuizDone(userEmail);

        new AlertDialog.Builder(this)
                .setTitle("Session Complete!")
                .setMessage("You earned " + totalScore + " points! Journey over for today. See you tomorrow!")
                .setCancelable(false)
                .setPositiveButton("Finish", (dialog, which) -> finish())
                .show();
    }

    private void showSessionOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Journey Over")
                .setMessage("Today's session is already over. Wait till tomorrow to start your journey!")
                .setCancelable(false)
                .setPositiveButton("Back", (dialog, which) -> finish())
                .show();
    }

    // Inner class to hold question data
    private static class Question {
        private String text, op1, op2, desc;
        private int correct;

        public Question(String text, String op1, String op2, int correct, String desc) {
            this.text = text; this.op1 = op1; this.op2 = op2; this.correct = correct; this.desc = desc;
        }
        public String getQuestionText() { return text; }
        public String getOption1() { return op1; }
        public String getOption2() { return op2; }
        public int getCorrectOption() { return correct; }
    }
}