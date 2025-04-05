package com.example.nailment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import android.Manifest;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingChangeListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_REQUEST = 1002;
    private static final int GALLERY_PERMISSION_REQUEST = 1003;
    
    private RecyclerView settingsRecyclerView;
    private List<SettingOption> settingsList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
        list.add(new SettingOption("Location Permission", SettingOption.Type.LOCATION_PERMISSION));
        list.add(new SettingOption("Camera Permission", SettingOption.Type.CAMERA_PERMISSION));
        list.add(new SettingOption("Gallery Permission", SettingOption.Type.GALLERY_PERMISSION));
        list.add(new SettingOption("Help", SettingOption.Type.HELP));
        list.add(new SettingOption("Deactivate Account", SettingOption.Type.DEACTIVATE_ACCOUNT));
        return list;
    }

    @Override
    public boolean isNotificationsEnabled() {
        return getSharedPreferences("app_settings", MODE_PRIVATE)
                .getBoolean("notifications_enabled", true);
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
                showPrivacyPolicy();
                break;
            case HELP:
                openHelpWebsite();
                break;
            case LOCATION_PERMISSION:
                showPermissionDialog(Manifest.permission.ACCESS_FINE_LOCATION, 
                    "Location access is needed to find nearby manicurists", 
                    LOCATION_PERMISSION_REQUEST);
                break;
            case CAMERA_PERMISSION:
                showPermissionDialog(Manifest.permission.CAMERA, 
                    "Camera access is needed for AR nail try-on", 
                    CAMERA_PERMISSION_REQUEST);
                break;
            case GALLERY_PERMISSION:
                showPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE, 
                    "Gallery access is needed to save nail designs", 
                    GALLERY_PERMISSION_REQUEST);
                break;
            case DEACTIVATE_ACCOUNT:
                showDeactivateAccountDialog();
                break;
        }
    }

    private void showDeactivateAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deactivate Account")
               .setMessage("Are you sure you want to deactivate your account? This action cannot be undone.")
               .setPositiveButton("Deactivate", (dialog, which) -> {
                   deactivateAccount();
               })
               .setNegativeButton("Cancel", (dialog, which) -> {
                   dialog.dismiss();
               });
        builder.create().show();
    }

    private void deactivateAccount() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("accountActive").setValue(false)
                .addOnSuccessListener(aVoid -> {
                    // Sign out the user
                    mAuth.signOut();
                    // Show success message
                    Toast.makeText(this, "Account deactivated successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to login screen
                    Intent intent = new Intent(this, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to deactivate account: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void showPermissionDialog(String permission, String message, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required")
               .setMessage(message)
               .setPositiveButton("Allow", (dialog, which) -> {
                   ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
               })
               .setNegativeButton("Deny", (dialog, which) -> {
                   dialog.dismiss();
               })
               .setNeutralButton("Settings", (dialog, which) -> {
                   Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                   Uri uri = Uri.fromParts("package", getPackageName(), null);
                   intent.setData(uri);
                   startActivity(intent);
               });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            String message = "Permission granted successfully";
            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST:
                    message = "Location permission granted";
                    break;
                case CAMERA_PERMISSION_REQUEST:
                    message = "Camera permission granted";
                    break;
                case GALLERY_PERMISSION_REQUEST:
                    message = "Gallery permission granted";
                    break;
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            // Permission denied
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPrivacyPolicy() {
        String privacyPolicyText = "Privacy Policy\n\n" +
                "This is a sample privacy policy. It explains how we collect, use, and protect your data. " +
                "Your privacy is important to us, and we are committed to safeguarding your information.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy");
        builder.setMessage(privacyPolicyText);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void openHelpWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(browserIntent);
    }
}
