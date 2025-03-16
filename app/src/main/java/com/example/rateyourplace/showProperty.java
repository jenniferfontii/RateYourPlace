package com.example.rateyourplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class showProperty extends AppCompatActivity {

    private TextView address;
    private RatingBar location, conditions, safety, landlord;
    private String propertyId; // Global variable to store the property ID

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(0);
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

        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);

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

        // Load property details and set propertyId dynamically
        findPropertyDetails(propertyAddress);

        addReview.setOnClickListener(view -> {
            if (propertyId != null) { // Ensure propertyId is set before opening the review dialog
                leaveReview dialog = leaveReview.newInstance(propertyId);
                dialog.show(getSupportFragmentManager(), "Leave a review");
            } else {
                Log.e("Review", "Property ID is null. Review cannot be added.");
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
                            // Set global propertyId
                            propertyId = doc.getId();  // Get Firestore document ID as the propertyId

                            // Update UI with property details
                            address.setText(property.getAddress());
                            location.setIsIndicator(true);
                            conditions.setIsIndicator(true);
                            safety.setIsIndicator(true);
                            landlord.setIsIndicator(true);
                            location.setRating(property.getLocation());
                            conditions.setRating(property.getPropertyCondition());
                            safety.setRating(property.getSafety());
                            landlord.setRating(property.getLandlord());

                            // Handle images
                            List<String> imageUris = property.getImageUris();
                            List<Uri> uris = new ArrayList<>();
                            if (imageUris != null && !imageUris.isEmpty()) {
                                for (String uriString : imageUris) {
                                    uris.add(Uri.parse(uriString));
                                }
                            } else {
                                uris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.ic_placeholder));
                            }

                            RecyclerView recyclerView = findViewById(R.id.recyclerViewImages);
                            ImageAdapter imageAdapter = new ImageAdapter(showProperty.this, uris);
                            recyclerView.setLayoutManager(new LinearLayoutManager(showProperty.this, LinearLayoutManager.HORIZONTAL, false));
                            recyclerView.setAdapter(imageAdapter);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching property", e));
    }
}
