package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class user extends AppCompatActivity {

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

        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_account);
        TextView manageAccount = findViewById(R.id.manageAccount);
        TextView settings = findViewById(R.id.settings);
        TextView pastReviews = findViewById(R.id.pastReviews);

        manageAccount.setOnClickListener(view -> {
            startActivity(new Intent(user.this, accountManagement.class));
        });

       settings.setOnClickListener(view -> {
            startActivity(new Intent(user.this, accountManagement.class));
        });

        pastReviews.setOnClickListener(view -> {
            startActivity(new Intent(user.this, accountManagement.class));
        });


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