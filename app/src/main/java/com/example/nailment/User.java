package com.example.nailment;

public class User {
    private String uid;
    private String email;
    private String displayName;

    // Required empty constructor for Firebase
    public User() {}

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.displayName = email.split("@")[0]; // Use part before @ as display name
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
} 