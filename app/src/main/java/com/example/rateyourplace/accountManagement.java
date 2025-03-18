package com.example.rateyourplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class accountManagement extends AppCompatActivity {

    private static final int IMAGE_PICKER_REQUEST_CODE = 100;

    private ImageView insertPicture;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private final ActivityResultLauncher<Intent> pictureSelectionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Uri selectedImageUri = result.getData().getData();
            if (selectedImageUri != null) {
                saveImageLocally(selectedImageUri);
            }
        }
    });

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
        setContentView(R.layout.activity_account_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button signout = findViewById(R.id.signOut);
        TextView changepsw = findViewById(R.id.changePsw);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(0);
        ImageButton back = findViewById(R.id.back);
        EditText email = findViewById(R.id.email);
        insertPicture = findViewById(R.id.account);

        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        email.setEnabled(false);


        if (user != null) {
            email.setText(user.getEmail());
            Profile.loadProfilePicture(this, insertPicture);

        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        back.setOnClickListener(view -> {
            finish();
        });

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(accountManagement.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(accountManagement.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(accountManagement.this, user.class));
            }

            return false;
        });

        changepsw.setOnClickListener(view -> {
            changePassword dialog = new changePassword();
            dialog.show(getSupportFragmentManager(), "ChangePassword");
        });

        signout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(accountManagement.this, MainActivity.class);
            startActivity(intent);
        });

        insertPicture.setOnClickListener(view -> openPictures());

    }

    private void openPictures() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureSelectionLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                saveImageLocally(selectedImageUri);
            }
        }
    }

    private void saveImageLocally(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                File profileImageFile = new File(getFilesDir(), "profile_picture.jpg");
                FileOutputStream outputStream = new FileOutputStream(profileImageFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                runOnUiThread(() -> insertPicture.setImageURI(Uri.fromFile(profileImageFile)));
                saveProfilePictureToFirestore(profileImageFile.getName());

                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveProfilePictureToFirestore(String fileName) {
        String userId = auth.getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(this, "User ID is null!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("profile_picture", fileName);

        firestore.collection("users").document(userId).set(data, SetOptions.merge())  // Merges if the document exists, creates if it doesn't
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Uploaded to Firebase!", Toast.LENGTH_SHORT).show();
                    Profile.loadProfilePicture(this, insertPicture);
                }).addOnFailureListener(e -> {
                    Log.e("ProfilePictureDebug", "Failed to save profile picture: " + e.getMessage());
                    Toast.makeText(this, "Failed to save profile picture", Toast.LENGTH_SHORT).show();
                });
    }

}
