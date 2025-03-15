package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class savedProperties extends AppCompatActivity {
    private ListView listView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList = new ArrayList<>();
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_saved);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_properties);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton mapView = findViewById(R.id.searchMap);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_search);

        listView = findViewById(R.id.listView);
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList);
        listView.setAdapter(propertyAdapter);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(savedProperties.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(savedProperties.this, user.class));
                return true;
            }

            return false;
        });

        mapView.setOnClickListener(view -> {
            Intent intent = new Intent(savedProperties.this, mapsSearch.class);
            startActivity(intent);
        });
        if (user != null) {
            fetchProperties(user);

        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void fetchProperties(FirebaseUser user) {

        String userId = user.getUid();
        DocumentReference userSavedRef = db.collection("saved_properties").document(userId);

        userSavedRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> savedProperties = (List<String>) documentSnapshot.get("properties");

                if (savedProperties != null && !savedProperties.isEmpty()) {
                    // Clear the list before adding new data
                    propertyList.clear();

                    // Fetch properties details for each saved property (address)
                    for (String propertyAddress : savedProperties) {
                        db.collection("properties")
                                .whereEqualTo("address", propertyAddress)  // Query for saved property
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            String address = document.getString("address");
                                            int location = document.contains("location") ? document.getLong("location").intValue() : 0;
                                            int propertyCondition = document.contains("property_condition") ? document.getLong("property_condition").intValue() : 0;
                                            int safety = document.contains("safety") ? document.getLong("safety").intValue() : 0;
                                            int landlord = document.contains("landlord") ? document.getLong("landlord").intValue() : 0;

                                            List<String> imageUris = (List<String>) document.get("imageUris");
                                            if (imageUris == null) {
                                                imageUris = new ArrayList<>();
                                            }

                                            Property property = new Property(address, imageUris, location, propertyCondition, safety, landlord);
                                            propertyList.add(property);
                                        }

                                        // Notify the adapter that the data has been updated
                                        propertyAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(this, "No saved property details found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load saved property details", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(this, "No saved properties found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No saved properties found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching saved properties", Toast.LENGTH_SHORT).show();
        });
    }

}