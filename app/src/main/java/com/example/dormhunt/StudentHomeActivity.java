package com.example.dormhunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dormhunt.adapters.StudentDormAdapter;
import com.example.dormhunt.models.Dorm;
import com.example.dormhunt.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StudentHomeActivity extends AppCompatActivity implements StudentDormAdapter.OnDormClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView dormsRecyclerView;
    private StudentDormAdapter adapter;
    private SessionManager sessionManager;
    private List<Dorm> allDorms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        setupToolbar();
        setupRecyclerView();
        loadDorms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Dorms");
        }
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white, getTheme()));
    }

    private void setupRecyclerView() {
        dormsRecyclerView = findViewById(R.id.dormsRecyclerView);
        dormsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentDormAdapter(this, this);
        dormsRecyclerView.setAdapter(adapter);
    }

    private void loadDorms() {
        allDorms.clear();
        db.collection("dorms")
            .whereEqualTo("isAvailable", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Dorm dorm = document.toObject(Dorm.class);
                    if (dorm != null) {
                        dorm.setId(document.getId());
                        allDorms.add(dorm);
                    }
                }
                adapter.updateDorms(allDorms);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading dorms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onDormClick(Dorm dorm) {
        Intent intent = new Intent(this, DormDetailsActivity.class);
        intent.putExtra("dormId", dorm.getId());
        startActivity(intent);
    }
}