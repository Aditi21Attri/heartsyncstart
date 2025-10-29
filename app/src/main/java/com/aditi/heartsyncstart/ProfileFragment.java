package com.aditi.heartsyncstart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvName, tvEmail, tvAge, tvBio, tvGender;
    private Button btnLogout, btnEditProfile, btnUploadPhoto, btnSettings;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userId;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");
        }

        // Initialize Views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAge = view.findViewById(R.id.tvAge);
        tvBio = view.findViewById(R.id.tvBio);
        tvGender = view.findViewById(R.id.tvGender);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        btnSettings = view.findViewById(R.id.btnSettings);

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadProfileImage(imageUri);
                    }
                });

        // Load user data
        loadUserData();

        // Upload photo button
        btnUploadPhoto.setOnClickListener(v -> openImagePicker());

        // Logout button click
        btnLogout.setOnClickListener(v -> logoutUser());

        // Settings button
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        // Edit profile button click
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        tvName.setText(user.getName());
                        tvEmail.setText(user.getEmail());
                        tvAge.setText("Age: " + user.getAge());
                        tvBio.setText(user.getBio());
                        tvGender.setText("Gender: " + user.getGender());

                        // Load profile image
                        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                            Glide.with(ProfileFragment.this)
                                    .load(user.getImageUrl())
                                    .circleCrop()
                                    .placeholder(R.drawable.heartsync_logo)
                                    .into(ivProfilePicture);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (imageUri == null) return;

        // Show loading
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

        // Create unique filename
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        // Upload to Firebase Storage
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Save URL to database
                        databaseReference.child("imageUrl").setValue(imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();

                                    // Load new image
                                    Glide.with(ProfileFragment.this)
                                            .load(imageUrl)
                                            .circleCrop()
                                            .into(ivProfilePicture);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}