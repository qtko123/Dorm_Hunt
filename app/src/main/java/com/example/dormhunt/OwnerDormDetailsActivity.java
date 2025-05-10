package com.example.dormhunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.example.dormhunt.models.Dorm;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerDormDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OwnerDormDetailsActivity";

    private ImageView dormImage;
    private TextView dormName, dormPrice, dormLocation, dormDescription, occupancyText;

    private LinearLayout inclusionsContainer;
    private Button editInclusionsButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dorm_details);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        dormImage = findViewById(R.id.dormImage);
        dormName = findViewById(R.id.dormName);
        dormPrice = findViewById(R.id.dormPrice);
        dormLocation = findViewById(R.id.dormLocation);
        dormDescription = findViewById(R.id.dormDescription);
        occupancyText = findViewById(R.id.occupancyText);
        inclusionsContainer = findViewById(R.id.inclusionsContainer);
        editInclusionsButton = findViewById(R.id.editInclusionsButton);

        // Get dormId from intent extras
        String dormId = getIntent().getStringExtra("dormId");

        if (dormId != null) {
            fetchDormDetails(dormId);
        } else {
            Toast.makeText(this, "Dorm ID not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no dorm ID
        }
    }

    private void fetchDormDetails(String dormId) {
        db.collection("dorms").document(dormId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Dorm dorm = documentSnapshot.toObject(Dorm.class);
                        if (dorm != null) {
                            displayDormDetails(dorm);
                        }
                    } else {
                        Toast.makeText(this, "Dorm not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching dorm details", e);
                    Toast.makeText(this, "Error loading dorm details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayDormDetails(Dorm dorm) {
        // Load image using Glide
        if (dorm.getImageUrl() != null && !dorm.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(dorm.getImageUrl())
                    .placeholder(R.drawable.default_dorm_image) // Use your placeholder
                    .error(R.drawable.default_dorm_image) // Use your error image
                    .into(dormImage);
        } else {
            // Set a default image or hide the ImageView if no image URL
            dormImage.setImageResource(R.drawable.default_dorm_image);
        }

        // Set text views
        dormName.setText(dorm.getName());
        dormPrice.setText(String.format("₱%.2f", dorm.getPrice()));
        dormLocation.setText(dorm.getLocation());
        dormDescription.setText(dorm.getDescription());

        // Calculate and display occupancy
        int availableRooms = dorm.getMaxOccupants() - dorm.getCurrentOccupants();
        occupancyText.setText(String.format("%d/%d occupied (%d available)",
                dorm.getCurrentOccupants(), dorm.getMaxOccupants(), availableRooms));

        // Display inclusions
        inclusionsContainer.removeAllViews(); // Clear any existing views
        if (dorm.getInclusions() != null) {
            for (String inclusion : dorm.getInclusions()) {
                TextView inclusionTextView = new TextView(this);
                inclusionTextView.setText(inclusion);
                // Add some basic styling (optional)
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.inclusion_item_margin_bottom));
                inclusionTextView.setLayoutParams(layoutParams);
                inclusionsContainer.addView(inclusionTextView);
            }
        }

        // Set up Edit Inclusions button click listener
        editInclusionsButton.setOnClickListener(v -> {
            // TODO: Implement logic to edit inclusions (e.g., show a dialog or new activity)
            Toast.makeText(this, "Edit Inclusions clicked", Toast.LENGTH_SHORT).show();
        });
    }
}