package com.example.rateyourplace;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PropertyAdapter extends ArrayAdapter<Property> {

    public PropertyAdapter(Context context, List<Property> properties) {
        super(context, 0, properties);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_property, parent, false);
        }

        Property property = getItem(position);
        TextView tvAddress = convertView.findViewById(R.id.address);
        ImageView ivPropertyImage = convertView.findViewById(R.id.ivMainImage);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingBar);

        if (property != null) {
            tvAddress.setText(property.getAddress());
            ratingBar.setRating(property.getAverageRating());

            String imageUrl = property.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivPropertyImage.setTag(imageUrl);

                // Use Picasso to load the image
                loadImageUsingPicasso(imageUrl, ivPropertyImage);
            } else {
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        }

        return convertView;
    }

    private void loadImageUsingPicasso(String imageUrl, final ImageView ivPropertyImage) {
        if (imageUrl.startsWith("content://")) {
            try {
                Uri uri = Uri.parse(imageUrl);
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.ic_placeholder) // Handle loading placeholder
                        .error(R.drawable.ic_placeholder) // Handle error placeholder
                        .into(ivPropertyImage);
            } catch (Exception e) {
                Log.e("ImageLoader", "Error loading image from content URI: " + e.getMessage());
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder) // Handle loading placeholder
                    .error(R.drawable.ic_placeholder) // Handle error placeholder
                    .into(ivPropertyImage);
        }
    }

}
