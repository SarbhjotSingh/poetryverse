package com.poetryverse.controller;

import com.poetryverse.entity.*;
import com.poetryverse.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiRestController {

    private final UserService userService;
    private final PoemService poemService;
    private final CollectionService collectionService;
    private final FavoriteService favoriteService;
    private final JournalService journalService;
    private final PoetryTemplateService templateService;
    private final AnalyticsService analyticsService;
    private final ExportImportService exportImportService;

    public ApiRestController(UserService userService,
                             PoemService poemService,
                             CollectionService collectionService,
                             FavoriteService favoriteService,
                             JournalService journalService,
                             PoetryTemplateService templateService,
                             AnalyticsService analyticsService,
                             ExportImportService exportImportService) {
        this.userService = userService;
        this.poemService = poemService;
        this.collectionService = collectionService;
        this.favoriteService = favoriteService;
        this.journalService = journalService;
        this.templateService = templateService;
        this.analyticsService = analyticsService;
        this.exportImportService = exportImportService;
    }

    // ========================================================================
    // 1. POEMS ENDPOINTS
    // ========================================================================

    @GetMapping("/poems/{id}")
    public ResponseEntity<Map<String, Object>> getPoemDetails(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        Poem p = poemService.getPoemForUser(user, id);
        return ResponseEntity.ok(mapPoemToResponse(p));
    }

    @PostMapping("/poems/save")
    public ResponseEntity<Map<String, Object>> saveOrAutoSavePoem(@RequestBody Map<String, Object> payload) {
        try {
            User user = userService.requireAuthenticatedUser();

            Long id = parseLong(payload.get("id"));
            String title = asString(payload.get("title"));
            String subtitle = asString(payload.get("subtitle"));
            String content = asString(payload.get("content"));
            String htmlContent = asString(payload.get("htmlContent"));
            String genre = asString(payload.get("genre"));
            String mood = asString(payload.get("mood"));
            String status = asString(payload.get("status"));
            String fontFamily = asString(payload.get("fontFamily"));
            Integer fontSize = parseInt(payload.get("fontSize"));
            Double lineSpacing = parseDouble(payload.get("lineSpacing"));
            String textAlign = asString(payload.get("textAlign"));
            String paperStyle = asString(payload.get("paperStyle"));
            String frameStyle = asString(payload.get("frameStyle"));
            String backgroundImage = asString(payload.get("backgroundImage"));
            String coverArtwork = asString(payload.get("coverArtwork"));
            String dedication = asString(payload.get("dedication"));
            String authorNote = asString(payload.get("authorNote"));
            String templateUsed = asString(payload.get("templateUsed"));
            String tagsCsv = asString(payload.get("tagsCsv"));
            Boolean favorite = payload.get("favorite") instanceof Boolean ? (Boolean) payload.get("favorite") : null;

            List<Long> collectionIds = new ArrayList<>();
            Object colsObj = payload.get("collectionIds");
            if (colsObj instanceof Collection<?> rawList) {
                for (Object item : rawList) {
                    Long cid = parseLong(item);
                    if (cid != null) collectionIds.add(cid);
                }
            }

            Poem saved = poemService.saveOrUpdatePoem(
                    user, id, title, subtitle, content, htmlContent, genre, mood, status,
                    fontFamily, fontSize, lineSpacing, textAlign, paperStyle, frameStyle,
                    backgroundImage, coverArtwork, dedication, authorNote, templateUsed,
                    tagsCsv, collectionIds, favorite
            );

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", true);
            res.put("message", "Manuscript saved to your sanctuary.");
            res.put("poem", mapPoemToResponse(saved));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/poems/{id}/favorite")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            boolean isFav = favoriteService.toggleFavorite(user, id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "favorite", isFav,
                    "message", isFav ? "Added to your Favorite Manuscripts." : "Removed from Favorites."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/poems/{id}/duplicate")
    public ResponseEntity<Map<String, Object>> duplicatePoem(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            Poem copy = poemService.duplicatePoem(user, id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "poemId", copy.getId(),
                    "message", "Manuscript duplicated as '" + copy.getTitle() + "'."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/poems/{id}/archive")
    public ResponseEntity<Map<String, Object>> toggleArchive(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            Poem updated = poemService.toggleArchive(user, id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "archived", updated.isArchived(),
                    "message", updated.isArchived() ? "Poem moved to Archive Vault." : "Poem restored to active library."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/poems/{id}")
    public ResponseEntity<Map<String, Object>> deletePoem(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            poemService.deletePoem(user, id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Poem permanently removed from library."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
    // 2. EXPORT & IMPORT ENDPOINTS
    // ========================================================================

    @GetMapping("/poems/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPoemPdf(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        Poem poem = poemService.getPoemForUser(user, id);
        byte[] bytes = exportImportService.exportPoemToPdf(user, poem);
        String filename = sanitizeFilename(poem.getTitle()) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/poems/{id}/export/docx")
    public ResponseEntity<byte[]> exportPoemDocx(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        Poem poem = poemService.getPoemForUser(user, id);
        byte[] bytes = exportImportService.exportPoemToDocx(user, poem);
        String filename = sanitizeFilename(poem.getTitle()) + ".docx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(bytes);
    }

    @GetMapping("/poems/{id}/export/txt")
    public ResponseEntity<byte[]> exportPoemTxt(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        Poem poem = poemService.getPoemForUser(user, id);
        byte[] bytes = exportImportService.exportPoemToTxt(user, poem);
        String filename = sanitizeFilename(poem.getTitle()) + ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    @PostMapping("/poems/import/txt")
    public ResponseEntity<Map<String, Object>> importPoemTxt(@RequestParam("file") MultipartFile file,
                                                             @RequestParam(required = false) String genre,
                                                             @RequestParam(required = false) Long collectionId) {
        try {
            User user = userService.requireAuthenticatedUser();
            Poem imported = exportImportService.importPoemFromTxtFile(user, file, genre, collectionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "poemId", imported.getId(),
                    "title", imported.getTitle(),
                    "message", "Imported '" + imported.getTitle() + "' into your library."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/backup/download")
    public ResponseEntity<byte[]> downloadPersonalBackup() {
        User user = userService.requireAuthenticatedUser();
        byte[] bytes = exportImportService.exportFullBackupArchive(user);
        String filename = "PoetryVerse_Archive_" + user.getUsername() + ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    // ========================================================================
    // 3. COLLECTIONS ENDPOINTS
    // ========================================================================

    @PostMapping("/collections/save")
    public ResponseEntity<Map<String, Object>> saveCollection(@RequestBody Map<String, Object> payload) {
        try {
            User user = userService.requireAuthenticatedUser();
            Long id = parseLong(payload.get("id"));
            String name = asString(payload.get("name"));
            String subtitle = asString(payload.get("subtitle"));
            String description = asString(payload.get("description"));
            String coverImage = asString(payload.get("coverImage"));
            String accentColor = asString(payload.get("accentColor"));
            String frameMotif = asString(payload.get("frameMotif"));
            String epigraph = asString(payload.get("epigraph"));
            Boolean featured = payload.get("featured") instanceof Boolean ? (Boolean) payload.get("featured") : null;

            List<Long> assignedPoemIds = null;
            if (payload.get("poemIds") instanceof Collection<?> rawIds) {
                assignedPoemIds = new ArrayList<>();
                for (Object item : rawIds) {
                    Long pid = parseLong(item);
                    if (pid != null) assignedPoemIds.add(pid);
                }
            }

            PoemCollection saved = collectionService.saveOrUpdateCollection(
                    user, id, name, subtitle, description, coverImage,
                    accentColor, frameMotif, epigraph, featured, assignedPoemIds
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "collectionId", saved.getId(),
                    "message", "Collection '" + saved.getName() + "' saved."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/collections/{id}")
    public ResponseEntity<Map<String, Object>> deleteCollection(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            collectionService.deleteCollection(user, id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Collection deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/collections/{collectionId}/assign/{poemId}")
    public ResponseEntity<Map<String, Object>> assignPoemToCollection(@PathVariable Long collectionId,
                                                                      @PathVariable Long poemId) {
        try {
            User user = userService.requireAuthenticatedUser();
            collectionService.assignPoemToCollection(user, collectionId, poemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Poem added to collection."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/collections/{collectionId}/remove/{poemId}")
    public ResponseEntity<Map<String, Object>> removePoemFromCollection(@PathVariable Long collectionId,
                                                                        @PathVariable Long poemId) {
        try {
            User user = userService.requireAuthenticatedUser();
            collectionService.removePoemFromCollection(user, collectionId, poemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Poem removed from collection."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/collections/{id}/export/pdf")
    public ResponseEntity<byte[]> exportCollectionPdf(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        PoemCollection col = collectionService.getCollectionForUser(user, id);
        List<Poem> poems = collectionService.getPoemsInCollection(user, id);
        byte[] bytes = exportImportService.exportCollectionToPdf(user, col, poems);
        String filename = sanitizeFilename(col.getName()) + "_Anthology.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ========================================================================
    // 4. FAVORITES NOTE ENDPOINT
    // ========================================================================

    @PostMapping("/favorites/{poemId}/note")
    public ResponseEntity<Map<String, Object>> saveFavoriteNote(@PathVariable Long poemId,
                                                                @RequestBody Map<String, String> payload) {
        try {
            User user = userService.requireAuthenticatedUser();
            Favorite fav = favoriteService.updateFavoriteNote(user, poemId, payload.get("note"));
            return ResponseEntity.ok(Map.of("success", true, "note", fav.getPersonalNote() != null ? fav.getPersonalNote() : ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
    // 5. JOURNAL ENDPOINTS
    // ========================================================================

    @GetMapping("/journal/{id}")
    public ResponseEntity<JournalEntry> getJournalEntry(@PathVariable Long id) {
        User user = userService.requireAuthenticatedUser();
        return ResponseEntity.ok(journalService.getEntryForUser(user, id));
    }

    @PostMapping("/journal/save")
    public ResponseEntity<Map<String, Object>> saveJournalEntry(@RequestBody Map<String, Object> payload) {
        try {
            User user = userService.requireAuthenticatedUser();
            Long id = parseLong(payload.get("id"));
            String dateStr = asString(payload.get("entryDate"));
            LocalDate entryDate = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();
            String title = asString(payload.get("title"));
            String content = asString(payload.get("content"));
            String mood = asString(payload.get("mood"));
            String weather = asString(payload.get("weather"));
            String spark = asString(payload.get("inspirationSpark"));
            String locationNote = asString(payload.get("locationNote"));
            Boolean pinned = payload.get("pinned") instanceof Boolean ? (Boolean) payload.get("pinned") : null;

            JournalEntry saved = journalService.saveOrUpdateEntry(
                    user, id, entryDate, title, content, mood, weather, spark, locationNote, pinned
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "entryId", saved.getId(),
                    "message", "Journal reflection recorded."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/journal/{id}")
    public ResponseEntity<Map<String, Object>> deleteJournalEntry(@PathVariable Long id) {
        try {
            User user = userService.requireAuthenticatedUser();
            journalService.deleteEntry(user, id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Journal entry deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
    // 6. DISCOVER & "INSPIRE ME" ENDPOINT
    // ========================================================================

    @GetMapping("/discover/inspire")
    public ResponseEntity<WritingPrompt> inspireMe() {
        return ResponseEntity.ok(templateService.getRandomInspiration());
    }

    // ========================================================================
    // 7. ANALYTICS & STATISTICS ENDPOINT
    // ========================================================================

    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        User user = userService.requireAuthenticatedUser();
        return ResponseEntity.ok(analyticsService.buildFullAnalytics(user));
    }

    // ========================================================================
    // 8. PROFILE, SETTINGS & ACCOUNT MANAGEMENT ENDPOINTS
    // ========================================================================

    @PostMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.requireAuthenticatedUser();
            User updated = userService.updateProfile(
                    user,
                    payload.get("fullName"),
                    payload.get("penName"),
                    payload.get("username"),
                    payload.get("email"),
                    payload.get("bio"),
                    payload.get("location"),
                    payload.get("website"),
                    payload.get("favoritePoet"),
                    payload.get("signatureQuote"),
                    payload.get("avatarUrl")
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Poet profile updated.",
                    "penName", updated.getPenName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/settings/update")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody UserSettings incoming) {
        try {
            User user = userService.requireAuthenticatedUser();
            UserSettings saved = userService.updateSettings(user, incoming);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Sanctuary & editor preferences saved.",
                    "settings", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/settings/password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.requireAuthenticatedUser();
            String currentPwd = payload.get("currentPassword");
            String newPwd = payload.get("newPassword");
            String confirmPwd = payload.get("confirmPassword");
            if (confirmPwd != null && !confirmPwd.equals(newPwd)) {
                throw new IllegalArgumentException("New password and confirmation do not match.");
            }
            userService.changePassword(user, currentPwd, newPwd);
            return ResponseEntity.ok(Map.of("success", true, "message", "Your passphrase has been updated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/account/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(HttpServletRequest request) {
        try {
            User user = userService.requireAuthenticatedUser();
            userService.deleteAccount(user);
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/login?deleted=true"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> mapPoemToResponse(Poem p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("subtitle", p.getSubtitle());
        m.put("content", p.getContent());
        m.put("htmlContent", p.getHtmlContent());
        m.put("genre", p.getGenre());
        m.put("mood", p.getMood());
        m.put("status", p.getStatus());
        m.put("archived", p.isArchived());
        m.put("favorite", p.isFavorite());
        m.put("fontFamily", p.getFontFamily());
        m.put("fontSize", p.getFontSize());
        m.put("lineSpacing", p.getLineSpacing());
        m.put("textAlign", p.getTextAlign());
        m.put("paperStyle", p.getPaperStyle());
        m.put("frameStyle", p.getFrameStyle());
        m.put("backgroundImage", p.getBackgroundImage());
        m.put("coverArtwork", p.getCoverArtwork());
        m.put("dedication", p.getDedication());
        m.put("authorNote", p.getAuthorNote());
        m.put("wordCount", p.getWordCount());
        m.put("characterCount", p.getCharacterCount());
        m.put("lineCount", p.getLineCount());
        m.put("stanzaCount", p.getStanzaCount());
        m.put("readingTimeSec", p.getReadingTimeSec());
        m.put("tagsCsv", p.getTagsCsv());
        m.put("tagNames", p.getTagNames());
        m.put("collectionIds", p.getCollectionIds());
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
        m.put("updatedAt", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : "");
        return m;
    }

    private String sanitizeFilename(String input) {
        if (input == null || input.isBlank()) return "PoetryVerse_Manuscript";
        return input.replaceAll("[^a-zA-Z0-9\\-_ ]", "").trim().replaceAll("\\s+", "_");
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Long parseLong(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(Object o) {
        if (o == null) return null;
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Object o) {
        if (o == null) return null;
        try {
            return Double.parseDouble(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
