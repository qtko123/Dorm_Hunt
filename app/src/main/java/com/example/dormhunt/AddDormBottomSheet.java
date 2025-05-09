package com.example.dormhunt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.dormhunt.utils.SessionManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDormBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText dormNameInput, descriptionInput, locationInput, priceInput;
    private ChipGroup amenitiesChipGroup;
    private List<String> availableAmenities = Arrays.asList(
        "Wi-Fi", "Air Conditioning", "Study Area", "Kitchen",
        "Laundry", "Security", "CCTV", "Parking"
    );
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private Button submitButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_dorm, container, false);
        dormNameInput = view.findViewById(R.id.dormNameInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        locationInput = view.findViewById(R.id.locationInput);
        priceInput = view.findViewById(R.id.priceInput);
        amenitiesChipGroup = view.findViewById(R.id.amenitiesChipGroup);
        submitButton = view.findViewById(R.id.submitButton);

        setupAmenityChips();
        
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveDormToFirestore();
            }
        });

        return view;
    }

    private void setupAmenityChips() {
        for (String amenity : availableAmenities) {
            Chip chip = new Chip(requireContext());
            chip.setText(amenity);
            chip.setCheckable(true);
            amenitiesChipGroup.addView(chip);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(dormNameInput.getText())) {
            dormNameInput.setError("Required");
            isValid = false;
        }
        if (TextUtils.isEmpty(descriptionInput.getText())) {
            descriptionInput.setError("Required");
            isValid = false;
        }
        if (TextUtils.isEmpty(locationInput.getText())) {
            locationInput.setError("Required");
            isValid = false;
        }
        if (TextUtils.isEmpty(priceInput.getText())) {
            priceInput.setError("Required");
            isValid = false;
        }

        return isValid;
    }

    private List<String> getSelectedAmenities() {
        List<String> selectedAmenities = new ArrayList<>();
        for (int i = 0; i < amenitiesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) amenitiesChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedAmenities.add(chip.getText().toString());
            }
        }
        return selectedAmenities;
    }

    private void saveDormToFirestore() {
        String ownerId = sessionManager.getUserId();
        if (ownerId == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dorm = new HashMap<>();
        dorm.put("ownerId", ownerId);
        dorm.put("name", dormNameInput.getText().toString());
        dorm.put("description", descriptionInput.getText().toString());
        dorm.put("location", locationInput.getText().toString());
        dorm.put("price", Double.parseDouble(priceInput.getText().toString()));
        dorm.put("inclusions", getSelectedAmenities());
        dorm.put("maxOccupants", 4);
        dorm.put("currentOccupants", 0);
        dorm.put("isAvailable", true);
        dorm.put("createdAt", FieldValue.serverTimestamp());

        submitButton.setEnabled(false);
        submitButton.setText("Adding dorm...");

        db.collection("dorms")
            .add(dorm)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Dorm added successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            })
            .addOnFailureListener(e -> {
                submitButton.setEnabled(true);
                submitButton.setText("Add Dorm");
                Toast.makeText(getContext(), "Error adding dorm: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}