package com.example.rateyourplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class pastReviews extends AppCompatActivity {

    private ListView listView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_past_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton back = findViewById(R.id.back);
        listView = findViewById(R.id.listview);
        reviewAdapter = new ReviewAdapter(this, reviewList,"pastReviews");
        listView.setAdapter(reviewAdapter);

        BottomNavigationView navBar = findViewById(R.id.bottom_navigation);
        navBar.setSelectedItemId(-1);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(pastReviews.this, home.class));
                return true;
            } else if (itemId == R.id.nav_saved) {
                startActivity(new Intent(pastReviews.this, savedProperties.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(pastReviews.this, user.class));
                return true;
            }

            return false;
        });
        back.setOnClickListener(view -> finish());
        fetchReviews();
    }
    public void fetchReviews(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("reviews")
                .whereEqualTo("userId", userId.trim())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {

                        reviewList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Review review = document.toObject(Review.class);
                            if (review != null) {
                                review.setReviewId(document.getId());
                                reviewList.add(review);
                            } else {
                                Log.w("debugJ", "Review is null for document: " + document.getId());
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("debugJ", "Exception inside onSuccess: ", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("debugJ", "Error fetching reviews", e);
                });
    }
}