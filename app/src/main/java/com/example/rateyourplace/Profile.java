package com.example.rateyourplace;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
// Helper for authentication things
public class Profile {
    public static void loadProfilePicture(Context context, ImageView imageView) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();

        if (userId == null) {
            Log.e("ProfilePictureDebug", "User ID is null!");
            return;
        }

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileName = documentSnapshot.getString("profile_picture");
                        if (fileName != null) {
                            File profileImageFile = new File(context.getFilesDir(), fileName);
                            if (profileImageFile.exists()) {
                                Uri profileImageUri = Uri.fromFile(profileImageFile);
                                imageView.setImageURI(profileImageUri);
                                Log.d("ProfilePictureDebug", "Loaded image from: " + profileImageFile.getAbsolutePath());
                            } else {
                                Log.e("ProfilePictureDebug", "File does not exist: " + profileImageFile.getAbsolutePath());
                            }
                        } else {
                            Log.e("ProfilePictureDebug", "No profile_picture field found in Firestore.");
                        }
                    } else {
                        Log.e("ProfilePictureDebug", "Firestore document does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfilePictureDebug", "Error fetching Firestore document: " + e.getMessage()));
    }
}
