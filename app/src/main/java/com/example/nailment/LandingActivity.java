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
        setContentView(R.layout.activity_landing);

        // Always navigate to the Login screen when the app is first opened.
        navigateToLoginPage();
    }

    private void navigateToLoginPage() {
        // Navigate directly to LoginActivity
        Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity so the user cannot return to it.
    }
}
