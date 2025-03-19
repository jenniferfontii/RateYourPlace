package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.widget.SearchView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class home extends AppCompatActivity {

    //set global variables
    private ListView listView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList = new ArrayList<>();
    private FirebaseFirestore db;

    //On resume method, used if activity is accessed using a back button
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

        //Assign xml components to variables
        listView = findViewById(R.id.listView);
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList);
        listView.setAdapter(propertyAdapter);
        db = FirebaseFirestore.getInstance();

        Button addPropertyBtn = findViewById(R.id.addPropertyBtn);
        ImageButton mapView = findViewById(R.id.searchMap);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_search);
        SearchView searchView = findViewById(R.id.searchView);
        //if searchview is selected hides navabr for better design
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                navBar.setVisibility(View.GONE);
            } else {
                navBar.setVisibility(View.VISIBLE);
            }
        });
        //Query listener for searchbox
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProperties(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchProperties(query);
                return true;
            }
        });
        //navbar action listener
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

        // Buttons action listeners
        addPropertyBtn.setOnClickListener(v -> startActivity(new Intent(home.this, addProperty.class)));
        mapView.setOnClickListener(view -> startActivity(new Intent(home.this, mapsSearch.class)));

        fetchProperties();
    }

    // Fetches properties from db and displays them
    private void fetchProperties() {
        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyList.clear(); // Clear the list before adding new data
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String address = document.getString("address");

                        List<String> imageUris = (List<String>) document.get("imageUris");
                        if (imageUris == null) {
                            imageUris = new ArrayList<>();
                        }

                        Property property = new Property(address, imageUris);
                        propertyList.add(property);
                    }
                    propertyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(home.this, "Failed to load properties", Toast.LENGTH_SHORT).show());
    }

    // Fetches properties from DB that match the query and displays them
    private void searchProperties(String query) {
        db.collection("properties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String address = document.getString("address");

                        if (address != null && address.toLowerCase().contains(query.toLowerCase())) {

                            List<String> imageUris = (List<String>) document.get("imageUris");
                            if (imageUris == null) {
                                imageUris = new ArrayList<>();
                            }

                            Property property = new Property(address, imageUris);
                            propertyList.add(property);
                        }
                    }
                    propertyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(home.this, "Search failed", Toast.LENGTH_SHORT).show());
    }


}
