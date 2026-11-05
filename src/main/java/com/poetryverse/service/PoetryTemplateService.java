package com.poetryverse.service;

import com.poetryverse.entity.WritingPrompt;
import com.poetryverse.repository.WritingPromptRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PoetryTemplateService {

    private final WritingPromptRepository promptRepository;
    private final Random random = new Random();

    public PoetryTemplateService(WritingPromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public static class TemplateDefinition {
        private final String key;
        private final String name;
        private final String subtitle;
        private final String genre;
        private final String mood;
        private final String structureRule;
        private final String meterNote;
        private final String defaultTitle;
        private final String starterContent;
        private final String sampleExcerpt;

        public TemplateDefinition(String key, String name, String subtitle, String genre, String mood,
                                  String structureRule, String meterNote, String defaultTitle,
                                  String starterContent, String sampleExcerpt) {
            this.key = key;
            this.name = name;
            this.subtitle = subtitle;
            this.genre = genre;
            this.mood = mood;
            this.structureRule = structureRule;
            this.meterNote = meterNote;
            this.defaultTitle = defaultTitle;
            this.starterContent = starterContent;
            this.sampleExcerpt = sampleExcerpt;
        }

        public String getKey() { return key; }
        public String getName() { return name; }
        public String getSubtitle() { return subtitle; }
        public String getGenre() { return genre; }
        public String getMood() { return mood; }
        public String getStructureRule() { return structureRule; }
        public String getMeterNote() { return meterNote; }
        public String getDefaultTitle() { return defaultTitle; }
        public String getStarterContent() { return starterContent; }
        public String getSampleExcerpt() { return sampleExcerpt; }
    }

    public List<TemplateDefinition> getAllTemplates() {
        List<TemplateDefinition> list = new ArrayList<>();

        list.add(new TemplateDefinition(
                "romantic",
                "Romantic Poetry",
                "Aubade & Intimate Epistolary Lyric",
                "Romantic Poetry",
                "Romantic",
                "3 to 4 lyrical quatrains with sensory imagery of light, touch, and time",
                "Flowing cadence • Soft consonance & internal rhyme",
                "Letter Sealed in Amber Light",
                "Before the morning unclasps its shutter of gold,\nI trace the quiet geography of your name—\nWhere olive shadows lean against the stone,\nAnd every silence speaks of whence you came.\n\n[Stanza II — An image of a shared season or memory]\n...\n\n[Stanza III — The vow that outlasts the turning year]\n...",
                "Before the morning unclasps its shutter of gold, / I trace the quiet geography of your name..."
        ));

        list.add(new TemplateDefinition(
                "haiku",
                "Haiku (5-7-5)",
                "Classical Three-Line Seasonal Distillation",
                "Haiku",
                "Serene",
                "3 lines: 5 syllables • 7 syllables • 5 syllables, anchored by a seasonal word (kigo)",
                "17 syllables total • Juxtaposition (kireji) between line 2 and 3",
                "Pine Mist at Dawn",
                "White mist on the pine,        [5 syllables]\nSilent breath across the ridge, [7 syllables]\nAutumn bell rings slow.         [5 syllables]\n\n— II —\nPale gold olive leaf,\nResting on the mossy well,\nRipples hold the sky.",
                "White mist on the pine, / Silent breath across the ridge, / Autumn bell rings slow."
        ));

        list.add(new TemplateDefinition(
                "sonnet",
                "Classical Sonnet",
                "Fourteen-Line Architectural Verse with Volta",
                "Sonnet",
                "Philosophical",
                "14 lines: Three quatrains (ABAB CDCD EFEF) followed by a closing rhymed couplet (GG)",
                "Iambic Pentameter (10 syllables per line, unstressed/stressed)",
                "Sonnet for the Golden Hour",
                "When amber lanterns gild the orchard bough,\nAnd evening folds its cloak across the hill,\nI call to mind the unforgotten vow\nThat keeps the restless pendulum so still.\n\nThough seasons scatter blossom into dust,\nAnd marble arches weather in the rain,\nNo quiet winter tarnishes With rust\nThe verse we carved upon the windowpane.\n\n[Turn / Volta — Line 9 shifts perspective]\nYet time is not a thief of what we keep,\nNor distance any wall to bar the sight;\nLike roots that drink the mountain water deep,\nOur silent letters blossom in the night.\n\n  So let the clock strike out the passing year—\n  Forever is the word written right here.",
                "When amber lanterns gild the orchard bough, / And evening folds its cloak across the hill..."
        ));

        list.add(new TemplateDefinition(
                "free_verse",
                "Free Verse",
                "Unfettered Contemporary Editorial Cadence",
                "Free Verse",
                "Contemplative",
                "Open form driven by natural breath, intentional line breaks, and caesura",
                "Organic rhythm • Enjambment & white-space pacing",
                "This Is the One Where I Dream",
                "This is the one where I dream\nmyself ursine, a silent breath\nof fur between pine on that\nridgeline crowded with cloud.\n\nBelow, the river unspools its silver thread\nthrough boulders older than language—\nand I ask nothing of the morning\nexcept the weight of moss underfoot\nand the long, unhurried light.",
                "This is the one where I dream / myself ursine, a silent breath / of fur between pine..."
        ));

        list.add(new TemplateDefinition(
                "acrostic",
                "Acrostic Poetry",
                "Vertical Cipher Where Initials Spell a Word",
                "Acrostic Poetry",
                "Nostalgic",
                "Initial letters of each line spell out a vertical theme word (e.g., ETERNITY or FOREVER)",
                "Flexible line length • Strong opening letter emphasis",
                "F.O.R.E.V.E.R. (An Acrostic)",
                "F irelight trembles on the linen page,\nO live branches tap the leaded glass,\nR emembering the vows of another age,\nE CHOING softly as the hours pass.\nV iolets pressed within a leather tome,\nE ndure beyond the frost of winter's flight,\nR eturning every wandering cadence home.",
                "F irelight trembles on the linen page, / O live branches tap the leaded glass..."
        ));

        list.add(new TemplateDefinition(
                "nature",
                "Nature & Botanical Poetry",
                "Field Notes from Forest, Coast & Herbarium",
                "Nature Poetry",
                "Serene",
                "Sensory observation of flora, weather, topography, and ecological stillness",
                "Vivid botanical nouns • Auditory & tactile imagery",
                "Herbarium of Wild Olive & Sage",
                "Along the limestone terrace after rain,\nthe wild sage releases its silver oil\ninto the cool Mediterranean air.\n\nEach olive leaf turns two faces to the wind:\none dark as ancient bronze,\none pale as the underside of a gull's wing.\n\nTeach me to stand as the orchard stands—\nknot-rooted in dry earth,\nyet yielding sweetness from the stone.",
                "Each olive leaf turns two faces to the wind: / one dark as ancient bronze..."
        ));

        list.add(new TemplateDefinition(
                "inspirational",
                "Inspirational Odyssey",
                "Cavafy-Inspired Philosophical Voyage",
                "Inspirational Poetry",
                "Hopeful",
                "Second-person meditative address ('When you set out...') celebrating resilience and journey",
                "Measured, noble cadence • Luminous closing benediction",
                "On Setting Out for Your Ithaca",
                "When you set out upon the unwritten road,\npray that the morning stretches wide and clear,\nfull of strange harbors, full of quiet lore.\n\nDo not fear the shadows on the pass;\nthey cannot cross the threshold of a mind\nthat carries its own lantern in the dark.\n\nWalk slowly through the markets of the world:\ngather the amber, coral, and old song,\nand know the journey is itself the crown.",
                "When you set out upon the unwritten road, / pray that the morning stretches wide and clear..."
        ));

        list.add(new TemplateDefinition(
                "sad",
                "Sad & Elegiac Verse",
                "Nocturne for Absence, Memory & Quiet Parting",
                "Sad & Elegiac",
                "Melancholic",
                "Restrained, poignant stanzas using understatement and domestic objects to evoke loss",
                "Slow tempo • Long vowels & gentle falling endings",
                "The Teacup Left Upon the Sill",
                "Nothing in the parlor has been moved\nsince the October rain began to fall;\nyour spectacles still rest beside the lamp,\nyour coat still keeps its shadow on the wall.\n\nHow strange that porcelain outlives the hand\nthat warmed it in the chill of early dawn—\nand how a room can hold so much of you\nlong after every spoken word is gone.",
                "How strange that porcelain outlives the hand / that warmed it in the chill of early dawn..."
        ));

        return list;
    }

    public Optional<TemplateDefinition> getTemplateByKey(String key) {
        if (key == null) return Optional.empty();
        return getAllTemplates().stream()
                .filter(t -> t.getKey().equalsIgnoreCase(key.trim()))
                .findFirst();
    }

    public List<WritingPrompt> getPromptsByCategory(String category) {
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
            return promptRepository.findAll();
        }
        return promptRepository.findByCategoryOrderByIdAsc(category.toUpperCase());
    }

    public WritingPrompt getRandomInspiration() {
        List<WritingPrompt> all = promptRepository.findAll();
        if (all.isEmpty()) {
            return new WritingPrompt(
                    "PROMPT",
                    "The Lantern in the Olive Grove",
                    "Write a poem about discovering an old brass lantern hanging on a gnarled olive branch at twilight.",
                    "Free Verse",
                    "Nostalgic",
                    "All Levels",
                    "PoetryVerse Editorial",
                    "A single flame behind the frosted glass...",
                    true
            );
        }
        return all.get(random.nextInt(all.size()));
    }

    public WritingPrompt getDailyQuote() {
        List<WritingPrompt> quotes = promptRepository.findByCategoryOrderByIdAsc("QUOTE");
        if (!quotes.isEmpty()) {
            int idx = Math.floorMod(java.time.LocalDate.now().getDayOfYear(), quotes.size());
            return quotes.get(idx);
        }
        return getRandomInspiration();
    }
}
