package com.example.dormhunt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dormhunt.models.Dorm;
import com.example.dormhunt.models.Inquiry;
import com.example.dormhunt.utils.ImageUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class DormDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Dorm currentDorm;
    private String dormId;
    private ImageView dormImage;
    private TextView dormName, dormPrice, dormDescription, dormLocation, occupancyStatus, noImageText;
    private Chip statusChip, genderChip;
    private ViewGroup amenitiesContainer, inclusionsContainer;
    private Button inquiryButton;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dorm_details);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dorm Details");

        dormId = getIntent().getStringExtra("dormId");
        if (dormId == null) {
            finish();
            return;
        }
        userRole = getIntent().getStringExtra("userRole");
        if ("student".equals(userRole)) {
            inquiryButton.setVisibility(View.VISIBLE);
        } else if ("owner".equals(userRole)) {
            inquiryButton.setVisibility(View.GONE);
        }

        initializeViews();
        loadDormDetails();

        ExtendedFloatingActionButton inquiryFab = findViewById(R.id.inquiryFab);
        inquiryFab.setOnClickListener(v -> {
            showInquiryDialog();
        });
    }

    private void initializeViews() {
        dormImage = findViewById(R.id.dormImage);
        dormName = findViewById(R.id.dormName);
        dormPrice = findViewById(R.id.dormPrice);
        dormDescription = findViewById(R.id.dormDescription);
        dormLocation = findViewById(R.id.dormLocation);
        occupancyStatus = findViewById(R.id.occupancyStatus);
        statusChip = findViewById(R.id.statusChip);
        genderChip = findViewById(R.id.genderChip);
        amenitiesContainer = findViewById(R.id.amenitiesContainer);
        inclusionsContainer = findViewById(R.id.inclusionsContainer);
        noImageText = findViewById(R.id.noImageText);
    }

    private void loadDormDetails() {
        db.collection("dorms").document(dormId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Dorm dorm = documentSnapshot.toObject(Dorm.class);
                if (dorm != null) {
                    currentDorm = dorm;
                    dorm.setId(documentSnapshot.getId());
                    String ownerId = dorm.getOwnerId();
                    if (ownerId != null) {
                        db.collection("users").document(ownerId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String ownerName = userDoc.getString("fullName");
                                    dorm.setOwnerName(ownerName);
                                }
                                displayDormDetails(dorm);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error loading owner details", Toast.LENGTH_SHORT).show();
                                displayDormDetails(dorm);
                            });
                    } else {
                        displayDormDetails(dorm);
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading dorm details", Toast.LENGTH_SHORT).show());
    }

    private void displayDormDetails(Dorm dorm) {
        dormName.setText(dorm.getName());
        dormPrice.setText(String.format(Locale.getDefault(), "₱%.2f", dorm.getPrice()));
        dormDescription.setText(dorm.getDescription());
        dormLocation.setText(dorm.getLocation());
        TextView viewCount = findViewById(R.id.occupancyStatus);
        viewCount.setText("120 views");
        viewCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility, 0, 0, 0);
        viewCount.setCompoundDrawablePadding(8);

        statusChip.setText(dorm.getAvailabilityStatus());
        statusChip.setChipBackgroundColorResource(
            dorm.isAvailable() ? R.color.available_color : R.color.unavailable_color);
        if (dorm.getImagePath() != null) {
            Bitmap bitmap = ImageUtils.loadImage(this, dorm.getImagePath());
            if (bitmap != null) {
                dormImage.setImageBitmap(bitmap);
                noImageText.setVisibility(View.GONE);
            } else {
                setupNoImagePlaceholder();
            }
        } else {
            setupNoImagePlaceholder();
        }
        if (dorm.getAmenities() != null) {
            for (String amenity : dorm.getAmenities()) {
                addChipToContainer(amenity, amenitiesContainer);
            }
        }
        if (dorm.getInclusions() != null) {
            for (String inclusion : dorm.getInclusions()) {
                addChipToContainer(inclusion, inclusionsContainer);
            }
        }
    }

    private void setupNoImagePlaceholder() {
        dormImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        dormImage.setBackgroundColor(getColor(R.color.grey));
        dormImage.setImageResource(R.drawable.ic_noimage);
        noImageText.setVisibility(View.VISIBLE);
    }

    private void addChipToContainer(String text, ViewGroup container) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(false);
        container.addView(chip);
    }

    private void showInquiryDialog() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_send_inquiry, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        EditText messageInput = bottomSheetView.findViewById(R.id.messageInput);
        RadioGroup paymentMethodGroup = bottomSheetView.findViewById(R.id.paymentMethodGroup);
        Button sendButton = bottomSheetView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            int selectedId = paymentMethodGroup.getCheckedRadioButtonId();

            if (message.isEmpty()) {
                messageInput.setError("Please enter a message");
                return;
            }

            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = bottomSheetView.findViewById(selectedId);
            String paymentMethod = selectedRadioButton.getText().toString();

            sendInquiry(currentDorm, message, paymentMethod);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void sendInquiry(Dorm dorm, String message, String paymentMethod) {
        String userId = mAuth.getCurrentUser().getUid();
        String inquiryId = db.collection("inquiries").document().getId();

        Inquiry inquiry = new Inquiry();
        inquiry.setId(inquiryId);
        inquiry.setUserId(userId);
        inquiry.setDormId(dorm.getId());
        inquiry.setDormName(dorm.getName());
        inquiry.setOwnerId(dorm.getOwnerId());
        inquiry.setOwnerName(dorm.getOwnerName());
        inquiry.setStatus("pending");
        inquiry.setPrice(dorm.getPrice());
        inquiry.setPaymentMethod(paymentMethod);
        inquiry.setMessage(message);
        inquiry.setTimestamp(Timestamp.now());

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(userDoc -> {
                inquiry.setUserName(userDoc.getString("fullName"));

                db.collection("inquiries").document(inquiryId)
                    .set(inquiry)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Inquiry sent successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send inquiry: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error getting user data: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}