package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "theme_mode", nullable = false, length = 30)
    private String themeMode = "EDITORIAL_OLIVE";

    @Column(name = "editor_font_family", nullable = false, length = 80)
    private String editorFontFamily = "Cormorant Garamond";

    @Column(name = "editor_font_size", nullable = false)
    private int editorFontSize = 20;

    @Column(name = "editor_line_spacing", nullable = false)
    private double editorLineSpacing = 1.85;

    @Column(name = "paper_texture", nullable = false, length = 50)
    private String paperTexture = "WARM_IVORY";

    @Column(name = "default_genre", nullable = false, length = 60)
    private String defaultGenre = "Free Verse";

    @Column(name = "default_alignment", nullable = false, length = 20)
    private String defaultAlignment = "LEFT";

    @Column(name = "auto_save_enabled", nullable = false)
    private boolean autoSaveEnabled = true;

    @Column(name = "auto_save_interval_sec", nullable = false)
    private int autoSaveIntervalSec = 15;

    @Column(name = "ambient_botanicals", nullable = false)
    private boolean ambientBotanicals = true;

    @Column(name = "show_live_stats", nullable = false)
    private boolean showLiveStats = true;

    @Column(name = "daily_word_goal", nullable = false)
    private int dailyWordGoal = 250;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserSettings() {
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

    public String getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode;
    }

    public String getEditorFontFamily() {
        return editorFontFamily;
    }

    public void setEditorFontFamily(String editorFontFamily) {
        this.editorFontFamily = editorFontFamily;
    }

    public int getEditorFontSize() {
        return editorFontSize;
    }

    public void setEditorFontSize(int editorFontSize) {
        this.editorFontSize = editorFontSize;
    }

    public double getEditorLineSpacing() {
        return editorLineSpacing;
    }

    public void setEditorLineSpacing(double editorLineSpacing) {
        this.editorLineSpacing = editorLineSpacing;
    }

    public String getPaperTexture() {
        return paperTexture;
    }

    public void setPaperTexture(String paperTexture) {
        this.paperTexture = paperTexture;
    }

    public String getDefaultGenre() {
        return defaultGenre;
    }

    public void setDefaultGenre(String defaultGenre) {
        this.defaultGenre = defaultGenre;
    }

    public String getDefaultAlignment() {
        return defaultAlignment;
    }

    public void setDefaultAlignment(String defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public int getAutoSaveIntervalSec() {
        return autoSaveIntervalSec;
    }

    public void setAutoSaveIntervalSec(int autoSaveIntervalSec) {
        this.autoSaveIntervalSec = autoSaveIntervalSec;
    }

    public boolean isAmbientBotanicals() {
        return ambientBotanicals;
    }

    public void setAmbientBotanicals(boolean ambientBotanicals) {
        this.ambientBotanicals = ambientBotanicals;
    }

    public boolean isShowLiveStats() {
        return showLiveStats;
    }

    public void setShowLiveStats(boolean showLiveStats) {
        this.showLiveStats = showLiveStats;
    }

    public int getDailyWordGoal() {
        return dailyWordGoal;
    }

    public void setDailyWordGoal(int dailyWordGoal) {
        this.dailyWordGoal = dailyWordGoal;
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
