package com.aditi.heartsyncstart;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String userId;
    private String name;
    private String email;
    private String age;
    private String bio;
    private String gender;
    private String imageUrl;
    private Map<String, Boolean> likedUsers; // Changed from List to Map
    private Map<String, Boolean> matches; // Changed from List to Map

    public User() {
        this.likedUsers = new HashMap<>();
        this.matches = new HashMap<>();
    }

    public User(String userId, String name, String email, String age, String bio, String gender) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.bio = bio;
        this.gender = gender;
        this.imageUrl = "";
        this.likedUsers = new HashMap<>();
        this.matches = new HashMap<>();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Map<String, Boolean> getLikedUsers() {
        if (likedUsers == null) likedUsers = new HashMap<>();
        return likedUsers;
    }
    public void setLikedUsers(Map<String, Boolean> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public Map<String, Boolean> getMatches() {
        if (matches == null) matches = new HashMap<>();
        return matches;
    }
    public void setMatches(Map<String, Boolean> matches) {
        this.matches = matches;
    }

    // Helper method to check if user is liked
    @Exclude
    public boolean hasLiked(String userId) {
        return likedUsers != null && likedUsers.containsKey(userId);
    }

    // Helper method to check if matched
    @Exclude
    public boolean hasMatched(String userId) {
        return matches != null && matches.containsKey(userId);
    }
}