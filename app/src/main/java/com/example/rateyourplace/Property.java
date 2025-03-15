package com.example.rateyourplace;

import android.util.Log;

import java.util.List;

public class Property {
    private String address;
    private List<String> imageUris;
    private int location;
    private int property_condition;
    private int safety;
    private int landlord;

    public Property() {
        // Required empty constructor for Firestore
    }

    public Property(String address, List<String> imageUris, int location, int property_condition, int safety, int landlord) {
        this.address = address;
        this.imageUris = imageUris;
        this.location = location;
        this.property_condition = property_condition;
        this.safety = safety;
        this.landlord = landlord;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public String getFirstImageUrl() {
        List<String> imageUrls = getImageUris(); // Assuming this method returns a list of image URLs

        // Log all the image URLs
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                Log.d("ImageUrls", "Image URL: " + imageUrl);
            }

            // Return the first image URL
            return imageUrls.get(0); // Return the first image URL
        } else {
            Log.d("ImageUrls", "No images found.");
            return null;
        }
    }

    public float getAverageRating() {
        return (location + property_condition + safety + landlord) / 4.0f;
    }
}
