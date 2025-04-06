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
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.util.Log;

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

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

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
        Log.d("SettingsActivity", "Settings list size: " + settingsList.size());

        // Adapter setup
        SettingsAdapter adapter = new SettingsAdapter(settingsList, this);
        settingsRecyclerView.setAdapter(adapter);
        Log.d("SettingsActivity", "Adapter set with " + settingsList.size() + " items");

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        
        // Camera button to open CameraActivity
        findViewById(R.id.cameraButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CameraActivity.class);
            startActivity(intent);
        });
        
        // Chat button to navigate to ChatActivity
        findViewById(R.id.chatButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChatActivity.class);
            startActivity(intent);
        });
        
        // Profile button to navigate to UserProfileActivity
        findViewById(R.id.profileButton).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(SettingsActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(SettingsActivity.this, "You must be logged in to view your profile", Toast.LENGTH_SHORT).show();
                // Optionally navigate to login screen
                Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }

    private List<SettingOption> createSettingsList() {
        List<SettingOption> list = new ArrayList<>();
        list.add(new SettingOption("Privacy Policy", SettingOption.Type.PRIVACY_POLICY));
        list.add(new SettingOption("Dark Mode", SettingOption.Type.APPEARANCE));
        list.add(new SettingOption("Location Permission", SettingOption.Type.LOCATION_PERMISSION));
        list.add(new SettingOption("Camera Permission", SettingOption.Type.CAMERA_PERMISSION));
        list.add(new SettingOption("Gallery Permission", SettingOption.Type.GALLERY_PERMISSION));
        list.add(new SettingOption("Help", SettingOption.Type.HELP));
        list.add(new SettingOption("Deactivate Account", SettingOption.Type.DEACTIVATE_ACCOUNT));
        list.add(new SettingOption("Log out", SettingOption.Type.LOGOUT));
        
        // Log the settings list
        for (SettingOption option : list) {
            Log.d("SettingsActivity", "Setting: " + option.getTitle() + " - Type: " + option.getType());
        }
        
        return list;
    }

    @Override
    public boolean isDarkModeEnabled() {
        try {
            return NailmentApplication.isDarkMode();
        } catch (Exception e) {
            // If there's an error, default to light mode
            return false;
        }
    }

    @Override
    public void toggleDarkMode(boolean enable) {
        try {
            NailmentApplication.setDarkMode(enable);
        } catch (Exception e) {
            // If there's an error, just apply the theme directly
            AppCompatDelegate.setDefaultNightMode(
                enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    showPermissionChangeDialog("Location");
                }
                break;
            case CAMERA_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                        == PackageManager.PERMISSION_GRANTED) {
                    showPermissionChangeDialog("Camera");
                } else {
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.CAMERA}, 
                        CAMERA_PERMISSION_REQUEST);
                }
                break;
            case GALLERY_PERMISSION:
                String galleryPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU 
                    ? Manifest.permission.READ_MEDIA_IMAGES 
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
                if (ContextCompat.checkSelfPermission(this, galleryPermission) 
                        == PackageManager.PERMISSION_GRANTED) {
                    showPermissionChangeDialog("Gallery");
                } else {
                    ActivityCompat.requestPermissions(this, 
                        new String[]{galleryPermission}, 
                        GALLERY_PERMISSION_REQUEST);
                }
                break;
            case DEACTIVATE_ACCOUNT:
                showDeactivateAccountDialog();
                break;
            case LOGOUT:
                showLogoutDialog();
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
                    // Sign out the user first
                    mAuth.signOut();
                    
                    // Show success message
                    Toast.makeText(this, "Account deactivated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Create intent for navigation
                    Intent intent = new Intent(this, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    
                    // Set a flag to indicate we need to reset the theme
                    intent.putExtra("reset_theme", true);
                    
                    // Start the activity and finish this one
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to deactivate account: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
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

    private void showPermissionChangeDialog(String permissionType) {
        new AlertDialog.Builder(this)
            .setTitle(permissionType + " Permission")
            .setMessage("You have already granted " + permissionType.toLowerCase() + " permission. Would you like to change it in Settings?")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log out", (dialog, which) -> {
                // Sign out the user first
                mAuth.signOut();
                
                // Show success message
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                
                // Create intent for navigation
                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                
                // Set a flag to indicate we need to reset the theme
                intent.putExtra("reset_theme", true);
                
                // Start the activity and finish this one
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }
}
