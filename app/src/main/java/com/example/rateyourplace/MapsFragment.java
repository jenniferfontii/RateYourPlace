package com.example.rateyourplace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean firstLocationSet = false;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            checkAndRequestLocationPermission();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Register the permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        checkUserLocation(); // If permission granted, fetch user location
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                        fetchPropertiesFromFirestore(); // Continue with properties
                    }
                }
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Show an AlertDialog to ask the user if they want to enable location
            new AlertDialog.Builder(requireContext())
                    .setTitle("Enable Location")
                    .setMessage("This app needs location permission to show your position on the map. Do you want to enable it?")
                    .setPositiveButton("Yes", (dialog, which) -> requestLocationPermission()) // Uses new method
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        fetchPropertiesFromFirestore(); // Continue without user location
                    })
                    .show();
        } else {
            checkUserLocation(); // If permission is already granted, fetch location
        }
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void checkUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                    firstLocationSet = true;
                }
            });
        }
    }

    private void fetchPropertiesFromFirestore() {
        db = FirebaseFirestore.getInstance();
        CollectionReference propertiesRef = db.collection("properties");
        propertiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult()) {
                    Double latitude = document.getDouble("latitude");
                    Double longitude = document.getDouble("longitude");
                    String address = document.getString("address");

                    if (latitude != null && longitude != null && address != null) {
                        LatLng location = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(address))
                                .setTag(address);

                        // If no user location was set, focus on the first property
                        if (!firstLocationSet) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                            firstLocationSet = true;
                        }
                    }
                }
                if (!firstLocationSet) {
                    Toast.makeText(getActivity(), "No properties found", Toast.LENGTH_SHORT).show();
                }

                mMap.setOnMarkerClickListener(marker -> {
                    String propertyId = (String) marker.getTag();
                    openPropertyDetails(propertyId);
                    return true;
                });
            } else {
                Toast.makeText(getActivity(), "Error loading properties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPropertyDetails(String propertyId) {
        Intent intent = new Intent(getActivity(), showProperty.class);
        intent.putExtra("address", propertyId);
        startActivity(intent);
    }
}
