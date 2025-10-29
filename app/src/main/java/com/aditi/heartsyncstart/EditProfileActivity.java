package com.aditi.heartsyncstart;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etAge, etBio;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSaveProfile, btnCancel;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        // Initialize Views
        etName = findViewById(R.id.etEditName);
        etAge = findViewById(R.id.etEditAge);
        etBio = findViewById(R.id.etEditBio);
        rgGender = findViewById(R.id.rgEditGender);
        rbMale = findViewById(R.id.rbEditMale);
        rbFemale = findViewById(R.id.rbEditFemale);
        rbOther = findViewById(R.id.rbEditOther);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancelEdit);

        // Load current user data
        loadUserData();

        // Save button
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    etName.setText(user.getName());
                    etAge.setText(user.getAge());
                    etBio.setText(user.getBio());

                    // Set gender radio button
                    if (user.getGender() != null) {
                        switch (user.getGender()) {
                            case "Male":
                                rbMale.setChecked(true);
                                break;
                            case "Female":
                                rbFemale.setChecked(true);
                                break;
                            case "Other":
                                rbOther.setChecked(true);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender != null ? selectedGender.getText().toString() : "";

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firebase
        userRef.child("name").setValue(name);
        userRef.child("age").setValue(age);
        userRef.child("bio").setValue(bio);
        userRef.child("gender").setValue(gender)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}