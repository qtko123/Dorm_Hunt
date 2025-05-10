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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.dormhunt.utils.SessionManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDormBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText dormNameInput, descriptionInput, locationInput, priceInput;
    private Button uploadPhotoButton;
    private ChipGroup amenitiesChipGroup;
    private List<String> availableAmenities = Arrays.asList(

        "Wi-Fi", "Air Conditioning", "Study Area", "Kitchen",
        "Laundry", "Security", "CCTV", "Parking"
    );
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private Button submitButton;
    private ImageView dormImageView;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_dorm, container, false);
        dormNameInput = view.findViewById(R.id.dormNameInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        locationInput = view.findViewById(R.id.locationInput);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        priceInput = view.findViewById(R.id.priceInput);
        amenitiesChipGroup = view.findViewById(R.id.amenitiesChipGroup);
        submitButton = view.findViewById(R.id.submitButton);
        dormImageView = view.findViewById(R.id.dormImageView);

        uploadPhotoButton.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });
        
        setupAmenityChips();
        
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveDormToFirestore();
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(getContext(), "Permission denied to read storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            } else {
                openImagePicker();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                dormImageView.setImageURI(selectedImageUri);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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

        submitButton.setEnabled(false);
        submitButton.setText("Adding dorm...");

        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child("dorm_images/" + System.currentTimeMillis() + ".jpg");
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDormDataToFirestore(ownerId, uri.toString());
                    }).addOnFailureListener(e -> {
                        handleFailure("Failed to get image download URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    handleFailure("Image upload failed: " + e.getMessage());
                });
        } else {
            saveDormDataToFirestore(ownerId, null);
        }
    }

    private void saveDormDataToFirestore(String ownerId, String imageUrl) {
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
        if (imageUrl != null) {
            dorm.put("imageUrl", imageUrl);
        }

        db.collection("dorms").add(dorm)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Dorm added successfully", Toast.LENGTH_SHORT).show();
                dismiss(); // Dismiss the bottom sheet on success
            })
            .addOnFailureListener(e -> {
                submitButton.setEnabled(true);
                submitButton.setText("Add Dorm");
                Toast.makeText(getContext(), "Error adding dorm: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void handleFailure(String message) {
        submitButton.setEnabled(true);
        submitButton.setText("Add Dorm");
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}