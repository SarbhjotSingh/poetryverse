package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "collections", indexes = {
        @Index(name = "idx_collections_user_id", columnList = "user_id")
})
public class PoemCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank(message = "Collection name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 250)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "accent_color", nullable = false, length = 30)
    private String accentColor = "#353626";

    @Column(name = "frame_motif", length = 60)
    private String frameMotif = "ORNATE_GOLD";

    @Column(length = 350)
    private String epigraph;

    @Column(name = "is_featured", nullable = false)
    private boolean featured = false;

    @ManyToMany(mappedBy = "collections")
    @JsonIgnore
    private Set<Poem> poems = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PoemCollection() {
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

    @Transient
    public int getPoemCount() {
        return poems != null ? poems.size() : 0;
    }

    @Transient
    public int getTotalWords() {
        if (poems == null) return 0;
        return poems.stream().mapToInt(Poem::getWordCount).sum();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getFrameMotif() {
        return frameMotif;
    }

    public void setFrameMotif(String frameMotif) {
        this.frameMotif = frameMotif;
    }

    public String getEpigraph() {
        return epigraph;
    }

    public void setEpigraph(String epigraph) {
        this.epigraph = epigraph;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Set<Poem> getPoems() {
        return poems;
    }

    public void setPoems(Set<Poem> poems) {
        this.poems = poems;
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
