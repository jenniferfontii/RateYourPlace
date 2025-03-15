package com.example.rateyourplace;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyAdapter extends ArrayAdapter<Property> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    public PropertyAdapter(Context context, List<Property> properties) {
        super(context, 0, properties);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_property, parent, false);
        }

        Property property = getItem(position);
        TextView tvAddress = convertView.findViewById(R.id.address);
        ImageView ivPropertyImage = convertView.findViewById(R.id.ivMainImage);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingBar);
        ImageButton saveBtn = convertView.findViewById(R.id.save);

        saveBtn.setOnClickListener(view -> handleSaveProperty(property, saveBtn));

        checkIfPropertyIsSaved(property.getAddress(), saveBtn);

        if (property != null) {
            tvAddress.setText(property.getAddress());
            ratingBar.setRating(property.getAverageRating());

            String imageUrl = property.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivPropertyImage.setTag(imageUrl);
                loadImageUsingPicasso(imageUrl, ivPropertyImage);
            } else {
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        }

        return convertView;
    }

    private void checkIfPropertyIsSaved(String propertyAddress, ImageButton btnSave) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        DocumentReference userSavedRef = db.collection("saved_properties").document(userId);

        userSavedRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> savedProperties = (List<String>) documentSnapshot.get("properties");
                if (savedProperties != null && savedProperties.contains(propertyAddress)) {
                    btnSave.setImageResource(R.drawable.ic_saved_full);
                } else {
                    btnSave.setImageResource(R.drawable.ic_saved);
                }
            } else {
                btnSave.setImageResource(R.drawable.ic_saved);
            }
        }).addOnFailureListener(e -> btnSave.setImageResource(R.drawable.ic_saved));
    }


    private void handleSaveProperty(Property property, ImageButton btnSave) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String propertyAddress = property.getAddress();
        DocumentReference userSavedRef = db.collection("saved_properties").document(userId);

        userSavedRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> savedProperties = (documentSnapshot.exists())
                    ? (List<String>) documentSnapshot.get("properties")
                    : new ArrayList<>();

            if (savedProperties == null) savedProperties = new ArrayList<>();

            if (savedProperties.contains(propertyAddress)) {
                // 🔹 Property is already saved → Remove it
                savedProperties.remove(propertyAddress);
                userSavedRef.set(Collections.singletonMap("properties", savedProperties), SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            btnSave.setImageResource(R.drawable.ic_saved);
                            Toast.makeText(getContext(), "Property removed!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove property", Toast.LENGTH_SHORT).show());
            } else {
                // 🔹 Property is NOT saved → Add it
                savedProperties.add(propertyAddress);
                userSavedRef.set(Collections.singletonMap("properties", savedProperties), SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            btnSave.setImageResource(R.drawable.ic_saved_full);
                            Toast.makeText(getContext(), "Property saved!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save property", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading saved properties", Toast.LENGTH_SHORT).show());
    }




    private void loadImageUsingPicasso(String imageUrl, final ImageView ivPropertyImage) {
        if (imageUrl.startsWith("content://")) {
            try {
                Uri uri = Uri.parse(imageUrl);
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivPropertyImage);
            } catch (Exception e) {
                Log.e("ImageLoader", "Error loading image from content URI: " + e.getMessage());
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivPropertyImage);
        }
    }

}
