package com.example.rateyourplace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            fetchPropertiesFromFirestore();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void fetchPropertiesFromFirestore() {
        db = FirebaseFirestore.getInstance();
        CollectionReference propertiesRef = db.collection("properties");

        propertiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean firstLocationSet = false;
                for (DocumentSnapshot document : task.getResult()) {
                    Double latitude = document.getDouble("latitude");
                    Double longitude = document.getDouble("longitude");
                    String address = document.getString("address");

                    if (latitude != null && longitude != null && address != null) {
                        LatLng location = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(location).title(address));

                        // Move camera to the first property
                        if (!firstLocationSet) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                            firstLocationSet = true;
                        }
                    }
                }
                if (!firstLocationSet) {
                    Toast.makeText(getActivity(), "No properties found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Firestore", "Error fetching properties", task.getException());
                Toast.makeText(getActivity(), "Error loading properties", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
