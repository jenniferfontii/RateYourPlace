package com.example.rateyourplace;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addProperty extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    private EditText address, comments;
    private RatingBar ratingLocation, ratingConditions, ratingSafety, ratingLandlord;
    private Button selectImagesBtn, submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        recyclerView = findViewById(R.id.recyclerViewImages);
        address = findViewById(R.id.address);
        comments = findViewById(R.id.comments);
        ratingLocation = findViewById(R.id.ratingLocation);
        ratingConditions = findViewById(R.id.ratingConditions);
        ratingSafety = findViewById(R.id.ratingSafety);
        ratingLandlord = findViewById(R.id.ratingLandlord);
        selectImagesBtn = findViewById(R.id.addImage);
        submitBtn = findViewById(R.id.submitBtn);


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
        String address = this.address.getText().toString().trim();
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
