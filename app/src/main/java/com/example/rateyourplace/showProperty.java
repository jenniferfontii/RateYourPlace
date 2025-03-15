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

        addReview.setOnClickListener(view -> {
            leaveReview dialog = new leaveReview();
            dialog.show(getSupportFragmentManager(), "Leave a review");
        });

        back.setOnClickListener(view -> finish());

        findPropertyDetails(propertyAddress);
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
                            // Set address text
                            address.setText(property.getAddress());

                            // Set ratings
                            location.setIsIndicator(true);
                            conditions.setIsIndicator(true);
                            safety.setIsIndicator(true);
                            landlord.setIsIndicator(true);

                            location.setRating(property.getLocation());
                            conditions.setRating(property.getPropertyCondition());
                            safety.setRating(property.getSafety());
                            landlord.setRating(property.getLandlord());

                            // Set images
                            List<String> imageUris = property.getImageUris();  // List of image URLs
                            List<Uri> uris = new ArrayList<>();

                            // Convert Strings to Uris, add placeholder if empty
                            if (imageUris != null && !imageUris.isEmpty()) {
                                for (String uriString : imageUris) {
                                    uris.add(Uri.parse(uriString));
                                }
                            } else {
                                uris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.ic_placeholder));
                            }

                            // Set the RecyclerView with the image adapter
                            ImageAdapter imageAdapter = new ImageAdapter(showProperty.this, uris);
                            RecyclerView recyclerView = findViewById(R.id.recyclerViewImages);
                            recyclerView.setLayoutManager(new LinearLayoutManager(showProperty.this, LinearLayoutManager.HORIZONTAL, false));
                            recyclerView.setAdapter(imageAdapter);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching property", e));
    }

}
