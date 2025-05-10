package com.example.dormhunt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dormhunt.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PricingPlansActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricing_plans);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        MaterialCardView basicPlanCard = findViewById(R.id.basicPlanCard);
        MaterialCardView premiumPlanCard = findViewById(R.id.premiumPlanCard);
        basicPlanCard.setOnClickListener(v -> selectPlan("basic"));
        premiumPlanCard.setOnClickListener(v -> selectPlan("premium"));

        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        if ("Owner".equals(userRole)) {
                            if (documentSnapshot.contains("subscriptionPlan")) {
                                redirectToOwnerHome();
                            }
                        } else {
                            redirectToAppropriateScreen(userRole);
                        }
                    } else {
                        Toast.makeText(this, "User account not found", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking subscription: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void selectPlan(String plan) {
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("subscriptionPlan", plan);
        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Subscription plan selected: " + plan,
                            Toast.LENGTH_SHORT).show();
                    redirectToOwnerHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error selecting plan: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void redirectToOwnerHome() {
        Intent intent = new Intent(this, OwnerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToAppropriateScreen(String userRole) {
        Intent intent;
        if ("Student".equals(userRole)) {
            intent = new Intent(this, StudentHomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}