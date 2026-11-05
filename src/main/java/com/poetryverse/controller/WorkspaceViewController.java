package com.poetryverse.controller;

import com.poetryverse.entity.*;
import com.poetryverse.repository.WritingPromptRepository;
import com.poetryverse.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

@Controller
public class WorkspaceViewController {

    private final UserService userService;
    private final PoemService poemService;
    private final CollectionService collectionService;
    private final FavoriteService favoriteService;
    private final JournalService journalService;
    private final PoetryTemplateService templateService;
    private final AnalyticsService analyticsService;
    private final WritingPromptRepository promptRepository;

    public WorkspaceViewController(UserService userService,
                                   PoemService poemService,
                                   CollectionService collectionService,
                                   FavoriteService favoriteService,
                                   JournalService journalService,
                                   PoetryTemplateService templateService,
                                   AnalyticsService analyticsService,
                                   WritingPromptRepository promptRepository) {
        this.userService = userService;
        this.poemService = poemService;
        this.collectionService = collectionService;
        this.favoriteService = favoriteService;
        this.journalService = journalService;
        this.templateService = templateService;
        this.analyticsService = analyticsService;
        this.promptRepository = promptRepository;
    }

    private User populateCommonContext(Model model, String activeNav) {
        User user = userService.requireAuthenticatedUser();
        UserSettings settings = userService.getOrCreateSettings(user);
        model.addAttribute("currentUser", user);
        model.addAttribute("userSettings", settings);
        model.addAttribute("activeNav", activeNav);
        return user;
    }

    // 4. MAIN DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = populateCommonContext(model, "dashboard");
        Map<String, Object> analytics = analyticsService.buildFullAnalytics(user);
        model.addAllAttributes(analytics);

        List<Poem> recentPoems = poemService.getRecentPoems(user, 6);
        model.addAttribute("recentPoems", recentPoems);

        List<PoemCollection> collections = collectionService.getUserCollections(user);
        model.addAttribute("collections", collections);

        List<JournalEntry> recentJournals = journalService.getJournalEntries(user, null, null, null);
        model.addAttribute("recentJournals", recentJournals.size() > 3 ? recentJournals.subList(0, 3) : recentJournals);

        model.addAttribute("dailyQuote", templateService.getDailyQuote());
        model.addAttribute("randomPrompt", templateService.getRandomInspiration());

        // Random poem suggestion from user's own library or public domain
        if (!recentPoems.isEmpty()) {
            Poem suggested = recentPoems.get(new Random().nextInt(recentPoems.size()));
            model.addAttribute("suggestedPoem", suggested);
        }

