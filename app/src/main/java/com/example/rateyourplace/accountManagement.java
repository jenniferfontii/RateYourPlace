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

    //set global variables
    private static final int IMAGE_PICKER_REQUEST_CODE = 100;
    private ImageView insertPicture;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    BottomNavigationView navBar;

    //image picker
    private final ActivityResultLauncher<Intent> pictureSelectionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Uri selectedImageUri = result.getData().getData();
            if (selectedImageUri != null) {
                saveImageLocally(selectedImageUri);
            }
        }
    });

    //On resume method, used if activity is accessed using a back button
    @Override
    protected void onResume() {
        super.onResume();
        navBar = findViewById(R.id.bottom_navigation);
        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);
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

        //Assign xml components to variables
        Button signout = findViewById(R.id.signOut);
        TextView changepsw = findViewById(R.id.changePsw);
        ImageButton back = findViewById(R.id.back);
        EditText email = findViewById(R.id.email);
        insertPicture = findViewById(R.id.account);

        //Initialize firestore Db and Auth service, get current user
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Set email as not enable so that it can't be changes
        email.setEnabled(false);

        //set up user details
        if (user != null) {
            email.setText(user.getEmail());
            Profile.loadProfilePicture(this, insertPicture);

        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Back button returns to previous screen
        back.setOnClickListener(view -> {
            finish();
        });

        //Set navbar focus to none since this page is not connected directly with it
        navBar = findViewById(R.id.bottom_navigation);
        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);

        //navbar action listeners
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

        //Call change password dialog when button is pressed
        changepsw.setOnClickListener(view -> {
            changePassword dialog = new changePassword();
            dialog.show(getSupportFragmentManager(), "ChangePassword");
        });

        //Sign out action listener
        signout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(accountManagement.this, MainActivity.class);
            startActivity(intent);
        });

        //Change profile picture by clicking the image
        insertPicture.setOnClickListener(view -> openPictures());

    }

    //Opens image picker
    private void openPictures() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureSelectionLauncher.launch(intent);
    }

    //Callback
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

    //Stores profile picture locally
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

                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

}
