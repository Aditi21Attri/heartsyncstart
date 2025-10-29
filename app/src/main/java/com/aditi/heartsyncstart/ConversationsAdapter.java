package com.aditi.heartsyncstart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(User user);
    }

    public ConversationsAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        User user = conversation.getUser();

        holder.tvName.setText(user.getName());
        holder.tvLastMessage.setText(conversation.getLastMessage());

        // Load profile image
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.heartsync_logo)
                    .into(holder.ivProfilePic);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.heartsync_logo);
        }

        // Format timestamp
        if (conversation.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String date = sdf.format(new Date(conversation.getTimestamp()));
            holder.tvTime.setText(date);
        } else {
            holder.tvTime.setText("New");
        }

        holder.cardView.setOnClickListener(v -> listener.onConversationClick(user));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProfilePic;
        TextView tvName, tvLastMessage, tvTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.conversationCard);
            ivProfilePic = itemView.findViewById(R.id.ivConversationProfilePic);
            tvName = itemView.findViewById(R.id.tvConversationName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvConversationTime);
        }
    }
}