package com.example.rateyourplace;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Property {
    private String address;
    private List<String> imageUris;

    //Firebase wants an empty constructor
    public Property() {
    }

    //Constructor
    public Property(String address, List<String> imageUris) {
        this.address = address;
        this.imageUris = imageUris;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getImageUris() {
        return imageUris;
    }

    public interface ReviewFetchCallback {
        void onReviewsFetched(Map<String, Float> averages, int reviewCount);
    }

    // Gets total average rating
    public float getAverageRating(Map<String, Float> averages) {
        if (averages.isEmpty()) return 0f; // Return 0 if no reviews

        float total = 0f;
        for (float rating : averages.values()) {
            total += rating;
        }
        return total / averages.size(); // Divide by number of rating categories
    }

    //From reviews gets average for each category
    public void fetchAverageRatings(FirebaseFirestore db, ReviewFetchCallback callback) {
        db.collection("reviews")
                .whereEqualTo("propertyId", address) // Assuming `address` is used as an ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float totalLandlord = 0, totalConditions = 0, totalSafety = 0, totalLocation = 0;
                    int reviewCount = queryDocumentSnapshots.size();

                    if (reviewCount > 0) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Review review = document.toObject(Review.class);
                            totalLandlord += review.getRatingLandlord();
                            totalConditions += review.getRatingConditions();
                            totalSafety += review.getRatingSafety();
                            totalLocation += review.getRatingLocation();
                        }

                        // Calculate the averages
                        Map<String, Float> averages = new HashMap<>();
                        averages.put("landlord", totalLandlord / reviewCount);
                        averages.put("property_condition", totalConditions / reviewCount);
                        averages.put("safety", totalSafety / reviewCount);
                        averages.put("location", totalLocation / reviewCount);

                        callback.onReviewsFetched(averages, reviewCount);
                    } else {
                        callback.onReviewsFetched(new HashMap<>(), 0);
                    }
                })
                .addOnFailureListener(e -> callback.onReviewsFetched(new HashMap<>(), 0));
    }

    // Gets the first Image
    public String getFirstImageUrl() {
        List<String> imageUrls = getImageUris();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                Log.d("ImageUrls", "Image URL: " + imageUrl);
            }
            return imageUrls.get(0);
        } else {
            Log.d("ImageUrls", "No images found.");
            return null;
        }
    }

}
