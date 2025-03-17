package com.example.rateyourplace;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Review {

    private String propertyId;
    private List<String> imageUris;
    private int ratingLandlord;
    private int ratingConditions;
    private int ratingSafety;
    private int ratingLocation;
    private String comment;
    private String userId;
    private String reviewId;

    public String getReviewId() {
        return reviewId;
    }
    public void setReviewId(String reviewId) {
        this.reviewId =reviewId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    private String userEmail;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }



    public int getRatingLocation() {
        return ratingLocation;
    }

    public void setRatingLocation(int ratingLocation) {
        this.ratingLocation = ratingLocation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRatingSafety() {
        return ratingSafety;
    }

    public void setRatingSafety(int ratingSafety) {
        this.ratingSafety = ratingSafety;
    }

    public int getRatingConditions() {
        return ratingConditions;
    }

    public void setRatingConditions(int ratingConditions) {
        this.ratingConditions = ratingConditions;
    }

    public int getRatingLandlord() {
        return ratingLandlord;
    }

    public void setRatingLandlord(int ratingLandlord) {
        this.ratingLandlord = ratingLandlord;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }



    public Review(){

    }

    public Review(String propertyId, List<String> imageUris, int ratingLandlord, int ratingConditions, int ratingSafety, int ratingLocation, String userId, String comments, String reviewId, String userEmail){
        this.propertyId = propertyId;
        this.imageUris = imageUris;
        this.ratingLandlord = ratingLandlord;
        this.ratingConditions = ratingConditions;
        this.ratingSafety = ratingSafety;
        this.ratingLocation = ratingLocation;
        this.userId = userId;
        this.comment = comments;
        this.reviewId = reviewId;
        this.userEmail = userEmail;
    }
    public float getAverageRating() {
        return (ratingLandlord + ratingConditions + ratingSafety + ratingLocation) / 4.0f;
    }

}
