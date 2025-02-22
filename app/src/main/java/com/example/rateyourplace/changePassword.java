package com.example.rateyourplace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class changePassword extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_change_password, null);

        builder.setView(view)
                .setTitle("Change Password")
                .setPositiveButton("Save", (dialog, id) -> dismiss())
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());
        return builder.create();
    }
}