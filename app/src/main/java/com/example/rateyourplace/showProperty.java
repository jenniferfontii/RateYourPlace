package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class showProperty extends AppCompatActivity {

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
        setContentView(R.layout.activity_show_property);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button addReview = findViewById(R.id.addReview);
        ImageButton back = findViewById(R.id.back);

        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(showProperty.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(showProperty.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(showProperty.this, user.class));
                return true;
            }

            return false;
        });

        addReview.setOnClickListener(view -> {
            leaveReview dialog = new leaveReview();
            dialog.show(getSupportFragmentManager(), "Leave a review");
        });

        back.setOnClickListener(view -> {
            finish();
        });
    }
}