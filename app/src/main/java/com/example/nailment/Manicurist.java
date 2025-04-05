package com.example.nailment;

import java.io.Serializable;

public class Manicurist implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String location;
    private String selfDescription;
    private String profilePictureLink;
    private double avgRating;
    private int ratingCount;
    private boolean accountActive;
    private String userType;

    // Required empty constructor for Firebase
    public Manicurist() {}

    public Manicurist(String uid, String name, String email, String location, String selfDescription, 
                      String profilePictureLink, double avgRating, int ratingCount, 
                      boolean accountActive, String userType) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.location = location;
        this.selfDescription = selfDescription;
        this.profilePictureLink = profilePictureLink;
        this.avgRating = avgRating;
        this.ratingCount = ratingCount;
        this.accountActive = accountActive;
        this.userType = userType;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getSelfDescription() { return selfDescription; }
    public void setSelfDescription(String selfDescription) { this.selfDescription = selfDescription; }
    
    public String getProfilePictureLink() { return profilePictureLink; }
    public void setProfilePictureLink(String profilePictureLink) { this.profilePictureLink = profilePictureLink; }
    
    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    
    public boolean isAccountActive() { return accountActive; }
    public void setAccountActive(boolean accountActive) { this.accountActive = accountActive; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
