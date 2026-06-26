package com.example.ecoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "EcoHero.db";
    // Version 16: Adds Admin Alert table
    public static final int DATABASE_VERSION = 16;

    // Table: Users
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_EMAIL = "EMAIL";
    public static final String COL_PASSWORD = "PASSWORD";
    public static final String COL_POINTS = "POINTS";
    public static final String COL_GOAL = "PRIMARY_GOAL";
    public static final String COL_LAST_QUIZ = "LAST_QUIZ_DATE";
    public static final String COL_DAILY_DIST = "DAILY_DISTANCE";
    public static final String COL_TRACK_DATE = "LAST_TRACK_DATE";
    public static final String COL_CERT_DOWNLOADED = "CERT_DOWNLOADED";

    // Table: Activity History
    public static final String TABLE_HISTORY = "activity_history";
    public static final String COL_HIST_ID = "H_ID";
    public static final String COL_HIST_EMAIL = "H_EMAIL";
    public static final String COL_HIST_ACTIVITY = "ACTIVITY_DONE";
    public static final String COL_HIST_POINTS = "POINTS_GAINED";
    public static final String COL_HIST_DATE = "DATE_TIME";

    // NEW Table: Admin Alerts
    public static final String TABLE_ALERTS = "admin_alerts";
    public static final String COL_ALERT_ID = "A_ID";
    public static final String COL_ALERT_MSG = "MESSAGE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_POINTS + " INTEGER DEFAULT 0, " +
                COL_GOAL + " TEXT, " +
                COL_LAST_QUIZ + " TEXT, " +
                COL_DAILY_DIST + " REAL DEFAULT 0.0, " +
                COL_TRACK_DATE + " TEXT, " +
                COL_CERT_DOWNLOADED + " INTEGER DEFAULT 0)";

        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HIST_EMAIL + " TEXT, " +
                COL_HIST_ACTIVITY + " TEXT, " +
                COL_HIST_POINTS + " INTEGER, " +
                COL_HIST_DATE + " TEXT)";

        String createAlertsTable = "CREATE TABLE " + TABLE_ALERTS + " (" +
                COL_ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ALERT_MSG + " TEXT)";

        db.execSQL(createUsersTable);
        db.execSQL(createHistoryTable);
        db.execSQL(createAlertsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        onCreate(db);
    }

    // --- NEW ADMIN ALERT METHODS ---

    public void updateCampusAlert(String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Clear previous alerts so only the latest broadcast is shown
        db.execSQL("DELETE FROM " + TABLE_ALERTS);
        ContentValues values = new ContentValues();
        values.put(COL_ALERT_MSG, message);
        db.insert(TABLE_ALERTS, null, values);
    }

    public String getLatestAlert() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_ALERT_MSG + " FROM " + TABLE_ALERTS + " ORDER BY " + COL_ALERT_ID + " DESC LIMIT 1", null);
        String alert = "Welcome to EcoHero!";
        if (cursor != null && cursor.moveToFirst()) {
            alert = cursor.getString(0);
            cursor.close();
        }
        return alert;
    }

    // --- ADMIN & CERTIFICATE TRACKING METHODS ---

    public boolean hasDownloadedCertificate(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_CERT_DOWNLOADED}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        boolean downloaded = false;
        if (cursor != null && cursor.moveToFirst()) {
            downloaded = cursor.getInt(0) == 1;
            cursor.close();
        }
        return downloaded;
    }

    public void markCertificateAsDownloaded(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CERT_DOWNLOADED, 1);
        db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
    }

    public int getTotalUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getGlobalEcoPoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_POINTS + ") FROM " + TABLE_USERS, null);
        int total = 0;
        if (cursor.moveToFirst()) total = cursor.getInt(0);
        cursor.close();
        return total;
    }

    public int getCertificatesIssuedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_CERT_DOWNLOADED + " = 1", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public Cursor getAllUsersForAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_NAME + ", " + COL_EMAIL + ", " + COL_POINTS +
                " FROM " + TABLE_USERS + " ORDER BY " + COL_POINTS + " DESC", null);
    }

    public void resetAllUserPoints() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POINTS, 0);
        values.put(COL_DAILY_DIST, 0.0);
        db.update(TABLE_USERS, values, null, null);
    }

    // --- TRACKING PERSISTENCE METHODS ---

    public void saveDailyProgress(String email, float distance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        values.put(COL_DAILY_DIST, distance);
        values.put(COL_TRACK_DATE, today);
        db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
    }

    public float getDailyDistance(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_DAILY_DIST}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);

        float distance = 0.0f;
        if (cursor != null && cursor.moveToFirst()) {
            distance = cursor.getFloat(0);
            cursor.close();
        }
        return distance;
    }

    public String getLastTrackingDate(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_TRACK_DATE}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);

        String date = "";
        if (cursor != null && cursor.moveToFirst()) {
            date = cursor.getString(0);
            cursor.close();
        }
        return date;
    }

    // --- DASHBOARD METHODS ---

    public int getTotalActionCount(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORY + " WHERE " + COL_HIST_EMAIL + "=?", new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        if (cursor != null) cursor.close();
        return count;
    }

    public String getUserGoal(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String goal = "Stay Green";
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_GOAL}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            goal = cursor.getString(0);
        }
        if (cursor != null) cursor.close();
        return goal;
    }

    // --- DAILY QUIZ LOGIC ---

    public boolean canUserPlayQuiz(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_LAST_QUIZ},
                COL_EMAIL + "=? AND " + COL_LAST_QUIZ + "=?",
                new String[]{email, today}, null, null, null);

        boolean alreadyPlayed = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return !alreadyPlayed;
    }

    public void markQuizDone(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put(COL_LAST_QUIZ, today);
        db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
    }

    // --- USER PROFILE METHODS ---

    public boolean updateUserName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, newName);
        return db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email}) > 0;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = "Eco Hero";
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_NAME}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        if (cursor != null) cursor.close();
        return name;
    }

    // --- COMMUNITY FEED ---

    public List<String> getCommunityFeed() {
        List<String> feed = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COL_NAME + ", h." + COL_HIST_ACTIVITY + ", h." + COL_HIST_POINTS +
                " FROM " + TABLE_USERS + " u INNER JOIN " + TABLE_HISTORY + " h " +
                " ON u." + COL_EMAIL + " = h." + COL_HIST_EMAIL +
                " ORDER BY h." + COL_HIST_ID + " DESC LIMIT 50";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                feed.add(cursor.getString(0) + " " + cursor.getString(1) + " (+" + cursor.getInt(2) + " pts)");
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        return feed;
    }

    // --- POINTS & HISTORY ---

    public int getUserPoints(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int points = 0;
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_POINTS}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        if (cursor != null) cursor.close();
        return points;
    }

    public boolean addEcoPoints(String email, int pointsToAdd, String actionDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int currentPoints = getUserPoints(email);
            ContentValues userValues = new ContentValues();
            userValues.put(COL_POINTS, currentPoints + pointsToAdd);
            int updateResult = db.update(TABLE_USERS, userValues, COL_EMAIL + "=?", new String[]{email});

            if (updateResult > 0) {
                ContentValues historyValues = new ContentValues();
                historyValues.put(COL_HIST_EMAIL, email);
                historyValues.put(COL_HIST_ACTIVITY, actionDescription);
                historyValues.put(COL_HIST_POINTS, pointsToAdd);
                historyValues.put(COL_HIST_DATE, new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
                db.insert(TABLE_HISTORY, null, historyValues);
                return true;
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error adding points: " + e.getMessage());
        }
        return false;
    }

    // --- SECURITY & UTILS ---

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public boolean insertUser(String name, String email, String password, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PASSWORD, hashPassword(password));
        contentValues.put(COL_POINTS, 0);
        contentValues.put(COL_GOAL, goal);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashPassword(password)}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, hashPassword(newPassword));
        return db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email}) > 0;
    }

    public Cursor getLeaderboard() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_NAME + ", " + COL_POINTS +
                " FROM " + TABLE_USERS + " ORDER BY " + COL_POINTS + " DESC", null);
    }

    public Cursor getUserActivityHistory(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + COL_HIST_EMAIL + "=? ORDER BY " + COL_HIST_ID + " DESC", new String[]{email});
    }
}