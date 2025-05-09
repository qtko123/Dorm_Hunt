package com.example.dormhunt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText fullNameInput, emailInput, contactInput, 
                            passwordInput, confirmPasswordInput;
    private RadioGroup roleRadioGroup;
    private MaterialButton btnRegister;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        
        // Set click listeners
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    registerUser();
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close registration activity
            }
        });
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        contactInput = findViewById(R.id.contactNumberInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        btnRegister = findViewById(R.id.btnRegister);
        loginLink = findViewById(R.id.loginLink);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Full Name
        String fullName = fullNameInput.getText().toString().trim();
        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            isValid = false;
        }

        // Validate Email
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            isValid = false;
        }

        // Validate Contact Number
        String contact = contactInput.getText().toString().trim();
        if (contact.isEmpty()) {
            contactInput.setError("Contact number is required");
            isValid = false;
        }

        // Validate Password
        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Validate Confirm Password
        String confirmPassword = confirmPasswordInput.getText().toString();
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordInput.setError("Passwords do not match");
            isValid = false;
        }

        // Validate Role Selection
        if (roleRadioGroup.getCheckedRadioButtonId() == -1) {
            // No role selected
            isValid = false;
            // Show error message
            // You can add a TextView for error message or show a Toast
        }

        return isValid;
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String fullName = fullNameInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        
        int selectedId = roleRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRole = findViewById(selectedId);
        String userRole = selectedRole.getText().toString();

        // Create confirmation dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Registration")
            .setMessage("Please verify your details:\n\n" +
                    "Full Name: " + fullName + "\n" +
                    "Email: " + email + "\n" +
                    "Contact: " + contact + "\n" +
                    "Role: " + userRole)
            .setPositiveButton("Confirm", (dialog, which) -> {
                // Proceed with registration
                createFirebaseUser(email, password, fullName, contact, userRole);
            })
            .setNegativeButton("Edit", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    private void createFirebaseUser(String email, String password, String fullName, 
                                  String contact, String userRole) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", fullName);
                        user.put("email", email);
                        user.put("contact", contact);
                        user.put("role", userRole);
                        user.put("createdAt", System.currentTimeMillis());

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    showSuccessDialog();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, 
                                            "Error: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, 
                                "Registration failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Registration Successful!")
            .setMessage("Your account has been created successfully.")
            .setPositiveButton("Login", (dialog, which) -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setCancelable(false)
            .show();
    }
}