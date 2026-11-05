package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "pen_name", length = 100)
    private String penName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl = "/images/avatar-poet.svg";

    @Column(length = 120)
    private String location = "Bath, Somerset";

    @Column(length = 255)
    private String website;

    @Column(name = "favorite_poet", length = 120)
    private String favoritePoet = "Constantine P. Cavafy & Emily Dickinson";

    @Column(name = "signature_quote", length = 300)
    private String signatureQuote = "Where words find their forever.";

    @Column(nullable = false, length = 30)
    private String role = "ROLE_POET";

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 1;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 1;

    @Column(name = "last_writing_date")
    private LocalDate lastWritingDate = LocalDate.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private UserSettings settings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Poem> poems = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PoemCollection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<JournalEntry> journalEntries = new ArrayList<>();

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.lastWritingDate == null) {
            this.lastWritingDate = LocalDate.now();
        }
        if (this.penName == null || this.penName.isBlank()) {
            this.penName = this.fullName;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFavoritePoet() {
        return favoritePoet;
    }

    public void setFavoritePoet(String favoritePoet) {
        this.favoritePoet = favoritePoet;
    }

    public String getSignatureQuote() {
        return signatureQuote;
    }

    public void setSignatureQuote(String signatureQuote) {
        this.signatureQuote = signatureQuote;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastWritingDate() {
        return lastWritingDate;
    }

    public void setLastWritingDate(LocalDate lastWritingDate) {
        this.lastWritingDate = lastWritingDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public List<Poem> getPoems() {
        return poems;
    }

    public void setPoems(List<Poem> poems) {
        this.poems = poems;
    }

    public List<PoemCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<PoemCollection> collections) {
        this.collections = collections;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    public List<JournalEntry> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }
}
