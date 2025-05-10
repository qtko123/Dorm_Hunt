package com.example.dormhunt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dormhunt.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private CheckBox showPasswordCheckbox;
    private MaterialButton btnLogin;
    private TextView registerLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        initializeViews();
        setListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        btnLogin = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.registerLink);
    }

    private void setListeners() {
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordInput.setTransformationMethod(null);
            } else {
                passwordInput.setTransformationMethod(new PasswordTransformationMethod());
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                loginUser(email, password);
            }
        });
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void loginUser(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    progressDialog.dismiss();
                                    if (documentSnapshot.exists()) {
                                        String userRole = documentSnapshot.getString("role");
                                        if ("Owner".equals(userRole)) {
                                            if (documentSnapshot.contains("subscriptionPlan")) {
                                                handleLoginSuccess(userId, userRole);
                                            } else {
                                                sessionManager.saveUserId(userId);
                                                Intent intent = new Intent(LoginActivity.this, PricingPlansActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {
                                            handleLoginSuccess(userId, userRole);
                                        }
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "User account not found",
                                                Toast.LENGTH_SHORT).show();
                                        btnLogin.setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    mAuth.signOut();
                                    Toast.makeText(LoginActivity.this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    btnLogin.setEnabled(true);
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                    }
                });
    }

    private void handleLoginSuccess(String userId, String userRole) {
        sessionManager.saveUserId(userId);
        Intent intent;
        if (userRole.equals("Student")) {
            intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
        } else if (userRole.equals("Owner")) {
            intent = new Intent(LoginActivity.this, OwnerHomeActivity.class);
        } else {
            Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            btnLogin.setEnabled(true);
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}