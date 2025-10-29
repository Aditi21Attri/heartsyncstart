package com.aditi.heartsyncstart;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String age;
    private String bio;
    private String gender;
    private String imageUrl;
    private List<String> likedUsers; // Users this user has liked
    private List<String> matches; // Mutual matches

    public User() {
        this.likedUsers = new ArrayList<>();
        this.matches = new ArrayList<>();
    }

    public User(String userId, String name, String email, String age, String bio, String gender) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.bio = bio;
        this.gender = gender;
        this.imageUrl = "";
        this.likedUsers = new ArrayList<>();
        this.matches = new ArrayList<>();
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

    public List<String> getLikedUsers() { return likedUsers; }
    public void setLikedUsers(List<String> likedUsers) { this.likedUsers = likedUsers; }

    public List<String> getMatches() { return matches; }
    public void setMatches(List<String> matches) { this.matches = matches; }
}