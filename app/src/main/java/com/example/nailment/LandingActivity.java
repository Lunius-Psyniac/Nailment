package com.example.nailment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user has seen the landing page
        SharedPreferences preferences = getSharedPreferences("appPreferences", MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        if (!isFirstLaunch) {
            navigateToMainPage();
            return;
        }

        setContentView(R.layout.activity_landing);

        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark as not first launch and go to the main page
                preferences.edit().putBoolean("isFirstLaunch", false).apply();
                navigateToMainPage();
            }
        });
    }

    private void navigateToMainPage() {
        Intent intent = new Intent(LandingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
