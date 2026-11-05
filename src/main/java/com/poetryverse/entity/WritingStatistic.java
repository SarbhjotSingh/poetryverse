package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "writing_statistics",
        uniqueConstraints = @UniqueConstraint(name = "uq_stats_user_date", columnNames = {"user_id", "activity_date"}),
        indexes = @Index(name = "idx_stats_user_date", columnList = "user_id, activity_date"))
public class WritingStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate = LocalDate.now();

    @Column(name = "poems_created", nullable = false)
    private int poemsCreated = 0;

    @Column(name = "poems_edited", nullable = false)
    private int poemsEdited = 0;

    @Column(name = "words_written", nullable = false)
    private int wordsWritten = 0;

    @Column(name = "characters_written", nullable = false)
    private int charactersWritten = 0;

    @Column(name = "minutes_spent", nullable = false)
    private int minutesSpent = 0;

    @Column(name = "dominant_genre", length = 80)
    private String dominantGenre = "Free Verse";

    @Column(name = "dominant_mood", length = 80)
    private String dominantMood = "Contemplative";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WritingStatistic() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public int getPoemsCreated() {
        return poemsCreated;
    }

    public void setPoemsCreated(int poemsCreated) {
        this.poemsCreated = poemsCreated;
    }

    public int getPoemsEdited() {
        return poemsEdited;
    }

    public void setPoemsEdited(int poemsEdited) {
        this.poemsEdited = poemsEdited;
    }

    public int getWordsWritten() {
        return wordsWritten;
    }

    public void setWordsWritten(int wordsWritten) {
        this.wordsWritten = wordsWritten;
    }

    public int getCharactersWritten() {
        return charactersWritten;
    }

    public void setCharactersWritten(int charactersWritten) {
        this.charactersWritten = charactersWritten;
    }

    public int getMinutesSpent() {
        return minutesSpent;
    }

    public void setMinutesSpent(int minutesSpent) {
        this.minutesSpent = minutesSpent;
    }

    public String getDominantGenre() {
        return dominantGenre;
    }

    public void setDominantGenre(String dominantGenre) {
        this.dominantGenre = dominantGenre;
    }

    public String getDominantMood() {
        return dominantMood;
    }

    public void setDominantMood(String dominantMood) {
        this.dominantMood = dominantMood;
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
}
