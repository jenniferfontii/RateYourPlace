package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class user extends AppCompatActivity {

    private FirebaseAuth auth;

    //On resume method, used if activity is accessed using a back button
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_account);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Assign xml components to variables
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_account);
        TextView manageAccount = findViewById(R.id.manageAccount);
        TextView settings = findViewById(R.id.settings);
        TextView pastReviews = findViewById(R.id.pastReviews);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        Button signout = findViewById(R.id.signOut);
        ImageView profilePic = findViewById(R.id.profilePic);
        TextView welcome = findViewById(R.id.welcome);

        //check that user is logged in
        if (user != null) {
            Profile.loadProfilePicture(this, profilePic);
            welcome.setText(String.format("Welcome %s", user.getEmail()));

        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Action listeners
        manageAccount.setOnClickListener(view -> {
            startActivity(new Intent(user.this, accountManagement.class));
        });

        settings.setOnClickListener(view -> {
            startActivity(new Intent(user.this, settings.class));
        });

        pastReviews.setOnClickListener(view -> {
            startActivity(new Intent(user.this, pastReviews.class));
        });

        signout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(user.this, MainActivity.class);
            startActivity(intent);
        });

        //navbar action listener
        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(user.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(user.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {

                return true;
            }

            return false;
        });
    }
}