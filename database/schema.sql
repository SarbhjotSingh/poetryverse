-- ============================================================================
-- POETRYVERSE - MYSQL RELATIONAL DATABASE SCHEMA
-- Database: poetryverse_db
-- Engine: InnoDB | Charset: utf8mb4_unicode_ci
-- ============================================================================

CREATE DATABASE IF NOT EXISTS poetryverse_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE poetryverse_db;

-- ----------------------------------------------------------------------------
-- 1. TABLE: users
-- Stores registered poets, authentication credentials, and profile biography.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    pen_name VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(500),
    location VARCHAR(120),
    website VARCHAR(255),
    favorite_poet VARCHAR(120),
    signature_quote VARCHAR(300),
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_POET',
    current_streak INT NOT NULL DEFAULT 1,
    longest_streak INT NOT NULL DEFAULT 1,
    last_writing_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. TABLE: user_settings
-- Stores 1-to-1 user preferences for theme, typography, and editor behavior.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    theme_mode VARCHAR(30) NOT NULL DEFAULT 'EDITORIAL_OLIVE',
    editor_font_family VARCHAR(80) NOT NULL DEFAULT 'Cormorant Garamond',
    editor_font_size INT NOT NULL DEFAULT 20,
    editor_line_spacing DOUBLE NOT NULL DEFAULT 1.85,
    paper_texture VARCHAR(50) NOT NULL DEFAULT 'WARM_IVORY',
    default_genre VARCHAR(60) NOT NULL DEFAULT 'Free Verse',
    default_alignment VARCHAR(20) NOT NULL DEFAULT 'LEFT',
    auto_save_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    auto_save_interval_sec INT NOT NULL DEFAULT 15,
    ambient_botanicals BOOLEAN NOT NULL DEFAULT TRUE,
    show_live_stats BOOLEAN NOT NULL DEFAULT TRUE,
    daily_word_goal INT NOT NULL DEFAULT 250,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. TABLE: poems
-- Core entity storing poems, rich HTML and plain text, styling, metadata, stats.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS poems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(220) NOT NULL,
    subtitle VARCHAR(300),
    content LONGTEXT NOT NULL,
    html_content LONGTEXT,
    genre VARCHAR(80) NOT NULL DEFAULT 'Free Verse',
    mood VARCHAR(80) DEFAULT 'Contemplative',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    font_family VARCHAR(80) DEFAULT 'Cormorant Garamond',
    font_size INT DEFAULT 20,
    line_spacing DOUBLE DEFAULT 1.85,
    text_align VARCHAR(20) DEFAULT 'LEFT',
    paper_style VARCHAR(60) DEFAULT 'IVORY_MANUSCRIPT',
    frame_style VARCHAR(60) DEFAULT 'BOTANICAL_GOLD',
    background_image VARCHAR(500),
    cover_artwork VARCHAR(500),
    dedication VARCHAR(255),
    author_note TEXT,
    word_count INT NOT NULL DEFAULT 0,
    character_count INT NOT NULL DEFAULT 0,
    line_count INT NOT NULL DEFAULT 0,
    stanza_count INT NOT NULL DEFAULT 0,
    reading_time_sec INT NOT NULL DEFAULT 0,
    template_used VARCHAR(80),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_poems_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_poems_user_id (user_id),
    INDEX idx_poems_genre (genre),
    INDEX idx_poems_mood (mood),
    INDEX idx_poems_status (status),
    INDEX idx_poems_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. TABLE: collections
-- Custom poetry collections/anthologies created by poets.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    subtitle VARCHAR(250),
    description TEXT,
    cover_image VARCHAR(500),
    accent_color VARCHAR(30) NOT NULL DEFAULT '#353626',
    frame_motif VARCHAR(60) DEFAULT 'ORNATE_GOLD',
    epigraph VARCHAR(350),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_collections_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_collections_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. TABLE: poem_collections
-- Many-to-Many join table associating poems with multiple collections.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS poem_collections (
    poem_id BIGINT NOT NULL,
    collection_id BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (poem_id, collection_id),
    CONSTRAINT fk_pc_poem FOREIGN KEY (poem_id) REFERENCES poems(id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_collection FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE,
    INDEX idx_pc_collection (collection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. TABLE: favorites
-- Dedicated table tracking poems marked as favorites by users.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    poem_id BIGINT NOT NULL,
    personal_note VARCHAR(300),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_favorites_user_poem UNIQUE (user_id, poem_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_poem FOREIGN KEY (poem_id) REFERENCES poems(id) ON DELETE CASCADE,
    INDEX idx_favorites_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 7. TABLE: journal_entries
-- Personal writing journal for daily reflections, mood tracking, and ideas.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    mood VARCHAR(60) NOT NULL DEFAULT 'Serene',
    weather VARCHAR(60) DEFAULT 'Mist & Morning Dew',
    inspiration_spark TEXT,
    location_note VARCHAR(140),
    word_count INT NOT NULL DEFAULT 0,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_journal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_journal_user_date (user_id, entry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 8. TABLE: tags
-- Reusable poetic tags scoped per user or global.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    color VARCHAR(30) DEFAULT '#A18B50',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_tags_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tags_user (user_id),
    INDEX idx_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 9. TABLE: poem_tags
-- Many-to-Many join table associating poems with tags.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS poem_tags (
    poem_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (poem_id, tag_id),
    CONSTRAINT fk_pt_poem FOREIGN KEY (poem_id) REFERENCES poems(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_pt_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 10. TABLE: writing_statistics
-- Daily writing activity log for analytics charts, streaks, and heatmaps.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS writing_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    poems_created INT NOT NULL DEFAULT 0,
    poems_edited INT NOT NULL DEFAULT 0,
    words_written INT NOT NULL DEFAULT 0,
    characters_written INT NOT NULL DEFAULT 0,
    minutes_spent INT NOT NULL DEFAULT 0,
    dominant_genre VARCHAR(80),
    dominant_mood VARCHAR(80),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_stats_user_date UNIQUE (user_id, activity_date),
    CONSTRAINT fk_stats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_stats_user_date (user_id, activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 11. TABLE: writing_prompts
-- Curated database of writing prompts, challenges, quotes, and public-domain verse.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS writing_prompts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(60) NOT NULL,
    title VARCHAR(200) NOT NULL,
    prompt_text TEXT NOT NULL,
    suggested_genre VARCHAR(80) DEFAULT 'Free Verse',
    suggested_mood VARCHAR(80) DEFAULT 'Nostalgic',
    difficulty VARCHAR(40) DEFAULT 'All Levels',
    author_attribution VARCHAR(140),
    sample_verse TEXT,
    is_daily_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_prompts_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
