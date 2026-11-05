package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_journal_user_date", columnList = "user_id, entry_date")
})
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate = LocalDate.now();

    @NotBlank(message = "Journal entry title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Journal reflection content is required")
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false, length = 60)
    private String mood = "Serene";

    @Column(length = 60)
    private String weather = "Mist & Morning Dew";

    @Column(name = "inspiration_spark", columnDefinition = "TEXT")
    private String inspirationSpark;

    @Column(name = "location_note", length = 140)
    private String locationNote = "Writing Desk";

    @Column(name = "word_count", nullable = false)
    private int wordCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public JournalEntry() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.entryDate == null) {
            this.entryDate = LocalDate.now();
        }
        recalculateWords();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalculateWords();
    }

    public void recalculateWords() {
        if (this.content == null || this.content.isBlank()) {
            this.wordCount = 0;
        } else {
            this.wordCount = this.content.trim().split("\\s+").length;
        }
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

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getInspirationSpark() {
        return inspirationSpark;
    }

    public void setInspirationSpark(String inspirationSpark) {
        this.inspirationSpark = inspirationSpark;
    }

    public String getLocationNote() {
        return locationNote;
    }

    public void setLocationNote(String locationNote) {
        this.locationNote = locationNote;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
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
