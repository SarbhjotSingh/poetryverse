package com.poetryverse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "poems", indexes = {
        @Index(name = "idx_poems_user_id", columnList = "user_id"),
        @Index(name = "idx_poems_genre", columnList = "genre"),
        @Index(name = "idx_poems_mood", columnList = "mood"),
        @Index(name = "idx_poems_status", columnList = "status"),
        @Index(name = "idx_poems_updated_at", columnList = "updated_at")
})
public class Poem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank(message = "Poem title is required")
    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @NotBlank(message = "Poem verse content is required")
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "html_content", columnDefinition = "LONGTEXT")
    private String htmlContent;

    @Column(nullable = false, length = 80)
    private String genre = "Free Verse";

    @Column(length = 80)
    private String mood = "Contemplative";

    @Column(nullable = false, length = 30)
    private String status = "PUBLISHED"; // DRAFT or PUBLISHED

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "font_family", length = 80)
    private String fontFamily = "Cormorant Garamond";

    @Column(name = "font_size")
    private Integer fontSize = 20;

    @Column(name = "line_spacing")
    private Double lineSpacing = 1.85;

    @Column(name = "text_align", length = 20)
    private String textAlign = "LEFT";

    @Column(name = "paper_style", length = 60)
    private String paperStyle = "IVORY_MANUSCRIPT";

    @Column(name = "frame_style", length = 60)
    private String frameStyle = "BOTANICAL_GOLD";

    @Column(name = "background_image", length = 500)
    private String backgroundImage;

    @Column(name = "cover_artwork", length = 500)
    private String coverArtwork;

    @Column(length = 255)
    private String dedication;

    @Column(name = "author_note", columnDefinition = "TEXT")
    private String authorNote;

    @Column(name = "word_count", nullable = false)
    private int wordCount = 0;

    @Column(name = "character_count", nullable = false)
    private int characterCount = 0;

    @Column(name = "line_count", nullable = false)
    private int lineCount = 0;

    @Column(name = "stanza_count", nullable = false)
    private int stanzaCount = 0;

    @Column(name = "reading_time_sec", nullable = false)
    private int readingTimeSec = 0;

    @Column(name = "template_used", length = 80)
    private String templateUsed;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "poem_collections",
            joinColumns = @JoinColumn(name = "poem_id"),
            inverseJoinColumns = @JoinColumn(name = "collection_id")
    )
    private Set<PoemCollection> collections = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "poem_tags",
            joinColumns = @JoinColumn(name = "poem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "poem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Favorite> favorites = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private boolean favorite;

    public Poem() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        recalculateMetrics();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalculateMetrics();
    }

    public void recalculateMetrics() {
        if (this.content == null || this.content.isBlank()) {
            this.wordCount = 0;
            this.characterCount = 0;
            this.lineCount = 0;
            this.stanzaCount = 0;
            this.readingTimeSec = 0;
            return;
        }
        String plain = this.content.replaceAll("<[^>]*>", "").trim();
        this.characterCount = plain.length();
        String[] words = plain.split("\\s+");
        this.wordCount = plain.isEmpty() ? 0 : words.length;
        String[] lines = plain.split("\\r?\\n");
        int nonEmptyLines = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                nonEmptyLines++;
            }
        }
        this.lineCount = Math.max(1, nonEmptyLines);
        String[] stanzas = plain.split("(\\r?\\n\\s*){2,}");
        this.stanzaCount = Math.max(1, stanzas.length);
        // Poetry reading speed ~110 words per minute
        this.readingTimeSec = Math.max(10, (int) Math.ceil((this.wordCount / 110.0) * 60));
        if (this.htmlContent == null || this.htmlContent.isBlank()) {
            this.htmlContent = plain.replace("\n", "<br/>");
        }
    }

    @Transient
    public String getExcerpt() {
        if (content == null) return "";
        String clean = content.replaceAll("<[^>]*>", "").trim();
        String[] lines = clean.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                if (count > 0) sb.append("\n");
                sb.append(line.trim());
                count++;
                if (count >= 4) break;
            }
        }
        return sb.toString();
    }

    @Transient
    public List<String> getTagNames() {
        if (tags == null) return List.of();
        return tags.stream().map(Tag::getName).sorted().collect(Collectors.toList());
    }

    @Transient
    public String getTagsCsv() {
        return String.join(", ", getTagNames());
    }

    @Transient
    public List<Long> getCollectionIds() {
        if (collections == null) return List.of();
        return collections.stream().map(PoemCollection::getId).collect(Collectors.toList());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public Double getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(Double lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public String getPaperStyle() {
        return paperStyle;
    }

    public void setPaperStyle(String paperStyle) {
        this.paperStyle = paperStyle;
    }

    public String getFrameStyle() {
        return frameStyle;
    }

    public void setFrameStyle(String frameStyle) {
        this.frameStyle = frameStyle;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getCoverArtwork() {
        return coverArtwork;
    }

    public void setCoverArtwork(String coverArtwork) {
        this.coverArtwork = coverArtwork;
    }

    public String getDedication() {
        return dedication;
    }

    public void setDedication(String dedication) {
        this.dedication = dedication;
    }

    public String getAuthorNote() {
        return authorNote;
    }

    public void setAuthorNote(String authorNote) {
        this.authorNote = authorNote;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getStanzaCount() {
        return stanzaCount;
    }

    public void setStanzaCount(int stanzaCount) {
        this.stanzaCount = stanzaCount;
    }

    public int getReadingTimeSec() {
        return readingTimeSec;
    }

    public void setReadingTimeSec(int readingTimeSec) {
        this.readingTimeSec = readingTimeSec;
    }

    public String getTemplateUsed() {
        return templateUsed;
    }

    public void setTemplateUsed(String templateUsed) {
        this.templateUsed = templateUsed;
    }

    public Set<PoemCollection> getCollections() {
        return collections;
    }

    public void setCollections(Set<PoemCollection> collections) {
        this.collections = collections;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<Favorite> favorites) {
        this.favorites = favorites;
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

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
