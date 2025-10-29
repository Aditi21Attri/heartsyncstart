package com.aditi.heartsyncstart;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class ChatActivity extends AppCompatActivity {

    private TextView tvChatUserName;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference messagesRef;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        otherUserId = getIntent().getStringExtra("userId");
        otherUserName = getIntent().getStringExtra("userName");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Create unique chat ID (alphabetically sorted to ensure same ID for both users)
        chatId = currentUserId.compareTo(otherUserId) < 0 ?
                currentUserId + "_" + otherUserId :
                otherUserId + "_" + currentUserId;

        messagesRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);

        // Initialize Views
        tvChatUserName = findViewById(R.id.tvChatUserName);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        tvChatUserName.setText(otherUserName);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(messageList, currentUserId);
        rvMessages.setAdapter(adapter);

        // Load messages
        loadMessages();

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    Message message = msgSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                if (messageList.size() > 0) {
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create message object
        String messageId = messagesRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Message message = new Message(messageId, currentUserId, otherUserId, messageText, timestamp);

        // Save to Firebase
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        etMessage.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}