package com.poetryverse.service;

import com.poetryverse.entity.*;
import com.poetryverse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final WritingPromptRepository promptRepository;
    private final CollectionRepository collectionRepository;
    private final PoemRepository poemRepository;
    private final JournalEntryRepository journalRepository;
    private final WritingStatisticRepository statisticRepository;
    private final UserService userService;
    private final PoemService poemService;

    public DataSeeder(UserRepository userRepository,
                      WritingPromptRepository promptRepository,
                      CollectionRepository collectionRepository,
                      PoemRepository poemRepository,
                      JournalEntryRepository journalRepository,
                      WritingStatisticRepository statisticRepository,
                      UserService userService,
                      PoemService poemService) {
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.collectionRepository = collectionRepository;
        this.poemRepository = poemRepository;
        this.journalRepository = journalRepository;
        this.statisticRepository = statisticRepository;
        this.userService = userService;
        this.poemService = poemService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedWritingPromptsIfEmpty();
        seedDemoPoetIfEmpty();
    }

    private void seedWritingPromptsIfEmpty() {
        if (promptRepository.count() > 0) {
            return;
        }
        log.info("Seeding curated Writing Prompts, Challenges, Quotes, and Public-Domain Anthology...");

        List<WritingPrompt> prompts = List.of(
                // PROMPTS
                new WritingPrompt("PROMPT", "The Pressed Botanical Specimen",
                        "Open an imaginary antique herbarium and describe a dried wildflower pressed between pages 142 and 143. Who placed it there, what season was it gathered, and what unspoken vow does it preserve?",
                        "Free Verse", "Nostalgic", "All Levels", "PoetryVerse Editorial",
                        "Between the yellowed linen leaves,\na sprig of wild thyme still remembers the rain.", true),
                new WritingPrompt("PROMPT", "Ursine Ridgeline in Cloud",
                        "Write from the perspective of a solitary creature moving silently through pine and mountain fog at dawn, where the boundary between body and landscape dissolves.",
                        "Nature Poetry", "Serene", "Intermediate", "Inspired by Plumwood Mountain Journal",
                        "This is the one where I dream myself ursine,\na silent breath of fur between pine\non that ridgeline crowded with cloud.", false),
                new WritingPrompt("PROMPT", "The Archway at Twilight",
                        "Stand before a vine-draped stone archway at dusk where fireflies begin to rise. Describe what lies on the other side of the threshold using sensory details of moss, iron, and starlight.",
                        "Romantic Poetry", "Ethereal", "All Levels", "PoetryVerse Editorial",
                        "Where every archway leads to adventure,\nand the climbing jasmine keeps the key.", false),
                new WritingPrompt("PROMPT", "Letters Sealed in Amber Wax",
                        "Compose an epistolary poem addressed to your future self ten autumns from now, sealed in an envelope of warm ivory paper.",
                        "Romantic Poetry", "Hopeful", "All Levels", "Aubade Pages Collection",
                        "Do you still keep the brass lamp lit beside the window when October turns the leaves?", false),
                new WritingPrompt("PROMPT", "Rain on the Conservatory Glass",
                        "Write a poem anchored entirely by sound—the rhythm of rain on glass, the ticking of a grandfather clock, and the turning of a page.",
                        "Free Verse", "Contemplative", "Intermediate", "PoetryVerse Editorial",
                        "Listen: the glass translates the storm into a language older than grief.", false),

                // CHALLENGES
                new WritingPrompt("CHALLENGE", "The Fourteen-Line Golden Sonnet",
                        "Compose a classical 14-line sonnet using imagery of antique gold leaf, candlelight, and autumn wind. Include a dramatic turn (volta) at line 9 and conclude with a rhyming couplet.",
                        "Sonnet", "Romantic", "Advanced", "PoetryVerse Masterclass",
                        "When amber lanterns gild the orchard bough,\nAnd evening folds its cloak across the hill...", false),
                new WritingPrompt("CHALLENGE", "Three-Stanza Haiku Triptych",
                        "Write three linked Haiku (5-7-5 syllables each) tracing the transition from dawn mist to midday sun to midnight frost on a mountain ridgeline.",
                        "Haiku", "Serene", "Intermediate", "Seasonal Kigo Workshop",
                        "White mist on the pine (5)\nSilent breath across the ridge (7)\nAutumn bell rings slow (5)", false),
                new WritingPrompt("CHALLENGE", "The Epistolary Elegy Without Adjectives",
                        "Write an eight-line poem of parting or remembrance using only nouns and verbs—letting concrete objects carry the emotional weight.",
                        "Sad & Elegiac", "Melancholic", "Advanced", "Craft & Restraint Challenge",
                        "The teacup cools upon the sill;\nthe rain erases footsteps from the stone.", false),

                // QUOTES
                new WritingPrompt("QUOTE", "Rainer Maria Rilke on Solitude",
                        "Go into yourself and see how deep the place is from which your life flows; at its source you will find the answer to the question of whether you must create.",
                        "Free Verse", "Contemplative", "All Levels", "Rainer Maria Rilke, Letters to a Young Poet",
                        "Accept the weight of every word as if it were drawn from silence itself.", true),
                new WritingPrompt("QUOTE", "Emily Dickinson on Poetic Truth",
                        "If I read a book and it makes my whole body so cold no fire can ever warm me, I know that is poetry. If I feel physically as if the top of my head were taken off, I know that is poetry.",
                        "Free Verse", "Passionate", "All Levels", "Emily Dickinson (1870)",
                        "Tell all the truth but tell it slant — Success in Circuit lies.", false),
                new WritingPrompt("QUOTE", "Constantine P. Cavafy on the Journey",
                        "Keep Ithaca always in your mind. Arriving there is what you are destined for. But do not hurry the journey at all.",
                        "Life & Philosophy", "Philosophical", "All Levels", "Constantine P. Cavafy (1911)",
                        "Ithaca gave you the marvelous journey. Without her you would not have set out.", true),

                // PUBLIC DOMAIN POEMS
                new WritingPrompt("PUBLIC_DOMAIN", "Ithaca",
                        "When you set out on your journey to Ithaca,\npray that the road is long,\nfull of adventure, full of knowledge.\nThe Lestrygonians and the Cyclops,\nthe angry Poseidon — do not fear them:\nYou will never find such as these on your path,\nif your thoughts remain lofty, if a fine\nemotion touches your spirit and your body.\n\nPray that the road is long.\nThat the summer mornings are many, when,\nwith such pleasure, with such joy\nyou will enter ports seen for the first time;\nstop at Phoenician markets,\nand purchase fine merchandise,\nmother-of-pearl and coral, amber and ebony,\nand sensual perfumes of all kinds,\nas many sensual perfumes as you can;\nvisit many Egyptian cities,\nto learn and learn from scholars.\n\nAlways keep Ithaca in your mind.\nTo arrive there is your ultimate goal.\nBut do not hurry the voyage at all.\nIt is better to let it last for many years;\nand to anchor at the island when you are old,\nrich with all you have gained on the way,\nnot expecting that Ithaca will offer you riches.",
                        "Life & Philosophy", "Philosophical", "Classic", "Constantine P. Cavafy (1911, Public Domain)",
                        "When you set out on your journey to Ithaca, pray that the road is long, full of adventure, full of knowledge.", true),
                new WritingPrompt("PUBLIC_DOMAIN", "Hope is the Thing with Feathers",
                        "“Hope” is the thing with feathers -\nThat perches in the soul -\nAnd sings the tune without the words -\nAnd never stops - at all -\n\nAnd sweetest - in the Gale - is heard -\nAnd sore must be the storm -\nThat could abash the little Bird\nThat kept so many warm -\n\nI’ve heard it in the chillest land -\nAnd on the strangest Sea -\nYet - never - in Extremity,\nIt asked a crumb - of me.",
                        "Inspirational Poetry", "Hopeful", "Classic", "Emily Dickinson (1891, Public Domain)",
                        "“Hope” is the thing with feathers - That perches in the soul...", false),
                new WritingPrompt("PUBLIC_DOMAIN", "The Lake Isle of Innisfree",
                        "I will arise and go now, and go to Innisfree,\nAnd a small cabin build there, of clay and wattles made:\nNine bean-rows will I have there, a hive for the honey-bee;\nAnd live alone in the bee-loud glade.\n\nAnd I shall have some peace there, for peace comes dropping slow,\nDropping from the veils of the morning to where the cricket sings;\nThere midnight’s all a glimmer, and noon a purple glow,\nAnd evening full of the linnet’s wings.\n\nI will arise and go now, for always night and day\nI hear lake water lapping with low sounds by the shore;\nWhile I stand on the roadway, or on the pavements grey,\nI hear it in the deep heart’s core.",
                        "Nature Poetry", "Serene", "Classic", "William Butler Yeats (1890, Public Domain)",
                        "And I shall have some peace there, for peace comes dropping slow...", false),
                new WritingPrompt("PUBLIC_DOMAIN", "There Will Come Soft Rains",
                        "There will come soft rains and the smell of the ground,\nAnd swallows circling with their shimmering sound;\n\nAnd frogs in the pools singing at night,\nAnd wild plum trees in tremulous white,\n\nRobins will wear their feathery fire\nWhistling their whims on a low fence-wire;\n\nAnd not one will know of the war, not one\nWill care at last when it is done.\n\nNot one would mind, neither bird nor tree\nIf mankind perished utterly;\n\nAnd Spring herself, when she woke at dawn,\nWould scarcely know that we were gone.",
                        "Nature Poetry", "Contemplative", "Classic", "Sara Teasdale (1920, Public Domain)",
                        "There will come soft rains and the smell of the ground...", false)
        );

        promptRepository.saveAll(prompts);
    }

    private void seedDemoPoetIfEmpty() {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding flagship demo poet account 'clara_vance' with collections, poems, journal, and analytics...");

        User clara = userService.registerNewPoet(
                "Clara Evangeline Vance",
                "clara_vance",
                "clara@poetryverse.art",
                "PoetVerse2026!",
                "C. E. Vance"
        );
        clara.setBio("Botanical archivist and lyric poet exploring memory, wild olive groves, coastal fog, and the quiet architecture of human longing.");
        clara.setLocation("Bath, Somerset & Kyoto");
        clara.setWebsite("https://poetryverse.art/clara");
        clara.setFavoritePoet("Constantine P. Cavafy & Emily Dickinson");
        clara.setSignatureQuote("Where words find their forever between pressed leaves and linen.");
        clara.setCurrentStreak(9);
        clara.setLongestStreak(24);
        userRepository.save(clara);

        List<PoemCollection> cols = collectionRepository.findByUserOrderByCreatedAtAsc(clara);
        Long colLove = cols.get(0).getId();
        Long colHeartbreak = cols.get(1).getId();
        Long colNature = cols.get(2).getId();
        Long colPhilosophy = cols.get(3).getId();
        Long colDreams = cols.get(4).getId();
        Long colSpirit = cols.get(5).getId();

        // Poem 1: Inspired by Reference Image 3 (Plumwood Mountain Journal)
        poemService.saveOrUpdatePoem(
                clara, null,
                "Dreaming Myself Ursine on the Ridgeline",
                "Field notes from the cloud-crowded pine crest",
                "This is the one where I dream\nmyself ursine, a silent breath\nof fur between pine on that\nridgeline crowded with cloud.\n\nBelow the granite ledge, the valley\nholds its bowl of silver mist\nlike cold milk in an earthen vessel.\n\nNo road climbs here, no iron bell\ndivides the morning into hours—\nonly the resinous hush of needles\nunder a heavy paw, and the wind\nturning the pages of the mountain.",
                null,
                "Nature Poetry", "Serene", "PUBLISHED",
                "Cormorant Garamond", 22, 1.9, "LEFT",
                "IVORY_MANUSCRIPT", "BOTANICAL_GOLD",
                "/images/bg-forest-mist.svg", "/images/cover-pine-ridge.svg",
                "Stefanie & the Mountain Keepers",
                "Inspired by high-altitude pine forests and coastal breakers.",
                "nature",
                "pine, ridgeline, cloud, botanical, solitude",
                List.of(colNature, colDreams),
                true
        );

        // Poem 2: Inspired by Reference Image 4 (Ithaca Botanical Framed Print)
        poemService.saveOrUpdatePoem(
                clara, null,
                "Ithaca — The Long Voyage",
                "After Constantine P. Cavafy",
                "When you set out on your journey to Ithaca,\npray that the road is long,\nfull of adventure, full of knowledge.\nThe Lestrygonians and the Cyclops,\nthe angry Poseidon — do not fear them:\nYou will never find such as these on your path,\nif your thoughts remain lofty, if a fine\nemotion touches your spirit and your body.\n\nPray that the road is long.\nThat the summer mornings are many, when,\nwith such pleasure, with such joy\nyou will enter ports seen for the first time;\nstop at Phoenician markets,\nand purchase fine merchandise,\nmother-of-pearl and coral, amber and ebony,\nand sensual perfumes of all kinds,\nas many sensual perfumes as you can;\nvisit many Egyptian cities,\nto learn and learn from scholars.\n\nAlways keep Ithaca in your mind.\nTo arrive there is your ultimate goal.\nBut do not hurry the voyage at all.\nIt is better to let it last for many years;\nand to anchor at the island when you are old,\nrich with all you have gained on the way,\nnot expecting that Ithaca will offer you riches.",
                null,
                "Life & Philosophy", "Philosophical", "PUBLISHED",
                "Cormorant Garamond", 19, 1.8, "RIGHT",
                "BOTANICAL_EUCALYPTUS", "WOOD_GALLERY",
                "/images/bg-eucalyptus.svg", "/images/cover-ithaca.svg",
                "Every traveler setting sail at dawn",
                "Set in right-aligned classical manuscript format with eucalyptus botanical corners.",
                "inspirational",
                "ithaca, voyage, philosophy, cavafy, mediterranean",
                List.of(colPhilosophy, colSpirit),
                true
        );

        // Poem 3: Inspired by Reference Image 5 (Aubade Pages — Love Letter)
        poemService.saveOrUpdatePoem(
                clara, null,
                "Aubade in the Olive Grove",
                "Like a love letter sealed in antique gold",
                "Before the sun unclasps its shutter of gold\nacross the linen of our quiet table,\nI gather the fallen blossoms from the sill\nand press them where your hand rested at dusk.\n\nYou deserve a sanctuary that feels\nlike an unhurried letter written by candlelight—\nwhere even the shadows of the olive boughs\nlean inward just to listen to you breathe.\n\nLet the world hurry past the garden wall;\nhere, between warm ivory and green leaf,\nour syllables have found their forever.",
                null,
                "Romantic Poetry", "Romantic", "PUBLISHED",
                "Playfair Display", 20, 1.85, "CENTER",
                "IVORY_MANUSCRIPT", "ORNATE_GOLD",
                null, null,
                "My beloved at daybreak",
                "Composed at dawn with black coffee and pressed white lilies.",
                "romantic",
                "aubade, romance, olive-grove, gold, letter",
                List.of(colLove),
                true
        );

        // Poem 4: Inspired by Reference Image 1 (Where Every Archway Leads to Adventure)
        poemService.saveOrUpdatePoem(
                clara, null,
                "Where Every Archway Leads to Adventure",
                "Nocturne for the Botanical Cloister",
                "Through the dark emerald cloister of the night,\nwhere terracotta peonies drink the dew\nand fireflies drift like embers from a lute,\nevery stone archway opens on a world.\n\nStep softly past the twining ivory vine:\nthe garden keeps its secrets for the brave\nwho read the constellations in the moss\nand trust the lantern of the quiet heart.",
                null,
                "Inspirational Poetry", "Ethereal", "PUBLISHED",
                "Cormorant Garamond", 21, 1.85, "CENTER",
                "DEEP_OLIVE_PARCHMENT", "ORNATE_GOLD",
                null, null,
                "The Seekers of the Cloister",
                "Inspired by nocturnal botanical gardens and illuminated manuscripts.",
                "inspirational",
                "archway, adventure, botanical, fireflies, cloister",
                List.of(colDreams, colNature),
                false
        );

        // Poem 5: Heartbreak & Elegy
        poemService.saveOrUpdatePoem(
                clara, null,
                "The Teacup Left Upon the Sill",
                "Elegy for an October afternoon",
                "Nothing in the parlor has been moved\nsince the October rain began to fall;\nyour spectacles still rest beside the lamp,\nyour coat still keeps its shadow on the wall.\n\nHow strange that porcelain outlives the hand\nthat warmed it in the chill of early dawn—\nand how a room can hold so much of you\nlong after every spoken word is gone.",
                null,
                "Sad & Elegiac", "Melancholic", "PUBLISHED",
                "Cormorant Garamond", 20, 1.85, "LEFT",
                "IVORY_MANUSCRIPT", "MINIMAL_HAIRLINE",
                null, null,
                null,
                "Written during the first autumn rain.",
                "sad",
                "elegy, autumn, rain, memory, porcelain",
                List.of(colHeartbreak),
                false
        );

        // Poem 6: Haiku Triptych (Draft)
        poemService.saveOrUpdatePoem(
                clara, null,
                "Three Studies in Olive & Mist",
                "Seasonal Haiku Triptych",
                "White mist on the pine,\nSilent breath across the ridge,\nAutumn bell rings slow.\n\nPale gold olive leaf,\nResting on the mossy well,\nRipples hold the sky.\n\nCandle in the hall,\nInk dries on the linen sheet,\nMidnight Cricket sings.",
                null,
                "Haiku", "Serene", "DRAFT",
                "Cormorant Garamond", 22, 2.0, "CENTER",
                "IVORY_MANUSCRIPT", "BOTANICAL_GOLD",
                null, null,
                null,
                "Working draft exploring 5-7-5 seasonal kigo.",
                "haiku",
                "haiku, mist, olive, zen",
                List.of(colNature, colSpirit),
                false
        );

        // Seed 5 Journal Entries across the current month
        LocalDate today = LocalDate.now();
        seedJournal(clara, today, "Morning Light on the Herbarium Desk",
                "Woke before six while the mist still hung low over the garden wall. Brewed Darjeeling tea and arranged three pressed eucalyptus stems beside my notebook. The line 'This is the one where I dream myself ursine' kept returning like a tide.",
                "Serene", "Mist & Morning Dew", "A solitary pine branch heavy with cloud", "Conservatory Desk", true);

        seedJournal(clara, today.minusDays(1), "Notes on Cavafy's Cadence",
                "Re-read Constantine Cavafy's 'Ithaca' aloud tonight by lamp-light. Notice how the repetition of 'Pray that the road is long' acts not as a delay, but as a benediction. I want my next collection to carry that same unhurried dignity.",
                "Philosophical", "Clear Starlight", "Mother-of-pearl, coral, amber, and ebony", "Library Alcove", false);

        seedJournal(clara, today.minusDays(3), "Pressed Peonies & Antique Gold",
                "Visited the old bookbinder in the lane. Found a roll of deep olive bookcloth (#353626) and warm cream cotton rag paper. There is something deeply comforting about physical craft when composing digital manuscripts.",
                "Nostalgic", "Golden Autumn Sun", "Baroque gilt frames in a quiet gallery", "Bath Bookbindery", true);

        seedJournal(clara, today.minusDays(5), "Fragments from the Rain",
                "Rain drummed against the conservatory roof all afternoon. Wrote two quatrains about an untouched teacup cooling on a windowsill. Sometimes restraint speaks louder than ornament.",
                "Melancholic", "Steady Autumn Rain", "Porcelain & rain-streaked glass", "Parlor Window", false);

        seedJournal(clara, today.minusDays(8), "The Archway at Dusk",
                "Walked through the botanical archway just as the first fireflies flickered among the climbing roses. Sketching a new sequence on thresholds and nocturnal gardens.",
                "Hopeful", "Warm Twilight Breeze", "Where every archway leads to adventure", "Botanical Cloister", false);

        // Seed 14 Days of Historical Writing Statistics for rich Analytics Charts
        int[] wordsHistory = {180, 240, 120, 310, 275, 195, 360, 220, 410, 165, 290, 340, 260, 385};
        int[] poemsHistory = {1, 2, 1, 2, 1, 1, 3, 1, 3, 1, 2, 2, 1, 2};
        String[] genresHistory = {"Free Verse", "Romantic Poetry", "Haiku", "Nature Poetry", "Sonnet", "Life & Philosophy", "Nature Poetry"};

        for (int i = 0; i < 14; i++) {
            LocalDate date = today.minusDays(13 - i);
            WritingStatistic ws = statisticRepository.findByUserAndActivityDate(clara, date)
                    .orElseGet(WritingStatistic::new);
            ws.setUser(clara);
            ws.setActivityDate(date);
            ws.setWordsWritten(wordsHistory[i]);
            ws.setCharactersWritten(wordsHistory[i] * 5 + 42);
            ws.setPoemsCreated(i % 2 == 0 ? 1 : 0);
            ws.setPoemsEdited(poemsHistory[i]);
            ws.setMinutesSpent(25 + (i * 4));
            ws.setDominantGenre(genresHistory[i % genresHistory.length]);
            ws.setDominantMood(i % 2 == 0 ? "Serene" : "Romantic");
            statisticRepository.save(ws);
        }

        // Also seed statistics for the previous 5 months so Monthly Activity Bar Charts are rich
        for (int m = 1; m <= 5; m++) {
            LocalDate monthSampleDate = today.minusMonths(m).withDayOfMonth(15);
            WritingStatistic mStat = new WritingStatistic();
            mStat.setUser(clara);
            mStat.setActivityDate(monthSampleDate);
            mStat.setWordsWritten(1450 + (m * 230));
            mStat.setCharactersWritten((1450 + (m * 230)) * 5);
            mStat.setPoemsCreated(4 + (m % 3));
            mStat.setPoemsEdited(6 + m);
            mStat.setMinutesSpent(210 + (m * 25));
            mStat.setDominantGenre(genresHistory[m % genresHistory.length]);
            mStat.setDominantMood("Contemplative");
            statisticRepository.save(mStat);
        }

        log.info("Demo poet 'clara_vance' seeded successfully.");
    }

    private void seedJournal(User user, LocalDate date, String title, String content,
                             String mood, String weather, String spark, String location, boolean pinned) {
        JournalEntry entry = new JournalEntry();
        entry.setUser(user);
        entry.setEntryDate(date);
        entry.setTitle(title);
        entry.setContent(content);
        entry.setMood(mood);
        entry.setWeather(weather);
        entry.setInspirationSpark(spark);
        entry.setLocationNote(location);
        entry.setPinned(pinned);
        entry.recalculateWords();
        journalRepository.save(entry);
    }
}
