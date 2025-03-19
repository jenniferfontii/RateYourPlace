package com.example.rateyourplace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class deleteAccount extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Builds dialog
        builder.setTitle("Delete account").setMessage("Are you sure you want to delete your account?").setPositiveButton("Delete", (dialog, id) -> {
            //If delete is pressed calls delete method
            delete();
            dismiss();
        }).setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void delete() {
        //Initializes database and authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        //Checks if user is logged in
        if (auth.getCurrentUser() != null) {
            //Gets userid
            String userId = auth.getCurrentUser().getUid();
            //Delete user from firestore with that userid
            auth.getCurrentUser().delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firestore.collection("users").document(userId).delete().addOnSuccessListener(aVoid -> {
                        auth.signOut();
                        Intent intent = new Intent(getActivity(), login.class);
                        startActivity(intent);
                        getActivity().finish();
                    }).addOnFailureListener(e -> {
                        Log.e("DeleteAccount", "Error deleting user data from Firestore: " + e.getMessage());
                        Toast.makeText(getActivity(), "Failed to delete account data. Please try again.", Toast.LENGTH_SHORT).show();

                    });
                } else {
                    Log.e("DeleteAccount", "Error deleting user from Firebase Authentication: " + task.getException().getMessage());
                    Toast.makeText(getActivity(), "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}

