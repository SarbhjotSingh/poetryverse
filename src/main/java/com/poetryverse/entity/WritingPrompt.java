package com.poetryverse.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "writing_prompts", indexes = {
        @Index(name = "idx_prompts_category", columnList = "category")
})
public class WritingPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String category = "PROMPT"; // PROMPT, CHALLENGE, QUOTE, PUBLIC_DOMAIN

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(name = "suggested_genre", length = 80)
    private String suggestedGenre = "Free Verse";

    @Column(name = "suggested_mood", length = 80)
    private String suggestedMood = "Nostalgic";

    @Column(length = 40)
    private String difficulty = "All Levels";

    @Column(name = "author_attribution", length = 140)
    private String authorAttribution;

    @Column(name = "sample_verse", columnDefinition = "TEXT")
    private String sampleVerse;

    @Column(name = "is_daily_featured", nullable = false)
    private boolean dailyFeatured = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WritingPrompt() {
    }

    public WritingPrompt(String category, String title, String promptText, String suggestedGenre,
                         String suggestedMood, String difficulty, String authorAttribution,
                         String sampleVerse, boolean dailyFeatured) {
        this.category = category;
        this.title = title;
        this.promptText = promptText;
        this.suggestedGenre = suggestedGenre;
        this.suggestedMood = suggestedMood;
        this.difficulty = difficulty;
        this.authorAttribution = authorAttribution;
        this.sampleVerse = sampleVerse;
        this.dailyFeatured = dailyFeatured;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getSuggestedGenre() {
        return suggestedGenre;
    }

    public void setSuggestedGenre(String suggestedGenre) {
        this.suggestedGenre = suggestedGenre;
    }

    public String getSuggestedMood() {
        return suggestedMood;
    }

    public void setSuggestedMood(String suggestedMood) {
        this.suggestedMood = suggestedMood;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAuthorAttribution() {
        return authorAttribution;
    }

    public void setAuthorAttribution(String authorAttribution) {
        this.authorAttribution = authorAttribution;
    }

    public String getSampleVerse() {
        return sampleVerse;
    }

    public void setSampleVerse(String sampleVerse) {
        this.sampleVerse = sampleVerse;
    }

    public boolean isDailyFeatured() {
        return dailyFeatured;
    }

    public void setDailyFeatured(boolean dailyFeatured) {
        this.dailyFeatured = dailyFeatured;
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
