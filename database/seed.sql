-- ============================================================================
-- POETRYVERSE - SAMPLE SEED DATA FOR MYSQL
-- Note: The Spring Boot DataSeeder also seeds this automatically on startup
-- if the database is empty. Default Demo Account:
-- Username: clara_vance  | Email: clara@poetryverse.art | Password: PoetVerse2026!
-- ============================================================================

USE poetryverse_db;

-- 1. Sample Poet Account (BCrypt hash for 'PoetVerse2026!')
INSERT IGNORE INTO users (
    id, username, email, password_hash, full_name, pen_name, bio,
    avatar_url, location, website, favorite_poet, signature_quote,
    role, current_streak, longest_streak, last_writing_date
) VALUES (
    1,
    'clara_vance',
    'clara@poetryverse.art',
    '$2a$10$wY1q1R2uJ6L6bC0D9eF7gO8hI9jK0lM1nO2pQ3rS4tU5vW6xY7zA8',
    'Clara Evangeline Vance',
    'C. E. Vance',
    'Botanical archivist and lyric poet exploring memory, wild olive groves, coastal fog, and the quiet architecture of human longing.',
    '/images/avatar-poet.svg',
    'Bath, Somerset & Kyoto',
    'https://poetryverse.art/clara',
    'Constantine P. Cavafy & Emily Dickinson',
    'Where words find their forever between pressed leaves and linen.',
    'ROLE_POET',
    7,
    19,
    CURRENT_DATE
);

-- 2. Default User Settings
INSERT IGNORE INTO user_settings (
    id, user_id, theme_mode, editor_font_family, editor_font_size,
    editor_line_spacing, paper_texture, default_genre, default_alignment,
    auto_save_enabled, auto_save_interval_sec, ambient_botanicals,
    show_live_stats, daily_word_goal
) VALUES (
    1, 1, 'EDITORIAL_OLIVE', 'Cormorant Garamond', 20, 1.85,
    'WARM_IVORY', 'Free Verse', 'LEFT', TRUE, 15, TRUE, TRUE, 300
);

-- 3. Sample Collections
INSERT IGNORE INTO collections (id, user_id, name, subtitle, description, accent_color, frame_motif, epigraph, is_featured)
VALUES
(1, 1, 'Love & Romance', 'Letters Sealed in Amber Wax', 'Intimate lyrics of devotion, whispered aubades, and the golden thread of enduring affection.', '#353626', 'ORNATE_GOLD', 'To love is to press a wildflower into the book of time.', TRUE),
(2, 1, 'Nature & Seasons', 'Herbarium of the Changing Year', 'Botanical meditations on pine ridgelines, autumn rain, olive orchards, and coastal tides.', '#60604D', 'BOTANICAL_VINE', 'Every leaf speaks bliss to me, fluttering from the autumn tree.', TRUE),
(3, 1, 'Life & Philosophy', 'Voyages Toward Ithaca', 'Stoic and lyrical reflections on time, mortality, purpose, and the long odyssey of the soul.', '#25251E', 'CLASSICAL_ARCH', 'Keep Ithaca always in your mind. Arriving there is what you are destined for.', TRUE),
(4, 1, 'Dreams & Memories', 'Nocturnes in Warm Ivory', 'Half-remembered corridors, childhood gardens, and lantern-lit reveries written after midnight.', '#A18B50', 'OVAL_ANTIQUE', 'We are such stuff as dreams are made on.', FALSE),
(5, 1, 'Heartbreak', 'Elegies for Unsent Letters', 'Tender reckonings with absence, silence, and the porcelain fracture of parting.', '#4A3E36', 'MINIMAL_HAIRLINE', 'The wound is the place where the Light enters you.', FALSE),
(6, 1, 'Spirituality', 'Sanctuary of Stillness', 'Sacred hymns of quietude, candlelit vigils, and the unseen grace binding all living things.', '#353626', 'ORNATE_GOLD', 'Be still, and listen to the music behind the breath.', FALSE),
(7, 1, 'Untitled Thoughts', 'Fragments & Marginalia', 'Raw ink sketches, unpolished couplets, and fleeting verses captured in the margins of the day.', '#60604D', 'BOTANICAL_VINE', 'Even a single line can hold an entire season.', FALSE);

-- 4. Curated Writing Prompts & Public Domain Inspiration
INSERT IGNORE INTO writing_prompts (id, category, title, prompt_text, suggested_genre, suggested_mood, difficulty, author_attribution, sample_verse, is_daily_featured)
VALUES
(1, 'PROMPT', 'The Pressed Botanical Specimen', 'Open an imaginary antique book and describe a dried flower pressed between pages 142 and 143. Who placed it there, what season was it gathered, and what unspoken promise does it preserve?', 'Free Verse', 'Nostalgic', 'All Levels', 'PoetryVerse Editorial', 'Between the yellowed linen leaves, a sprig of wild thyme still remembers the rain.', TRUE),
(2, 'PROMPT', 'Ursine Ridgeline in Cloud', 'Write from the perspective of a solitary creature moving silently through pine and mountain fog at dawn, where the boundary between body and landscape dissolves.', 'Nature Poetry', 'Serene', 'Intermediate', 'Inspired by Plumwood Journal', 'This is the one where I dream myself ursine, a silent breath of fur between pine on that ridgeline crowded with cloud.', FALSE),
(3, 'CHALLENGE', 'The Fourteen-Line Golden Sonnet', 'Compose a classical 14-line sonnet using imagery of gold leaf, candlelight, and autumn wind. Conclude with a rhyming couplet that transforms sorrow into gratitude.', 'Sonnet', 'Romantic', 'Advanced', 'PoetryVerse Masterclass', 'Shall I compare thee to an autumn eve, / When amber lanterns gild the orchard bough?', FALSE),
(4, 'PUBLIC_DOMAIN', 'Ithaca (1911)', 'Keep Ithaca always in your mind. Arriving there is what you are destined for. But do not hurry the journey at all. Better if it lasts for years, so you are old by the time you reach the island, wealthy with all you have gained on the way, not expecting Ithaca to make you rich.', 'Life & Philosophy', 'Philosophical', 'Classic', 'Constantine P. Cavafy (Public Domain Translation)', 'When you set out on your journey to Ithaca, pray that the road is long, full of adventure, full of knowledge.', TRUE),
(5, 'PUBLIC_DOMAIN', 'Hope is the Thing with Feathers', 'Hope is the thing with feathers / That perches in the soul, / And sings the tune without the words, / And never stops at all, / And sweetest in the gale is heard; / And sore must be the storm / That could abash the little bird / That kept so many warm.', 'Inspirational Poetry', 'Hopeful', 'Classic', 'Emily Dickinson (1891, Public Domain)', 'Hope is the thing with feathers / That perches in the soul...', FALSE),
(6, 'PUBLIC_DOMAIN', 'The Lake Isle of Innisfree', 'I will arise and go now, and go to Innisfree, / And a small cabin build there, of clay and wattles made: / Nine bean-rows will I have there, a hive for the honey-bee, / And live alone in the bee-loud glade.', 'Nature Poetry', 'Peaceful', 'Classic', 'William Butler Yeats (1890, Public Domain)', 'And I shall have some peace there, for peace comes dropping slow...', FALSE),
(7, 'QUOTE', 'Rainer Maria Rilke on Solitude', 'Go into yourself and see how deep the place is from which your life flows; at its source you will find the answer to the question of whether you must create.', 'Free Verse', 'Contemplative', 'All Levels', 'Rainer Maria Rilke, Letters to a Young Poet', 'Accept the weight of every word as if it were drawn from silence itself.', TRUE);
