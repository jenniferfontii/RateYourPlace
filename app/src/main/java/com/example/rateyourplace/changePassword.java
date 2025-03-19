package com.example.rateyourplace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class changePassword extends DialogFragment {
    //Set Global variables
    private FirebaseAuth auth;
    private EditText password, confirmPassword;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Links layout to xml file
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_change_password, null);

        //Initializes variables
        auth = FirebaseAuth.getInstance();
        password = view.findViewById(R.id.newPassword);
        confirmPassword = view.findViewById(R.id.confirmPassword);

        //Creates the dialog
        builder.setView(view)
                .setTitle("Change Password")
                .setPositiveButton("Save", (dialog, id) -> {
                    //If successful changes password
                    changeUserPassword();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void changeUserPassword() {
        //From the dialog gets the new and confirmed password
        String newPassword = password.getText().toString().trim();
        String confirmPassword = this.confirmPassword.getText().toString().trim();

        //If all the fields are not filled an error message shows
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //If passwords don't match an error shows
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        //Gets logged in user
        FirebaseUser user = auth.getCurrentUser();

        //Changes password
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
