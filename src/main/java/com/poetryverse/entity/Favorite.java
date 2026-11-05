package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(name = "uq_favorites_user_poem", columnNames = {"user_id", "poem_id"}),
        indexes = @Index(name = "idx_favorites_user", columnList = "user_id"))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poem_id", nullable = false)
    private Poem poem;

    @Column(name = "personal_note", length = 300)
    private String personalNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Favorite() {
    }

    public Favorite(User user, Poem poem) {
        this.user = user;
        this.poem = poem;
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

    public Poem getPoem() {
        return poem;
    }

    public void setPoem(Poem poem) {
        this.poem = poem;
    }

    public String getPersonalNote() {
        return personalNote;
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = personalNote;
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
