package com.aditi.heartsyncstart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private CardView userCard;
    private TextView tvUserName, tvUserAge, tvUserBio;
    private FloatingActionButton btnLike, btnPass;
    private LinearLayout noUsersLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    private List<User> userList = new ArrayList<>();
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

            // Initialize Views
            userCard = view.findViewById(R.id.userCard);
            tvUserName = view.findViewById(R.id.tvUserName);
            tvUserAge = view.findViewById(R.id.tvUserAge);
            tvUserBio = view.findViewById(R.id.tvUserBio);
            btnLike = view.findViewById(R.id.btnLike);
            btnPass = view.findViewById(R.id.btnPass);
            noUsersLayout = view.findViewById(R.id.noUsersLayout);

            // Load users
            loadUsers();

            // Button Click Listeners
            btnLike.setOnClickListener(v -> likeUser());
            btnPass.setOnClickListener(v -> passUser());

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return view;
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);

                        // Don't show current user
                        if (user != null && user.getUserId() != null &&
                                !user.getUserId().equals(currentUserId)) {
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

            userCard.setVisibility(View.VISIBLE);
            noUsersLayout.setVisibility(View.GONE);
        } else {
            showNoUsersMessage();
        }
    }

    private void likeUser() {
        if (currentUserIndex < userList.size()) {
            User likedUser = userList.get(currentUserIndex);

            Toast.makeText(getContext(), "Liked " + likedUser.getName() + " ❤️", Toast.LENGTH_SHORT).show();

            // Move to next user
            currentUserIndex++;
            displayCurrentUser();
        }
    }

    private void passUser() {
        if (currentUserIndex < userList.size()) {
            User passedUser = userList.get(currentUserIndex);
            Toast.makeText(getContext(), "Passed " + passedUser.getName(), Toast.LENGTH_SHORT).show();

            // Move to next user
            currentUserIndex++;
            displayCurrentUser();
        }
    }

    private void showNoUsersMessage() {
        if (userCard != null && noUsersLayout != null) {
            userCard.setVisibility(View.GONE);
            noUsersLayout.setVisibility(View.VISIBLE);
        }
    }
}