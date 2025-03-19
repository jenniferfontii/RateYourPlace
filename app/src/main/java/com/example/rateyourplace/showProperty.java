package com.example.rateyourplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class showProperty extends AppCompatActivity {

    private TextView address;
    private RatingBar location, conditions, safety, landlord;
    private String propertyId;

    private ListView listView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    BottomNavigationView navBar;

    @Override
    protected void onResume() {
        super.onResume();
        navBar = findViewById(R.id.bottom_navigation);
        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_property);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button addReview = findViewById(R.id.addReview);
        ImageButton back = findViewById(R.id.back);
        String propertyAddress = getIntent().getStringExtra("address");

        address = findViewById(R.id.twAddress);
        location = findViewById(R.id.ratingLocation);
        conditions = findViewById(R.id.ratingConditions);
        safety = findViewById(R.id.ratingSafety);
        landlord = findViewById(R.id.ratingLandlord);

        listView = findViewById(R.id.listview);
        reviewAdapter = new ReviewAdapter(this, reviewList,"showProperty");
        listView.setAdapter(reviewAdapter);

        navBar = findViewById(R.id.bottom_navigation);
        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(showProperty.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(showProperty.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(showProperty.this, user.class));
                return true;
            }

            return false;
        });

        findPropertyDetails(propertyAddress);

        addReview.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Toast.makeText(showProperty.this, "Login to leave a review", Toast.LENGTH_SHORT).show();
            } else {
                leaveReview reviewDialog = leaveReview.newInstance(propertyId);
                reviewDialog.show(getSupportFragmentManager(), "LeaveReviewDialog");
            }
        });

        back.setOnClickListener(view -> finish());
    }

    private void findPropertyDetails(String pAddress) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("properties").whereEqualTo("address", pAddress)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Property property = doc.toObject(Property.class);

                        if (property != null) {
                            propertyId = doc.getId();
                            address.setText(property.getAddress());

                            location.setIsIndicator(true);
                            conditions.setIsIndicator(true);
                            safety.setIsIndicator(true);
                            landlord.setIsIndicator(true);

                            List<String> imageUrls = (List<String>) doc.get("imageUris");
                            if (imageUrls != null && !imageUrls.isEmpty()) {
                                Log.d("showProperty", "Retrieved images: " + imageUrls);
                                loadPropertyImages(imageUrls);
                            } else {
                                Log.d("showProperty", "No images found for this property.");
                            }

                            // Fetch average ratings and set to RatingBars
                            property.fetchAverageRatings(db, (averages, reviewCount) -> {
                                location.setRating(averages.getOrDefault("location", 0f));
                                conditions.setRating(averages.getOrDefault("property_condition", 0f));
                                safety.setRating(averages.getOrDefault("safety", 0f));
                                landlord.setRating(averages.getOrDefault("landlord", 0f));
                            });

                            fetchReviews();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching property", e));
    }


    private void loadPropertyImages(List<String> imageUrls) {
        RecyclerView imageRecyclerView = findViewById(R.id.recyclerViewImages);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Convert Strings to Uri
        List<Uri> imageUris = new ArrayList<>();
        for (String url : imageUrls) {
            imageUris.add(Uri.parse(url));
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, imageUris);
        imageRecyclerView.setAdapter(imageAdapter);
    }



    private void fetchReviews() {
        if (propertyId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("reviews")
                .whereEqualTo("propertyId", propertyId.trim())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {

                        reviewList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Review review = document.toObject(Review.class);
                            if (review != null) {
                                review.setReviewId(document.getId());
                                reviewList.add(review);
                            } else {
                                Log.w("debugJ", "Review is null for document: " + document.getId());
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("debugJ", "Exception inside onSuccess: ", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("debugJ", "Error fetching reviews", e);
                });
    }
}

