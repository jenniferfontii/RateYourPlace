package com.example.rateyourplace;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class leaveReview extends DialogFragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private RatingBar location, conditions, landlord, safety;
    private EditText comments;
    private Button addPics;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private String propertyId;
    private String existingReviewId;

    public static leaveReview newInstance(String propertyId) {
        leaveReview fragment = new leaveReview();
        Bundle args = new Bundle();
        args.putString("propertyId", propertyId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            propertyId = getArguments().getString("propertyId");
        }

        if (user == null) {
            Toast.makeText(getActivity(), "Login to leave a review", Toast.LENGTH_SHORT).show();
            dismiss();
            return new Dialog(requireContext());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_leave_review, null);

        location = view.findViewById(R.id.ratingLocation);
        conditions = view.findViewById(R.id.ratingConditions);
        landlord = view.findViewById(R.id.ratingLandlord);
        safety = view.findViewById(R.id.ratingSafety);
        comments = view.findViewById(R.id.comments);
        addPics = view.findViewById(R.id.addPics);
        recyclerView = view.findViewById(R.id.recyclerViewImages);

        addPics.setOnClickListener(view1 -> checkPermissionsAndOpenImagePicker());

        imageAdapter = new ImageAdapter(getActivity(), imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(imageAdapter);

        builder.setView(view)
                .setTitle("Leave a Review")
                .setPositiveButton("Save", (dialog, id) -> saveReviewToFirebase())
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        checkExistingReview();

        return builder.create();
    }

    private void checkExistingReview() {
        if (user == null || propertyId == null) return;

        db.collection("reviews")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("propertyId", propertyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        existingReviewId = document.getId();
                        loadExistingReview(document);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking existing review", e));
    }

    private void loadExistingReview(DocumentSnapshot document) {
        location.setRating(document.getDouble("ratingLocation").floatValue());
        conditions.setRating(document.getDouble("ratingConditions").floatValue());
        landlord.setRating(document.getDouble("ratingLandlord").floatValue());
        safety.setRating(document.getDouble("ratingSafety").floatValue());
        comments.setText(document.getString("comment"));

        List<String> imageUrisList = (List<String>) document.get("imageUris");
        if (imageUrisList != null) {
            for (String uri : imageUrisList) {
                imageUris.add(Uri.parse(uri));
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void checkPermissionsAndOpenImagePicker() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(getActivity(), "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
                }
            });

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            ClipData.Item item = data.getClipData().getItemAt(i);
                            if (item != null && item.getUri() != null) {
                                imageUris.add(item.getUri());
                            }
                        }
                    } else if (data.getData() != null) {
                        imageUris.add(data.getData());
                    }

                    imageAdapter.notifyDataSetChanged();
                }
            }
    );

    private Uri saveImageLocally(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            File directory = requireContext().getExternalFilesDir(null);
            if (directory == null) {
                Toast.makeText(getActivity(), "Storage not available", Toast.LENGTH_SHORT).show();
                return null;
            }
            directory = new File(directory, "property_images");
            if (!directory.exists()) directory.mkdirs();

            String fileName = "image_" + System.currentTimeMillis() + ".png";
            File imageFile = new File(directory, fileName);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return Uri.fromFile(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveReviewToFirebase() {
        if (user != null && propertyId != null) {
            String reviewId = (existingReviewId != null) ? existingReviewId : UUID.randomUUID().toString();

            float ratingLocation = location.getRating();
            float ratingConditions = conditions.getRating();
            float ratingSafety = safety.getRating();
            float ratingLandlord = landlord.getRating();
            String comment = comments.getText().toString();

            Map<String, Object> review = new HashMap<>();
            review.put("propertyId", propertyId);
            review.put("userId", user.getUid());
            review.put("userEmail", user.getEmail());
            review.put("ratingLocation", ratingLocation);
            review.put("ratingConditions", ratingConditions);
            review.put("ratingSafety", ratingSafety);
            review.put("ratingLandlord", ratingLandlord);
            review.put("comment", comment);
            review.put("timestamp", System.currentTimeMillis());

            db.collection("reviews").document(reviewId)
                    .set(review)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Review Saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save review", Toast.LENGTH_SHORT).show());
        }
    }
}
