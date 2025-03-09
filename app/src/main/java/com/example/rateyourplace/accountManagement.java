package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class accountManagement extends AppCompatActivity {

    private FirebaseAuth auth;

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
        TextView changepsw =findViewById(R.id.changePsw);
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(R.id.nav_account);
        ImageButton back = findViewById(R.id.back);
        EditText email = findViewById(R.id.email);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        email.setEnabled(false);

        if (user != null) {
            email.setText(user.getEmail());

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
    }
}