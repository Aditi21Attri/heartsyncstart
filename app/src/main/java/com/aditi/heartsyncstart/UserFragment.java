package com.aditi.heartsyncstart;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private GestureDetector gestureDetector;
    private List<User> users;
    private int currentIndex = 0;

    private ImageView userImage;
    private TextView userName, userBio;
    private View card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        card = view.findViewById(R.id.user_card);
        userImage = view.findViewById(R.id.user_image);
        userName = view.findViewById(R.id.user_name);
        userBio = view.findViewById(R.id.user_bio);

        // Buttons
        Button btnNext = view.findViewById(R.id.btn_next);
        Button btnPrev = view.findViewById(R.id.btn_prev);

        // Initialize users
        users = new ArrayList<>();
        users.add(new User("Aditi", 23, "Loves coding, coffee, and travel 🌍", R.drawable.sample_user));
        users.add(new User("Rohan", 25, "Music lover 🎵 and foodie 🍕", R.drawable.sample_user2));

        showUser(currentIndex);

        // GestureDetector
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Button Click Listeners
        btnNext.setOnClickListener(v -> nextUser());
        btnPrev.setOnClickListener(v -> previousUser());

        return view;
    }

    private void showUser(int index) {
        if (index < 0 || index >= users.size()) return;
        User user = users.get(index);
        userName.setText(user.name + ", " + user.age);
        userBio.setText(user.bio);
        userImage.setImageResource(user.imageResId);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) onSwipeRight();
                    else onSwipeLeft();
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) onSwipeDown();
                    else onSwipeUp();
                    return true;
                }
            }
            return false;
        }
    }

    private void onSwipeLeft() {
        Toast.makeText(getContext(), "Connection Request Sent", Toast.LENGTH_SHORT).show();
        nextUserWithAnimation(-500);
    }

    private void onSwipeRight() {
        Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
        nextUserWithAnimation(500);
    }

    private void onSwipeUp() {
        Toast.makeText(getContext(), "Next User", Toast.LENGTH_SHORT).show();
        nextUserWithAnimation(-500);
    }

    private void onSwipeDown() {
        Toast.makeText(getContext(), "Previous User", Toast.LENGTH_SHORT).show();
        previousUserWithAnimation();
    }

    private void nextUser() {
        currentIndex = (currentIndex + 1) % users.size();
        showUser(currentIndex);
    }

    private void previousUser() {
        currentIndex = (currentIndex - 1 + users.size()) % users.size();
        showUser(currentIndex);
    }

    // Animate card movement on swipe
    private void nextUserWithAnimation(float distanceX) {
        card.animate()
                .translationXBy(distanceX)
                .alpha(0)
                .setDuration(300)
                .withEndAction(() -> {
                    nextUser();
                    card.setTranslationX(0);
                    card.setAlpha(1f);
                });
    }

    private void previousUserWithAnimation() {
        card.animate()
                .translationXBy(-500)
                .alpha(0)
                .setDuration(300)
                .withEndAction(() -> {
                    previousUser();
                    card.setTranslationX(0);
                    card.setAlpha(1f);
                });
    }

    // Simple User model
    private static class User {
        String name, bio;
        int age, imageResId;

        User(String name, int age, String bio, int imageResId) {
            this.name = name;
            this.age = age;
            this.bio = bio;
            this.age = age;
            this.imageResId = imageResId;
        }
    }
}
