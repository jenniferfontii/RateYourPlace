package com.example.rateyourplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter<Review> {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;
    //private String reviewId;

    public ReviewAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_review, parent, false);
        }

        Review review = getItem(position);
        TextView userEmail = convertView.findViewById(R.id.userEmail);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingBar);
        EditText comments = convertView.findViewById(R.id.comments);

        comments.setFocusable(false);
        comments.setClickable(false);
        comments.setBackground(null);

        if (review != null) {

            if(review.getUserEmail().isEmpty() || review.getUserEmail() == null){
                userEmail.setText("Unknown User");
            } else {
                userEmail.setText(review.getUserEmail());
            }

            ratingBar.setRating(review.getAverageRating());
            comments.setText(review.getComment());
            //reviewId = review.getReviewId();
        }

        convertView.setOnClickListener(v -> {
            String reviewId = review.getReviewId();
            if (review != null) {
                showReview reviewDialog = showReview.newInstance(reviewId);
                if (context instanceof showProperty) {
                    showProperty showProp = (showProperty) context;
                    reviewDialog.show(showProp.getSupportFragmentManager(), "showReview");
                }
            }
        });

        return convertView;
    }
}
