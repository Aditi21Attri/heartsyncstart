package com.aditi.heartsyncstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout noMessagesLayout;
    private ConversationsAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, chatsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("Users");
            chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        }

        // Initialize Views
        rvConversations = view.findViewById(R.id.rvConversations);
        noMessagesLayout = view.findViewById(R.id.noMessagesLayout);

        // Setup RecyclerView
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ConversationsAdapter(conversationList, this::openChat);
        rvConversations.setAdapter(adapter);

        // Load conversations
        loadConversations();

        return view;
    }

    private void loadConversations() {
        // Get all matches first
        usersRef.child(currentUserId).child("matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversationList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showNoMessages();
                    return;
                }

                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    loadConversation(matchedUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load conversations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadConversation(String matchedUserId) {
        // Load user info
        usersRef.child(matchedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    // Create chat ID
                    String chatId = currentUserId.compareTo(matchedUserId) < 0 ?
                            currentUserId + "_" + matchedUserId :
                            matchedUserId + "_" + currentUserId;

                    // Load last message
                    loadLastMessage(chatId, user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadLastMessage(String chatId, User user) {
        chatsRef.child(chatId).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMessage = "Start a conversation";
                long timestamp = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                        Message message = msgSnapshot.getValue(Message.class);
                        if (message != null) {
                            lastMessage = message.getText();
                            timestamp = message.getTimestamp();
                        }
                    }
                }

                Conversation conversation = new Conversation(user, lastMessage, timestamp);
                conversationList.add(conversation);
                adapter.notifyDataSetChanged();
                showConversations();
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

    private void showConversations() {
        rvConversations.setVisibility(View.VISIBLE);
        noMessagesLayout.setVisibility(View.GONE);
    }

    private void showNoMessages() {
        rvConversations.setVisibility(View.GONE);
        noMessagesLayout.setVisibility(View.VISIBLE);
    }
}