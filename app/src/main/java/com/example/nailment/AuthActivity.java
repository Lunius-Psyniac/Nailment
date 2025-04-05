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
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

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
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
            
            // Show/hide registration fields
            nameInput.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
            userTypeGroup.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
            profilePictureLinkInput.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
            selfDescriptionInput.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
            manicuristFields.setVisibility(View.GONE);
        });

        // Set initial visibility
        nameInput.setVisibility(View.GONE);
        userTypeGroup.setVisibility(View.GONE);
        profilePictureLinkInput.setVisibility(View.GONE);
        selfDescriptionInput.setVisibility(View.GONE);
        manicuristFields.setVisibility(View.GONE);
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
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String name = nameInput.getText().toString();
        String profilePictureLink = profilePictureLinkInput.getText().toString();
        String selfDescription = selfDescriptionInput.getText().toString();

        // Validate common fields
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || 
            profilePictureLink.isEmpty() || selfDescription.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user type selection
        int selectedType = userTypeGroup.getCheckedRadioButtonId();
        if (selectedType == -1) {
            Toast.makeText(AuthActivity.this, "Please select a user type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate type-specific fields
        if (selectedType == R.id.manicuristTypeRadio) {
            String location = locationInput.getText().toString();
            if (location.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Please fill in your location", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d("AuthActivity", "Starting registration process for email: " + email);
        
        // Create the authentication user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthActivity", "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            String userType = selectedType == R.id.manicuristTypeRadio ? "MANICURIST" : "USER";
                            
                            // Create user object with common fields
                            User user = new User(uid, email, name, userType);
                            user.setProfilePictureLink(profilePictureLink);
                            user.setSelfDescription(selfDescription);
                            
                            // Add type-specific fields
                            if (userType.equals("MANICURIST")) {
                                user.setLocation(locationInput.getText().toString());
                                user.setAvgRating(0.0);
                            }
                            
                            // Get database reference
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            database.getReference()
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
                        } else {
                            Log.e("AuthActivity", "FirebaseUser is null after successful registration");
                            Toast.makeText(AuthActivity.this,
                                "Registration error: User creation failed",
                                Toast.LENGTH_SHORT).show();
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