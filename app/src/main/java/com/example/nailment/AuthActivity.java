package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
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
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
            return;
        }

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
                registerUser(email, password);
            }
        });

        registerButton.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            loginButton.setText(isLoginMode ? "Login" : "Register");
            registerButton.setText(isLoginMode ? "Need an account? Register" : "Already have an account? Login");
        });
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

    private void registerUser(String email, String password) {
        Log.d("AuthActivity", "Starting registration process for email: " + email);
        
        // First, create the authentication user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthActivity", "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            
                            // Create user object
                            User user = new User(uid, email);
                            
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
                                    // If database creation fails, still allow login but notify user
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