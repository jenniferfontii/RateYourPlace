package com.example.rateyourplace;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addProperty extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    private EditText addressET, comments;
    private RatingBar ratingLocation, ratingConditions, ratingSafety, ratingLandlord;
    private Button selectImagesBtn, submitBtn, addressFinderBtn;
    private BottomNavigationView navBar;
    private ImageButton back;
    private LinearLayout rootLayout;
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        recyclerView = findViewById(R.id.recyclerViewImages);
        addressET = findViewById(R.id.address);
        comments = findViewById(R.id.comments);
        ratingLocation = findViewById(R.id.ratingLocation);
        ratingConditions = findViewById(R.id.ratingConditions);
        ratingSafety = findViewById(R.id.ratingSafety);
        ratingLandlord = findViewById(R.id.ratingLandlord);
        selectImagesBtn = findViewById(R.id.addImage);
        submitBtn = findViewById(R.id.submitBtn);
        navBar = findViewById(R.id.bottom_navigation);
        rootLayout = findViewById(R.id.linear);
        back = findViewById(R.id.back);
        addressFinderBtn =findViewById(R.id.openAddressFinder);


        imageAdapter = new ImageAdapter(this, imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(imageAdapter);

        selectImagesBtn.setOnClickListener(v -> openImagePicker());

        submitBtn.setOnClickListener(v -> {
            savePropertyToFirestore();
            Intent intent = new Intent(addProperty.this, home.class);
            startActivity(intent);
            Toast.makeText(this, "Property added succesfully", Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(view -> {
            finish();
        });

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(addProperty.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(addProperty.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(addProperty.this, user.class));
            }

            return false;
        });

        comments.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                navBar.setVisibility(BottomNavigationView.GONE); // Hide navigation bar
            } else {
                navBar.setVisibility(BottomNavigationView.VISIBLE);
            }
        });

        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Dismiss the keyboard if any view is touched outside the EditText
                View view = getWindow().getDecorView();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // Perform the click action for accessibility
                rootLayout.performClick();
            }
            return false;
        });

        addressFinderBtn.setOnClickListener(view -> {
                findAddress dialog = new findAddress((address, latitude, longitude) -> {
                // Set the selected address and coordinates
                addressET.setText(address);
                selectedLat = latitude;
                selectedLon = longitude;
            });
            dialog.show(getSupportFragmentManager(), "FindAddressDialog");
        });

    }

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        if (data.getClipData() != null) {
                            // Multiple images selected
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                ClipData.Item item = data.getClipData().getItemAt(i);
                                if (item != null && item.getUri() != null) {
                                    imageUris.add(item.getUri());
                                }
                            }
                        } else if (data.getData() != null) {
                            // Single image selected
                            imageUris.add(data.getData());
                        }

                        // Notify the adapter to update the RecyclerView
                        imageAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error selecting images", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Open gallery to select images
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    // Save property details to Firestore
    private void savePropertyToFirestore() {
        String address = this.addressET.getText().toString().trim();
        String comments = this.comments.getText().toString().trim();
        int locationRating = (int) ratingLocation.getRating();
        int conditionRating = (int) ratingConditions.getRating();
        int safetyRating = (int) ratingSafety.getRating();
        int landlordRating = (int) ratingLandlord.getRating();

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> property = new HashMap<>();
        property.put("address", address);
        property.put("location", locationRating);
        property.put("property_condition", conditionRating);
        property.put("safety", safetyRating);
        property.put("landlord", landlordRating);
        property.put("additional_comments", comments);
        property.put("latitude", selectedLat);
        property.put("longitude", selectedLon);

        // Convert URIs to Strings to store in Firestore
        List<String> imageUrisList = new ArrayList<>();
        for (Uri uri : imageUris) {
            imageUrisList.add(uri.toString());
        }
        property.put("imageUris", imageUrisList);

        db.collection("properties").document(address)
                .set(property)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Property saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving property", Toast.LENGTH_SHORT).show());
    }
}
