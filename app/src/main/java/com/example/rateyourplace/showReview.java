package com.example.rateyourplace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class showReview extends DialogFragment {

    //Global variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RatingBar location, conditions, landlord, safety;
    private EditText comments;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private String reviewId;

    //constructor
    public static showReview newInstance(String reviewId) {
        showReview fragment = new showReview();
        Bundle args = new Bundle();
        args.putString("reviewId",reviewId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            reviewId = getArguments().getString("reviewId");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_show_review, null);

        //Assign xml components to variables
        location = view.findViewById(R.id.ratingLocation);
        conditions = view.findViewById(R.id.ratingConditions);
        landlord = view.findViewById(R.id.ratingLandlord);
        safety = view.findViewById(R.id.ratingSafety);
        comments = view.findViewById(R.id.comments);
        recyclerView = view.findViewById(R.id.recyclerViewImages);

        location.setIsIndicator(true);
        conditions.setIsIndicator(true);
        landlord.setIsIndicator(true);
        safety.setIsIndicator(true);
        comments.setClickable(false);
        comments.setFocusable(false);
        comments.setBackground(null);

        // Setup RecyclerView
        imageAdapter = new ImageAdapter(getActivity(), imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(imageAdapter);

        builder.setView(view)
                .setTitle("Review Details")
                .setNegativeButton("Close", (dialog, id) -> dismiss());

        // Load review data from Firestore
        loadReviewData();

        return builder.create();
    }

    private void loadReviewData() {
        if (reviewId == null) return;
        db.collection("reviews").document(reviewId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        populateReviewData(document); // Populate data if the document exists
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("showReview", "Error fetching review data", e);
                });
    }

    private void populateReviewData(DocumentSnapshot document) {
        // Set ratings and comments from the document
        location.setRating(document.getDouble("ratingLocation").floatValue());
        conditions.setRating(document.getDouble("ratingConditions").floatValue());
        landlord.setRating(document.getDouble("ratingLandlord").floatValue());
        safety.setRating(document.getDouble("ratingSafety").floatValue());
        comments.setText(document.getString("comment"));

        // Get list of stored image paths (if any)
        List<String> imagePaths = (List<String>) document.get("imageUris");
        if (imagePaths != null) {
            loadImagesFromLocalStorage(imagePaths);
        }
    }

    private void loadImagesFromLocalStorage(List<String> imagePaths) {
        imageUris.clear();
        for (String imagePath : imagePaths) {
            File imageFile = new File(requireContext().getExternalFilesDir(null), "property_images/" + new File(imagePath).getName());
            if (imageFile.exists()) {
                imageUris.add(Uri.fromFile(imageFile));
            } else {
                Log.e("showReview", "Image file not found: " + imageFile.getAbsolutePath());
            }
        }
        imageAdapter.notifyDataSetChanged();
    }

}
