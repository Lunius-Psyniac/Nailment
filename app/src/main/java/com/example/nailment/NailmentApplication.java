package com.example.nailment;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class NailmentApplication extends Application {
    private static final String PREF_NAME = "appPreferences";
    private static final String KEY_DARK_MODE = "darkMode";
    private static NailmentApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Configure Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);  // Enable offline persistence
        
        // Apply theme based on saved preference
        applyTheme();
    }
    
    public static void setDarkMode(boolean isDarkMode) {
        if (instance == null) {
            // If instance is not set yet, just apply the theme directly
            AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            return;
        }
        
        SharedPreferences prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
        
        // Apply the theme
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
    
    public static boolean isDarkMode() {
        if (instance == null) {
            // If instance is not set yet, return default value (light mode)
            return false;
        }
        
        SharedPreferences prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
    
    private void applyTheme() {
        boolean isDarkMode = isDarkMode();
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
    
    public static NailmentApplication getInstance() {
        return instance;
    }
} 