package com.example.rateyourplace;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class mapsSearch extends AppCompatActivity {

    BottomNavigationView navBar;
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
        setContentView(R.layout.activity_maps_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.maps_container, new MapsFragment());
            transaction.commit();
        }

        Button listView =findViewById(R.id.listView);
        navBar = findViewById(R.id.bottom_navigation);

        navBar.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < navBar.getMenu().size(); i++) {
            navBar.getMenu().getItem(i).setChecked(false);
        }
        navBar.getMenu().setGroupCheckable(0, true, true);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(mapsSearch.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(mapsSearch.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(mapsSearch.this, user.class));
            }

            return false;
        });

        listView.setOnClickListener(view -> {
            Intent intent = new Intent(mapsSearch.this, home.class);
            startActivity(intent);
        });

    }

}