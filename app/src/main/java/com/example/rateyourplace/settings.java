package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class settings extends AppCompatActivity {

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

        ImageButton back = findViewById(R.id.back);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);
        TextView deleteAccount = findViewById(R.id.deleteAccount);

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
}