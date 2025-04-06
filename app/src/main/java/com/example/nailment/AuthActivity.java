package com.example.nailment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private EditText emailInput, passwordInput, nameInput;
    private EditText locationInput;
    private EditText selfDescriptionInput;
    private LinearLayout manicuristFields;
    private RadioGroup userTypeGroup;
    private Button loginButton, registerButton;
    private Button uploadPictureButton;
    private ImageView profilePicturePreview;
    private Uri selectedImageUri;
    private boolean isLoginMode = true;
    private FirebaseDatabase mDatabase;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                profilePicturePreview.setImageURI(uri);
                profilePicturePreview.setVisibility(View.VISIBLE);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check if we need to reset the theme
        if (getIntent().getBooleanExtra("reset_theme", false)) {
            NailmentApplication.setDarkMode(false);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nameInput = findViewById(R.id.nameInput);
        locationInput = findViewById(R.id.locationInput);
        selfDescriptionInput = findViewById(R.id.selfDescriptionInput);
        manicuristFields = findViewById(R.id.manicuristFields);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        uploadPictureButton = findViewById(R.id.uploadPictureButton);
        profilePicturePreview = findViewById(R.id.profilePicturePreview);

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
            return;
        }

        // Set up user type selection listener
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.manicuristTypeRadio) {
                manicuristFields.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.userTypeRadio) {
                manicuristFields.setVisibility(View.GONE);
            }
        });

        // Set up profile picture selection
        uploadPictureButton.setOnClickListener(v -> pickImage.launch("image/*"));

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                registerUser();
            }
        });

        registerButton.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            loginButton.setText(isLoginMode ? "Login" : "Register");
            registerButton.setText(isLoginMode ? "Need an account? Register" : "Already have an account? Login");

            toggleRegistrationFields(!isLoginMode);
        });

        toggleRegistrationFields(false); // Start in login mode by default
    }

    private void toggleRegistrationFields(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        nameInput.setVisibility(visibility);
        userTypeGroup.setVisibility(visibility);
        manicuristFields.setVisibility(View.GONE); // only show if "Manicurist" selected
        selfDescriptionInput.setVisibility(visibility);
        uploadPictureButton.setVisibility(visibility);
        profilePicturePreview.setVisibility(visibility);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Check if account is active
                    String userId = authResult.getUser().getUid();
                    mDatabase.getReference("users").child(userId).get()
                            .addOnSuccessListener(dataSnapshot -> {
                                if (dataSnapshot.exists()) {
                                    Boolean isActive = dataSnapshot.child("accountActive").getValue(Boolean.class);
                                    if (isActive != null && !isActive) {
                                        // Account is deactivated
                                        mAuth.signOut();
                                        Toast.makeText(AuthActivity.this, 
                                            "This account has been deactivated", 
                                            Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                                // Account is active, proceed with login
                                Toast.makeText(AuthActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AuthActivity.this, 
                                    "Error checking account status: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AuthActivity.this, "Login failed: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String selfDescription = selfDescriptionInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || selfDescription.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(AuthActivity.this, "Please select a profile picture", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedType = userTypeGroup.getCheckedRadioButtonId();
        if (selectedType == -1) {
            Toast.makeText(AuthActivity.this, "Please select a user type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedType == R.id.manicuristTypeRadio) {
            String location = locationInput.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Please fill in your location", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d(TAG, "Starting registration process for email: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            uploadProfilePicture(firebaseUser.getUid(), email, name, selectedType, selfDescription);
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Registration failed: " + 
                                (exception != null ? exception.getMessage() : "Unknown error"));
                        Toast.makeText(AuthActivity.this,
                                "Registration failed: " + 
                                        (exception != null ? exception.getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfilePicture(String uid, String email, String name, int selectedType, String selfDescription) {
        String fileName = "Profile Pictures/" + uid + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference profilePictureRef = storage.getReference().child(fileName);

        profilePictureRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profilePictureRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String profilePictureLink = uri.toString();
                                createUserInDatabase(uid, email, name, selectedType, selfDescription, profilePictureLink);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL: " + e.getMessage());
                                Toast.makeText(AuthActivity.this,
                                        "Error uploading profile picture. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading image: " + e.getMessage());
                    Toast.makeText(AuthActivity.this,
                            "Error uploading profile picture. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createUserInDatabase(String uid, String email, String name, int selectedType, 
                                    String selfDescription, String profilePictureLink) {
        String userType = selectedType == R.id.manicuristTypeRadio ? "MANICURIST" : "USER";
        User user = new User(uid, email, name, userType);
        user.setProfilePictureLink(profilePictureLink);
        user.setSelfDescription(selfDescription);

        if (userType.equals("MANICURIST")) {
            user.setLocation(locationInput.getText().toString().trim());
            user.setAvgRating(0.0);
        }

        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(uid)
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created in database successfully");
                    Toast.makeText(AuthActivity.this,
                            "Registration successful!",
                            Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Database Error: " + e.getMessage());
                    Toast.makeText(AuthActivity.this,
                            "Account created but profile setup incomplete. Please try again later.",
                            Toast.LENGTH_LONG).show();
                    navigateToMain();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
