package com.example.nailment;

public class User {
    private String uid;
    private String email;
    private String name;
    private String userType; // "MANICURIST" or "USER"
    private int ratingCount;
    private boolean isAccountActive;
    
    // Common fields for both types
    private String profilePictureLink;
    private String selfDescription;
    
    // Manicurist specific fields
    private String location;
    private double avgRating;

    // Required empty constructor for Firebase
    public User() {}

    public User(String uid, String email, String name, String userType) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.ratingCount = 0;
        this.isAccountActive = true; // Set default value to true
    }

    // Getters and setters for common fields
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public boolean isAccountActive() { return isAccountActive; }
    public void setAccountActive(boolean accountActive) { isAccountActive = accountActive; }
    public String getProfilePictureLink() { return profilePictureLink; }
    public void setProfilePictureLink(String profilePictureLink) { this.profilePictureLink = profilePictureLink; }
    public String getSelfDescription() { return selfDescription; }
    public void setSelfDescription(String selfDescription) { this.selfDescription = selfDescription; }

    // Getters and setters for Manicurist fields
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
} 