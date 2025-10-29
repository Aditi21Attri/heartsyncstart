package com.aditi.heartsyncstart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private CardView userCard;
    private TextView tvUserName, tvUserAge, tvUserBio;
    private FloatingActionButton btnLike, btnPass;
    private LinearLayout noUsersLayout;
    private ImageView ivUserImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, currentUserRef;
    private String currentUserId;

    private List<User> userList = new ArrayList<>();
    private Map<String, Boolean> likedUserIds = new HashMap<>();
    private Map<String, Boolean> passedUserIds = new HashMap<>();
    private int currentUserIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
                return view;
            }

            currentUserId = mAuth.getCurrentUser().getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("Users");
            currentUserRef = usersRef.child(currentUserId);

            // Initialize Views
            userCard = view.findViewById(R.id.userCard);
            tvUserName = view.findViewById(R.id.tvUserName);
            tvUserAge = view.findViewById(R.id.tvUserAge);
            tvUserBio = view.findViewById(R.id.tvUserBio);
            btnLike = view.findViewById(R.id.btnLike);
            btnPass = view.findViewById(R.id.btnPass);
            noUsersLayout = view.findViewById(R.id.noUsersLayout);
            ivUserImage = view.findViewById(R.id.ivUserImage);

            // Load current user's liked/passed list first, then load users
            loadCurrentUserData();

            // Button Click Listeners
            btnLike.setOnClickListener(v -> likeUser());
            btnPass.setOnClickListener(v -> passUser());

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return view;
    }

    private void loadCurrentUserData() {
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load liked users
                if (snapshot.child("likedUsers").exists()) {
                    for (DataSnapshot likedSnapshot : snapshot.child("likedUsers").getChildren()) {
                        likedUserIds.put(likedSnapshot.getKey(), true);
                    }
                }

                // Load passed users
                if (snapshot.child("passedUsers").exists()) {
                    for (DataSnapshot passedSnapshot : snapshot.child("passedUsers").getChildren()) {
                        passedUserIds.put(passedSnapshot.getKey(), true);
                    }
                }

                // Now load all users
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);

                        // Filter logic:
                        // 1. Don't show current user
                        // 2. Don't show already liked users
                        // 3. Don't show already passed users
                        if (user != null && user.getUserId() != null &&
                                !user.getUserId().equals(currentUserId) &&
                                !likedUserIds.containsKey(user.getUserId()) &&
                                !passedUserIds.containsKey(user.getUserId())) {
                            userList.add(user);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (userList.isEmpty()) {
                    showNoUsersMessage();
                } else {
                    displayCurrentUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCurrentUser() {
        if (currentUserIndex < userList.size()) {
            User user = userList.get(currentUserIndex);

            tvUserName.setText(user.getName() != null ? user.getName() : "Unknown");
            tvUserAge.setText(user.getAge() != null ? user.getAge() : "N/A");
            tvUserBio.setText(user.getBio() != null && !user.getBio().isEmpty() ?
                    user.getBio() : "No bio available");

            // Load user image
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.heartsync_logo)
                        .into(ivUserImage);
            } else {
                ivUserImage.setImageResource(R.drawable.heartsync_logo);
            }

            userCard.setVisibility(View.VISIBLE);
            noUsersLayout.setVisibility(View.GONE);
        } else {
            showNoUsersMessage();
        }
    }

    private void likeUser() {
        if (currentUserIndex < userList.size()) {
            User likedUser = userList.get(currentUserIndex);

            // ✅ SAVE LIKE TO FIREBASE
            currentUserRef.child("likedUsers").child(likedUser.getUserId()).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        // Add to local list so it won't appear again
                        likedUserIds.put(likedUser.getUserId(), true);

                        // Check for match
                        checkForMatch(likedUser);

                        Toast.makeText(getContext(), "Liked " + likedUser.getName() + " ❤️", Toast.LENGTH_SHORT).show();

                        // Move to next user
                        currentUserIndex++;
                        displayCurrentUser();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save like", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void passUser() {
        if (currentUserIndex < userList.size()) {
            User passedUser = userList.get(currentUserIndex);

            // ✅ SAVE PASS TO FIREBASE (so they don't appear again)
            currentUserRef.child("passedUsers").child(passedUser.getUserId()).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        // Add to local list
                        passedUserIds.put(passedUser.getUserId(), true);

                        Toast.makeText(getContext(), "Passed " + passedUser.getName(), Toast.LENGTH_SHORT).show();

                        // Move to next user
                        currentUserIndex++;
                        displayCurrentUser();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save pass", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkForMatch(User likedUser) {
        // Check if the other user has also liked you
        usersRef.child(likedUser.getUserId()).child("likedUsers").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // ✅ IT'S A MATCH!
                            createMatch(likedUser);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void createMatch(User matchedUser) {
        // Add to both users' matches in Firebase
        currentUserRef.child("matches").child(matchedUser.getUserId()).setValue(true);
        usersRef.child(matchedUser.getUserId()).child("matches").child(currentUserId).setValue(true);

        // Show match notification
        Toast.makeText(getContext(),
                "🎉 IT'S A MATCH with " + matchedUser.getName() + "! 💕\nCheck your Matches tab!",
                Toast.LENGTH_LONG).show();
    }

    private void showNoUsersMessage() {
        if (userCard != null && noUsersLayout != null) {
            userCard.setVisibility(View.GONE);
            noUsersLayout.setVisibility(View.VISIBLE);
        }
    }
}