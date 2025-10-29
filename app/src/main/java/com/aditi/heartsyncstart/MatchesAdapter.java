package com.aditi.heartsyncstart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<User> matchedUsers;
    private OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(User user);
    }

    public MatchesAdapter(List<User> matchedUsers, OnMatchClickListener listener) {
        this.matchedUsers = matchedUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        User user = matchedUsers.get(position);
        holder.tvName.setText(user.getName());
        holder.tvAge.setText(user.getAge() + " years old");
        holder.tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ?
                user.getBio() : "No bio");

        holder.cardView.setOnClickListener(v -> listener.onMatchClick(user));
    }

    @Override
    public int getItemCount() {
        return matchedUsers.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvAge, tvBio;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.matchCard);
            tvName = itemView.findViewById(R.id.tvMatchName);
            tvAge = itemView.findViewById(R.id.tvMatchAge);
            tvBio = itemView.findViewById(R.id.tvMatchBio);
        }
    }
}