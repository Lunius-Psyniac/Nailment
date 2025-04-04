package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput, nameInput;
    private EditText locationInput, profilePictureLinkInput;
    private EditText selfDescriptionInput;
    private LinearLayout manicuristFields;
    private RadioGroup userTypeGroup;
    private Button loginButton, registerButton;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nameInput = findViewById(R.id.nameInput);
        locationInput = findViewById(R.id.locationInput);
        profilePictureLinkInput = findViewById(R.id.profilePictureLinkInput);
        selfDescriptionInput = findViewById(R.id.selfDescriptionInput);
        manicuristFields = findViewById(R.id.manicuristFields);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

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
        profilePictureLinkInput.setVisibility(visibility);
        selfDescriptionInput.setVisibility(visibility);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthActivity", "Login successful");
                        navigateToMain();
                    } else {
                        Log.e("AuthActivity", "Login failed: " + task.getException().getMessage());
                        Toast.makeText(AuthActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String profilePictureLink = profilePictureLinkInput.getText().toString().trim();
        String selfDescription = selfDescriptionInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() ||
                profilePictureLink.isEmpty() || selfDescription.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
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

        Log.d("AuthActivity", "Starting registration process for email: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthActivity", "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
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
                                        Log.d("AuthActivity", "User created in database successfully");
                                        Toast.makeText(AuthActivity.this,
                                                "Registration successful!",
                                                Toast.LENGTH_SHORT).show();
                                        navigateToMain();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("AuthActivity", "Database Error: " + e.getMessage());
                                        Toast.makeText(AuthActivity.this,
                                                "Account created but profile setup incomplete. Please try again later.",
                                                Toast.LENGTH_LONG).show();
                                        navigateToMain();
                                    });
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.e("AuthActivity", "Registration failed: " +
                                (exception != null ? exception.getMessage() : "Unknown error"));
                        Toast.makeText(AuthActivity.this,
                                "Registration failed: " +
                                        (exception != null ? exception.getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
