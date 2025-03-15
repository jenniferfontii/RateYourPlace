package com.example.rateyourplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
                new ImageLoader(ivPropertyImage).execute(imageUrl);
            } else {
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        }

        return convertView;
    }
}
