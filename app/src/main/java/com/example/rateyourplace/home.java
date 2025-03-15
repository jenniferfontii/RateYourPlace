package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class home extends AppCompatActivity {

    private ListView listView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_search);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        listView = findViewById(R.id.listView);
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList);
        listView.setAdapter(propertyAdapter);
        db = FirebaseFirestore.getInstance();

        Button addPropertyBtn = findViewById(R.id.addPropertyBtn);
        ImageButton mapView = findViewById(R.id.searchMap);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_search);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(home.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(home.this, user.class));
                return true;
            }

            return false;
        });

        addPropertyBtn.setOnClickListener(v -> startActivity(new Intent(home.this, addProperty.class)));
        mapView.setOnClickListener(view -> startActivity(new Intent(home.this, mapsSearch.class)));

        fetchProperties();
    }

    private void fetchProperties() {
        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyList.clear(); // Clear the list before adding new data
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
                    propertyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(home.this, "Failed to load properties", Toast.LENGTH_SHORT).show());
    }




}
