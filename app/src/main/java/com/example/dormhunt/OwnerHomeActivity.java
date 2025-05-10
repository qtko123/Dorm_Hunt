package com.example.dormhunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormhunt.adapters.OwnerDormAdapter;
import com.example.dormhunt.adapters.InquiryAdapter;
import com.example.dormhunt.interfaces.OnDormClickListener;
import com.example.dormhunt.models.Dorm;
import com.example.dormhunt.models.Inquiry;
import com.example.dormhunt.utils.SessionManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity implements OwnerDormAdapter.OnDormClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView inquiriesRecyclerView, dormsRecyclerView;
    private TextView pendingCount, approvedCount;
    private ExtendedFloatingActionButton addDormFab;
    private OwnerDormAdapter dormAdapter;
    private InquiryAdapter inquiryAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initializeViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Dorms");

        loadInquiries();
        setupRecyclerView();
        loadOwnerDorms();

        addDormFab.setOnClickListener(view -> {
            AddDormBottomSheet bottomSheet = new AddDormBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddDormBottomSheet");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.owner_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        inquiriesRecyclerView = findViewById(R.id.inquiriesRecyclerView);
        pendingCount = findViewById(R.id.pendingCount);
        approvedCount = findViewById(R.id.approvedCount);
        addDormFab = findViewById(R.id.addDormFab);

        inquiryAdapter = new InquiryAdapter(new InquiryAdapter.OnInquiryActionListener() {
            @Override
            public void onAcceptClick(Inquiry inquiry) {
                handleInquiryAction(inquiry, "approved");
            }

            @Override
            public void onDeclineClick(Inquiry inquiry) {
                handleInquiryAction(inquiry, "declined");
            }
        });
        inquiriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inquiriesRecyclerView.setAdapter(inquiryAdapter);
    }

    private void setupRecyclerView() {
        dormsRecyclerView = findViewById(R.id.dormsRecyclerView);
        dormsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dormAdapter = new OwnerDormAdapter(this, this);
        dormsRecyclerView.setAdapter(dormAdapter);
    }

    private void loadInquiries() {
        String ownerId = mAuth.getCurrentUser().getUid();
        List<Inquiry> inquiryList = new ArrayList<>();
        
        db.collection("inquiries")
            .whereEqualTo("ownerId", ownerId)
            .limit(10)
            .get()
            .addOnSuccessListener(inquirySnapshots -> {
                int pending = 0;
                int approved = 0;
                
                for (DocumentSnapshot inquiryDoc : inquirySnapshots) {
                    Inquiry inquiry = inquiryDoc.toObject(Inquiry.class);
                    if (inquiry != null) {
                        inquiry.setId(inquiryDoc.getId());
                        String userId = inquiryDoc.getString("userId");
                        if (userId != null) {
                            db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        inquiry.setUserName(userDoc.getString("fullName"));
                                        db.collection("dorms")
                                            .document(inquiry.getDormId())
                                            .get()
                                            .addOnSuccessListener(dormDoc -> {
                                                if (dormDoc.exists()) {
                                                    inquiry.setDormName(dormDoc.getString("name"));
                                                    inquiry.setPrice(dormDoc.getDouble("price"));
                                                    inquiryList.add(inquiry);
                                                    inquiryAdapter.updateInquiries(inquiryList);
                                                }
                                            });
                                    }
                                });
                        }
                        String status = inquiry.getStatus();
                        if ("pending".equals(status)) pending++;
                        else if ("approved".equals(status)) approved++;
                    }
                }
                pendingCount.setText(String.valueOf(pending));
                approvedCount.setText(String.valueOf(approved));
            })
            .addOnFailureListener(e -> {
                Toast.makeText(OwnerHomeActivity.this, "Error loading inquiries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadOwnerDorms() {
        String ownerId = sessionManager.getUserId();
        if (ownerId == null) return;

        db.collection("dorms")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading dorms", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Dorm> dorms = new ArrayList<>();
                if (value != null) {
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Dorm dorm = doc.toObject(Dorm.class);
                        if (dorm != null) {
                            dorm.setId(doc.getId());
                            dorms.add(dorm);
                        }
                    }
                }
                dormAdapter.updateDorms(dorms);
            });
    }

    private void updateCounters() {
        String ownerId = mAuth.getCurrentUser().getUid();
        db.collection("bookings")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int pending = 0;
                int approved = 0;
                
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String status = document.getString("status");
                    if ("pending".equals(status)) {
                        pending++;
                    } else if ("approved".equals(status)) {
                        approved++;
                    }
                }
                pendingCount.setText(String.valueOf(pending));
                approvedCount.setText(String.valueOf(approved));
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading counts", Toast.LENGTH_SHORT).show();
            });
    }

    private void handleInquiryAction(Inquiry inquiry, String newStatus) {
        if ("approved".equals(newStatus)) {
            DocumentReference dormRef = db.collection("dorms")
                .document(inquiry.getDormId());

            db.runTransaction(transaction -> {
                DocumentSnapshot dormSnapshot = transaction.get(dormRef);
                if (!dormSnapshot.exists()) {
                    return "Dorm does not exist!";
                }

                Long currentOccupants = dormSnapshot.getLong("currentOccupants");
                Long maxOccupants = dormSnapshot.getLong("maxOccupants");

                if (currentOccupants == null || maxOccupants == null) {
                    return "Invalid occupancy data!";
                }

                if (currentOccupants >= maxOccupants) {
                    return "Dorm is already full!";
                }

                transaction.update(dormRef, "currentOccupants", currentOccupants + 1);
                transaction.update(db.collection("inquiries").document(inquiry.getId()), 
                    "status", newStatus);
                return null;
            }).addOnSuccessListener(result -> {
                if (result == null) {
                    Toast.makeText(this, "Inquiry approved and occupancy updated", Toast.LENGTH_SHORT).show();
                    loadInquiries();
                    loadOwnerDorms();
                } else {
                    Toast.makeText(this, "Error: " + result, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            db.collection("inquiries")
                .document(inquiry.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Inquiry declined", Toast.LENGTH_SHORT).show();
                    loadInquiries();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error declining inquiry", Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public void onDormClick(Dorm dorm) {
        Intent intent = new Intent(this, DormDetailsActivity.class);
        intent.putExtra("dormId", dorm.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Dorm dorm) {
    }

    @Override
    public void onDeleteClick(Dorm dorm) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Dorm")
            .setMessage("Are you sure you want to delete this dorm? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteDorm(dorm);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteDorm(Dorm dorm) {
        if (dorm == null || dorm.getId() == null) return;

        // First delete all inquiries related to this dorm
        db.collection("inquiries")
            .whereEqualTo("dormId", dorm.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                WriteBatch batch = db.batch();
                
                // Add inquiry deletions to batch
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    batch.delete(document.getReference());
                }
                
                // Add dorm deletion to batch
                batch.delete(db.collection("dorms").document(dorm.getId()));
                
                // Execute batch
                batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Dorm and related inquiries deleted successfully", 
                            Toast.LENGTH_SHORT).show();
                        loadOwnerDorms(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting dorm: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error finding related inquiries: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}