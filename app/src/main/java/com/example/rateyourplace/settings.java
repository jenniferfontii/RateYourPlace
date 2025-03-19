package com.example.rateyourplace;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class settings extends AppCompatActivity {

    FirebaseAuth auth;
    private Switch notifications, location, galleryAccess;
    BottomNavigationView navBar;

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
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("firebase", "Firebase channel",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        getFirebaseCloudMessagingToken();

        //Assign xml components to variables
        ImageButton back = findViewById(R.id.back);
        navBar = findViewById(R.id.bottom_navigation);
        TextView deleteAccount = findViewById(R.id.deleteAccount);
        ImageView profilePic = findViewById(R.id.profilePic);
        TextView welcome = findViewById(R.id.welcome);
        notifications = findViewById(R.id.switchNotification);
        location = findViewById(R.id.switchLocation);
        galleryAccess = findViewById(R.id.switchGallery);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Chack that user is logged in
        if (user != null) {
            Profile.loadProfilePicture(this, profilePic);
            welcome.setText(String.format("Welcome %s", user.getEmail()));
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        //navbar action listener and focus
        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                startActivity(new Intent(settings.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(settings.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(settings.this, user.class));
                return true;
            }
            return false;
        });

        back.setOnClickListener(view -> finish());

        deleteAccount.setOnClickListener(view -> {
            deleteAccount dialog = new deleteAccount();
            dialog.show(getSupportFragmentManager(), "Delete account");
        });

        //Check permissions
        checkPermissions();

        //Set switches accordig to permissions
        notifications.setOnCheckedChangeListener((buttonView, isChecked) -> handleNotificationPermission(isChecked));
        location.setOnCheckedChangeListener((buttonView, isChecked) -> handleLocationPermission(isChecked));
        galleryAccess.setOnCheckedChangeListener((buttonView, isChecked) -> handleGalleryPermission(isChecked));
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            notifications.setChecked(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
        }

        location.setChecked(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        galleryAccess.setChecked(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }


    private void handleNotificationPermission(boolean enable) {
        if (enable) {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        } else {
            Toast.makeText(this, "Cannot revoke notification permission manually. Change in system settings.", Toast.LENGTH_SHORT).show();
            notifications.setChecked(true);
        }
    }

    private void handleLocationPermission(boolean enable) {
        if (enable) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 102);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Disable location access from App Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryPermission(boolean enable) {
        if (enable) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 103);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Disable gallery access from App Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFirebaseCloudMessagingToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d("FCM Token", "Token: " + token);
                    } else {
                        Log.e("FCM Token", "Failed to get token");
                    }
                });
    }
}