        return "dashboard";
    }

    // 5. MY POETRY LIBRARY
    @GetMapping("/library")
    public String library(@RequestParam(required = false) String search,
                          @RequestParam(required = false) String genre,
                          @RequestParam(required = false) String mood,
                          @RequestParam(required = false) String tag,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false, defaultValue = "false") Boolean archived,
                          @RequestParam(required = false, defaultValue = "recently_edited") String sort,
                          @RequestParam(required = false, defaultValue = "grid") String view,
                          Model model) {
        User user = populateCommonContext(model, "library");

        List<Poem> poems = poemService.getUserPoems(user, search, genre, mood, tag, status, archived, sort);
        model.addAttribute("poems", poems);
        model.addAttribute("genres", poemService.getAllGenresForUser(user));
        model.addAttribute("moods", poemService.getAllMoodsForUser(user));
        model.addAttribute("tags", poemService.getUserTags(user));
        model.addAttribute("collections", collectionService.getUserCollections(user));

        model.addAttribute("selectedSearch", search != null ? search : "");
        model.addAttribute("selectedGenre", genre != null ? genre : "ALL");
        model.addAttribute("selectedMood", mood != null ? mood : "ALL");
        model.addAttribute("selectedTag", tag != null ? tag : "ALL");
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("selectedArchived", archived);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedView", view);

        return "library";
    }

    // 6. POETRY EDITOR (NEW OR EXISTING)
    @GetMapping({"/editor", "/editor/{id}"})
    public String editor(@PathVariable(required = false) Long id,
                         @RequestParam(required = false) String template,
                         @RequestParam(required = false) Long promptId,
                         @RequestParam(required = false) Long collectionId,
                         Model model) {
        User user = populateCommonContext(model, "editor");
        UserSettings settings = userService.getOrCreateSettings(user);

        Poem poem;
        if (id != null) {
            poem = poemService.getPoemForUser(user, id);
        } else {
            poem = new Poem();
            poem.setFontFamily(settings.getEditorFontFamily());
            poem.setFontSize(settings.getEditorFontSize());
            poem.setLineSpacing(settings.getEditorLineSpacing());
            poem.setGenre(settings.getDefaultGenre());
            poem.setTextAlign(settings.getDefaultAlignment());
            poem.setMood("Contemplative");
            poem.setStatus("DRAFT");
            poem.setPaperStyle("IVORY_MANUSCRIPT");
            poem.setFrameStyle("BOTANICAL_GOLD");

            if (template != null && !template.isBlank()) {
                templateService.getTemplateByKey(template).ifPresent(t -> {
                    poem.setTitle(t.getDefaultTitle());
                    poem.setSubtitle(t.getSubtitle());
                    poem.setContent(t.getStarterContent());
                    poem.setGenre(t.getGenre());
                    poem.setMood(t.getMood());
                    poem.setTemplateUsed(t.getName());
                    poem.recalculateMetrics();
                    model.addAttribute("activeTemplate", t);
                });
            } else if (promptId != null) {
                promptRepository.findById(promptId).ifPresent(pr -> {
                    poem.setTitle(pr.getTitle());
                    poem.setSubtitle("Inspired by: " + pr.getCategory());
                    poem.setContent(pr.getSampleVerse() != null ? pr.getSampleVerse() + "\n\n" : "");
                    poem.setGenre(pr.getSuggestedGenre() != null ? pr.getSuggestedGenre() : "Free Verse");
                    poem.setMood(pr.getSuggestedMood() != null ? pr.getSuggestedMood() : "Contemplative");
                    poem.setAuthorNote("Prompt: " + pr.getPromptText());
                    poem.recalculateMetrics();
                    model.addAttribute("activePrompt", pr);
                });
            }
        }

        model.addAttribute("poem", poem);
        model.addAttribute("collections", collectionService.getUserCollections(user));
        model.addAttribute("preselectedCollectionId", collectionId);
        model.addAttribute("genres", poemService.getAllGenresForUser(user));
        model.addAttribute("moods", poemService.getAllMoodsForUser(user));
        model.addAttribute("templates", templateService.getAllTemplates());

        return "editor";
    }

    // 7. COLLECTIONS & CATEGORIES
    @GetMapping({"/collections", "/collections/{id}"})
    public String collections(@PathVariable(required = false) Long id, Model model) {
        User user = populateCommonContext(model, "collections");
        List<PoemCollection> cols = collectionService.getUserCollections(user);
        model.addAttribute("collections", cols);
        model.addAttribute("allUserPoems", poemService.getUserPoems(user, null, null, null, null, null, false, "title"));

        PoemCollection activeCollection = null;
        if (id != null) {
            activeCollection = collectionService.getCollectionForUser(user, id);
        } else if (!cols.isEmpty()) {
            activeCollection = cols.get(0);
        }

        if (activeCollection != null) {
            model.addAttribute("activeCollection", activeCollection);
            model.addAttribute("collectionPoems", collectionService.getPoemsInCollection(user, activeCollection.getId()));
        }

        return "collections";
    }

    // 8. FAVORITE POEMS
    @GetMapping("/favorites")
    public String favorites(@RequestParam(required = false) String search,
                            @RequestParam(required = false, defaultValue = "recent_favorited") String sort,
                            Model model) {
        User user = populateCommonContext(model, "favorites");
        List<Favorite> favs = favoriteService.getUserFavorites(user, search, sort);
        model.addAttribute("favorites", favs);
        model.addAttribute("selectedSearch", search != null ? search : "");
        model.addAttribute("selectedSort", sort);
        return "favorites";
    }

    // 9. WRITING JOURNAL
    @GetMapping("/journal")
    public String journal(@RequestParam(required = false) String search,
                          @RequestParam(required = false) String mood,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          Model model) {
        User user = populateCommonContext(model, "journal");
        List<JournalEntry> entries = journalService.getJournalEntries(user, search, mood, date);
        model.addAttribute("entries", entries);
        model.addAttribute("selectedSearch", search != null ? search : "");
        model.addAttribute("selectedMood", mood != null ? mood : "ALL");
        model.addAttribute("selectedDate", date);
        model.addAttribute("todayDate", LocalDate.now());
        model.addAttribute("moods", poemService.getAllMoodsForUser(user));
        return "journal";
    }

    // 10. POETRY TEMPLATES
    @GetMapping("/templates")
    public String templates(Model model) {
        populateCommonContext(model, "templates");
        model.addAttribute("templates", templateService.getAllTemplates());
        return "templates";
    }

    // 11. SEARCH & DISCOVER
    @GetMapping("/discover")
    public String discover(@RequestParam(required = false, defaultValue = "ALL") String category,
                           Model model) {
        populateCommonContext(model, "discover");
        model.addAttribute("prompts", templateService.getPromptsByCategory("PROMPT"));
        model.addAttribute("challenges", templateService.getPromptsByCategory("CHALLENGE"));
        model.addAttribute("quotes", templateService.getPromptsByCategory("QUOTE"));
        model.addAttribute("publicDomainPoems", templateService.getPromptsByCategory("PUBLIC_DOMAIN"));
        model.addAttribute("dailyQuote", templateService.getDailyQuote());
        model.addAttribute("featuredInspiration", templateService.getRandomInspiration());
        model.addAttribute("templates", templateService.getAllTemplates());
        model.addAttribute("selectedCategory", category);
        return "discover";
    }

    // 12. STATISTICS & INSIGHTS
    @GetMapping("/statistics")
    public String statistics(Model model) {
        User user = populateCommonContext(model, "statistics");
        Map<String, Object> analytics = analyticsService.buildFullAnalytics(user);
        model.addAllAttributes(analytics);
        return "statistics";
    }

    // 13. USER PROFILE
    @GetMapping("/profile")
    public String profile(Model model) {
        User user = populateCommonContext(model, "profile");
        Map<String, Object> analytics = analyticsService.buildFullAnalytics(user);
        model.addAllAttributes(analytics);
        model.addAttribute("recentPoems", poemService.getRecentPoems(user, 4));
        model.addAttribute("collections", collectionService.getUserCollections(user));
        return "profile";
    }

    // 14. SETTINGS
    @GetMapping("/settings")
    public String settings(Model model) {
        User user = populateCommonContext(model, "settings");
        model.addAttribute("genres", poemService.getAllGenresForUser(user));
        return "settings";
    }
}
