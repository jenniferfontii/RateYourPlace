package com.example.rateyourplace;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class settings extends AppCompatActivity {
    FirebaseAuth auth;

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= 33) {
                if
                (ContextCompat.checkSelfPermission(settings.this,
                        android.Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(settings.this, new
                            String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
                }
            }

            NotificationChannel channel = new
                    NotificationChannel("firebase", "Firebase channel",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        getFirebaseCloudMessagingToken();

        ImageButton back = findViewById(R.id.back);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);
        TextView deleteAccount = findViewById(R.id.deleteAccount);
        ImageView profilePic = findViewById(R.id.profilePic);
        TextView welcome = findViewById(R.id.welcome);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            Profile.loadProfilePicture(this, profilePic);
            welcome.setText(String.format("Welcome %s", user.getEmail()));

        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

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

        back.setOnClickListener(view -> {
            finish();
        });

        deleteAccount.setOnClickListener(view -> {
            deleteAccount dialog = new deleteAccount();
            dialog.show(getSupportFragmentManager(), "Delete account");
        });

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