package com.example.nailment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingChangeListener {

    private RecyclerView settingsRecyclerView;
    private List<SettingOption> settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Back button to go to the previous activity
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // RecyclerView setup
        settingsRecyclerView = findViewById(R.id.settingsRecyclerView);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample settings data
        settingsList = createSettingsList();

        // Adapter setup
        SettingsAdapter adapter = new SettingsAdapter(settingsList, this);
        settingsRecyclerView.setAdapter(adapter);

        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private List<SettingOption> createSettingsList() {
        List<SettingOption> list = new ArrayList<>();
        list.add(new SettingOption("Privacy Policy", SettingOption.Type.PRIVACY_POLICY));
        list.add(new SettingOption("Notifications", SettingOption.Type.NOTIFICATIONS));
        list.add(new SettingOption("Dark Mode", SettingOption.Type.APPEARANCE));
        list.add(new SettingOption("Help", SettingOption.Type.HELP));
        return list;
    }

    @Override
    public boolean isNotificationsEnabled() {
        return getSharedPreferences("app_settings", MODE_PRIVATE)
                .getBoolean("notifications_enabled", true); // Default is true
    }

    @Override
    public void toggleNotifications(boolean enable) {
        getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit()
                .putBoolean("notifications_enabled", enable)
                .apply();
    }

    @Override
    public boolean isDarkModeEnabled() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void toggleDarkMode(boolean enable) {
        AppCompatDelegate.setDefaultNightMode(enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void handleSettingClick(SettingOption setting) {
        switch (setting.getType()) {
            case PRIVACY_POLICY:
                // Show privacy policy popup
                showPrivacyPolicy();
                break;
            case HELP:
                // Open support website
                openHelpWebsite();
                break;
        }
    }

    private void showPrivacyPolicy() {
        // Logic to show a popup with privacy policy
    }

    private void openHelpWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(browserIntent);
    }
}
