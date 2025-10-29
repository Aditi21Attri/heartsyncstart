package com.aditi.heartsyncstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {

    private RecyclerView rvMatches;
    private LinearLayout noMatchesLayout;
    private MatchesAdapter adapter;
    private List<User> matchedUsers = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("Users");
        }

        // Initialize Views
        rvMatches = view.findViewById(R.id.rvMatches);
        noMatchesLayout = view.findViewById(R.id.noMatchesLayout);

        // Setup RecyclerView
        rvMatches.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MatchesAdapter(matchedUsers, this::openChat);
        rvMatches.setAdapter(adapter);

        // Load matches
        loadMatches();

        return view;
    }

    private void loadMatches() {
        usersRef.child(currentUserId).child("matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUsers.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showNoMatches();
                    return;
                }

                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    loadMatchedUser(matchedUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load matches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMatchedUser(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    matchedUsers.add(user);
                    adapter.notifyDataSetChanged();
                    showMatches();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void openChat(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("userId", user.getUserId());
        intent.putExtra("userName", user.getName());
        startActivity(intent);
    }

    private void showMatches() {
        rvMatches.setVisibility(View.VISIBLE);
        noMatchesLayout.setVisibility(View.GONE);
    }

    private void showNoMatches() {
        rvMatches.setVisibility(View.GONE);
        noMatchesLayout.setVisibility(View.VISIBLE);
    }
}