package com.example.rateyourplace;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addProperty extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    private EditText addressET;
    private Button selectImagesBtn, submitBtn, addressFinderBtn;
    private ImageButton back;
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;
    private BottomNavigationView navBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        recyclerView = findViewById(R.id.recyclerViewImages);
        addressET = findViewById(R.id.address);
        selectImagesBtn = findViewById(R.id.addImage);
        submitBtn = findViewById(R.id.submitBtn);
        back = findViewById(R.id.back);
        addressFinderBtn = findViewById(R.id.openAddressFinder);
        navBar = findViewById(R.id.bottom_navigation);

        imageAdapter = new ImageAdapter(this, imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(imageAdapter);

        selectImagesBtn.setOnClickListener(v -> checkGalleryPermission());

        submitBtn.setOnClickListener(v -> {
            savePropertyToFirebase();
            Intent intent = new Intent(addProperty.this, home.class);
            startActivity(intent);
            Toast.makeText(this, "Property added successfully", Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(view -> finish());

        addressFinderBtn.setOnClickListener(view -> {
            findAddress dialog = new findAddress((address, latitude, longitude) -> {
                addressET.setText(address);
                selectedLat = latitude;
                selectedLon = longitude;
            });
            dialog.show(getSupportFragmentManager(), "FindAddressDialog");
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
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
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
            }
    );

    private final ActivityResultLauncher<String> galleryPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Gallery permission denied. Cannot select images.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Gallery Permission Needed")
                .setMessage("This app requires access to your gallery to select images. Would you like to grant permission?")
                .setPositiveButton("Yes", (dialog, which) -> requestGalleryPermission())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void requestGalleryPermission() {
        galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void savePropertyToFirebase() {
        String address = this.addressET.getText().toString().trim();

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> property = new HashMap<>();
        property.put("address", address);
        property.put("latitude", selectedLat);
        property.put("longitude", selectedLon);

        List<String> imageUrisList = new ArrayList<>();
        for (Uri uri : imageUris) {
            Uri savedUri = saveImageLocally(uri);
            if (savedUri != null) {
                imageUrisList.add(savedUri.toString());
            }
        }
        property.put("imageUris", imageUrisList);

        db.collection("properties").document(address)
                .set(property)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Property saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving property", Toast.LENGTH_SHORT).show());
    }

    private Uri saveImageLocally(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            File directory = new File(getExternalFilesDir(null), "property_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

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
}
