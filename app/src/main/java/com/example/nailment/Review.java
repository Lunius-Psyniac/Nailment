package com.example.nailment;

import java.io.Serializable;

public class Review implements Serializable {
    private String reviewId;
    private String userId;
    private String manicuristId;
    private String userName;
    private double rating;
    private String comment;
    private long timestamp;

    // Empty constructor required for Firebase
    public Review() {
    }

    public Review(String reviewId, String userId, String manicuristId, String userName, double rating, String comment) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.manicuristId = manicuristId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getManicuristId() {
        return manicuristId;
    }

    public void setManicuristId(String manicuristId) {
        this.manicuristId = manicuristId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 