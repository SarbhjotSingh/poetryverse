package com.poetryverse.service;

import com.poetryverse.entity.*;
import com.poetryverse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoemService {

    private final PoemRepository poemRepository;
    private final CollectionRepository collectionRepository;
    private final TagRepository tagRepository;
    private final FavoriteRepository favoriteRepository;
    private final WritingStatisticRepository statisticRepository;
    private final UserService userService;

    public PoemService(PoemRepository poemRepository,
                       CollectionRepository collectionRepository,
                       TagRepository tagRepository,
                       FavoriteRepository favoriteRepository,
                       WritingStatisticRepository statisticRepository,
                       UserService userService) {
        this.poemRepository = poemRepository;
        this.collectionRepository = collectionRepository;
        this.tagRepository = tagRepository;
        this.favoriteRepository = favoriteRepository;
        this.statisticRepository = statisticRepository;
        this.userService = userService;
    }

    public List<Poem> getUserPoems(User user, String search, String genre, String mood,
                                   String tag, String status, Boolean archived, String sort) {
        List<Poem> baseList;
        if (Boolean.TRUE.equals(archived)) {
            baseList = poemRepository.findByUserAndArchivedTrueOrderByUpdatedAtDesc(user);
        } else {
            baseList = poemRepository.findByUserAndArchivedFalseOrderByUpdatedAtDesc(user);
        }

        Set<Long> favPoemIds = favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(f -> f.getPoem().getId())
                .collect(Collectors.toSet());

        for (Poem p : baseList) {
            p.setFavorite(favPoemIds.contains(p.getId()));
        }

        List<Poem> filtered = baseList.stream()
                .filter(p -> {
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        boolean matchTitle = p.getTitle() != null && p.getTitle().toLowerCase().contains(q);
                        boolean matchSub = p.getSubtitle() != null && p.getSubtitle().toLowerCase().contains(q);
                        boolean matchContent = p.getContent() != null && p.getContent().toLowerCase().contains(q);
                        boolean matchTag = p.getTags().stream().anyMatch(t -> t.getName().toLowerCase().contains(q));
                        if (!matchTitle && !matchSub && !matchContent && !matchTag) return false;
                    }
                    if (genre != null && !genre.isBlank() && !"ALL".equalsIgnoreCase(genre)) {
                        if (p.getGenre() == null || !p.getGenre().equalsIgnoreCase(genre.trim())) return false;
                    }
                    if (mood != null && !mood.isBlank() && !"ALL".equalsIgnoreCase(mood)) {
                        if (p.getMood() == null || !p.getMood().equalsIgnoreCase(mood.trim())) return false;
                    }
                    if (tag != null && !tag.isBlank() && !"ALL".equalsIgnoreCase(tag)) {
                        boolean hasTag = p.getTags().stream().anyMatch(t -> t.getName().equalsIgnoreCase(tag.trim()));
                        if (!hasTag) return false;
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (p.getStatus() == null || !p.getStatus().equalsIgnoreCase(status.trim())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        String sortMode = (sort == null || sort.isBlank()) ? "recently_edited" : sort.toLowerCase();
        switch (sortMode) {
            case "newest":
                filtered.sort(Comparator.comparing(Poem::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "oldest":
                filtered.sort(Comparator.comparing(Poem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "title":
                filtered.sort(Comparator.comparing(p -> p.getTitle() == null ? "" : p.getTitle().toLowerCase()));
                break;
            case "words":
                filtered.sort(Comparator.comparingInt(Poem::getWordCount).reversed());
                break;
            case "recently_edited":
            default:
                filtered.sort(Comparator.comparing(Poem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
        }

        return filtered;
    }

    public List<Poem> getRecentPoems(User user, int limit) {
        List<Poem> list = poemRepository.findTop6ByUserAndArchivedFalseOrderByUpdatedAtDesc(user);
        Set<Long> favIds = favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(f -> f.getPoem().getId())
                .collect(Collectors.toSet());
        list.forEach(p -> p.setFavorite(favIds.contains(p.getId())));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    public Poem getPoemForUser(User user, Long poemId) {
        Poem poem = poemRepository.findByIdAndUser(poemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Poem not found in your library."));
        poem.setFavorite(favoriteRepository.existsByUserAndPoem(user, poem));
        return poem;
    }

    @Transactional
    public Poem saveOrUpdatePoem(User user, Long id, String title, String subtitle,
                                 String content, String htmlContent, String genre, String mood,
                                 String status, String fontFamily, Integer fontSize,
                                 Double lineSpacing, String textAlign, String paperStyle,
                                 String frameStyle, String backgroundImage, String coverArtwork,
                                 String dedication, String authorNote, String templateUsed,
                                 String tagsCsv, List<Long> collectionIds, Boolean favoriteFlag) {
        boolean isNew = (id == null);
        Poem poem = isNew ? new Poem() : poemRepository.findByIdAndUser(id, user).orElseGet(Poem::new);
        if (poem.getId() == null) {
            isNew = true;
        }

        poem.setUser(user);
        poem.setTitle((title == null || title.isBlank()) ? "Untitled Verse" : title.trim());
        poem.setSubtitle(subtitle != null ? subtitle.trim() : null);
        poem.setContent((content == null || content.isBlank()) ? "..." : content);
        poem.setHtmlContent(htmlContent);
        if (genre != null && !genre.isBlank()) poem.setGenre(genre.trim());
        if (mood != null && !mood.isBlank()) poem.setMood(mood.trim());
        if (status != null && !status.isBlank()) poem.setStatus(status.trim().toUpperCase());
        if (fontFamily != null && !fontFamily.isBlank()) poem.setFontFamily(fontFamily.trim());
        if (fontSize != null && fontSize >= 12) poem.setFontSize(fontSize);
        if (lineSpacing != null && lineSpacing >= 1.0) poem.setLineSpacing(lineSpacing);
        if (textAlign != null && !textAlign.isBlank()) poem.setTextAlign(textAlign.trim().toUpperCase());
        if (paperStyle != null && !paperStyle.isBlank()) poem.setPaperStyle(paperStyle.trim());
        if (frameStyle != null && !frameStyle.isBlank()) poem.setFrameStyle(frameStyle.trim());
        if (backgroundImage != null) poem.setBackgroundImage(backgroundImage.trim());
        if (coverArtwork != null) poem.setCoverArtwork(coverArtwork.trim());
        if (dedication != null) poem.setDedication(dedication.trim());
        if (authorNote != null) poem.setAuthorNote(authorNote.trim());
        if (templateUsed != null && !templateUsed.isBlank()) poem.setTemplateUsed(templateUsed.trim());

        poem.recalculateMetrics();

        // Resolve tags
        if (tagsCsv != null) {
            Set<Tag> resolvedTags = new HashSet<>();
            String[] parts = tagsCsv.split(",");
            for (String raw : parts) {
                String cleanTag = raw.trim().replaceAll("^#+", "");
                if (!cleanTag.isEmpty()) {
                    Tag tagEntity = tagRepository.findByUserAndNameIgnoreCase(user, cleanTag)
                            .orElseGet(() -> tagRepository.save(new Tag(user, cleanTag)));
                    resolvedTags.add(tagEntity);
                }
            }
            poem.setTags(resolvedTags);
        }

        // Resolve collections
        if (collectionIds != null) {
            Set<PoemCollection> cols = new HashSet<>();
            for (Long colId : collectionIds) {
                if (colId != null) {
                    collectionRepository.findByIdAndUser(colId, user).ifPresent(cols::add);
                }
            }
            poem.setCollections(cols);
        }

        Poem saved = poemRepository.save(poem);

        // Handle favorite flag if passed
        if (favoriteFlag != null) {
            boolean currentlyFav = favoriteRepository.existsByUserAndPoem(user, saved);
            if (favoriteFlag && !currentlyFav) {
                favoriteRepository.save(new Favorite(user, saved));
            } else if (!favoriteFlag && currentlyFav) {
                favoriteRepository.deleteByUserAndPoem(user, saved);
            }
        }
        saved.setFavorite(favoriteRepository.existsByUserAndPoem(user, saved));

        // Update daily writing statistics & streak
        recordActivity(user, saved, isNew);

        return saved;
    }

    @Transactional
    public void recordActivity(User user, Poem poem, boolean isNew) {
        LocalDate today = LocalDate.now();
        WritingStatistic stat = statisticRepository.findByUserAndActivityDate(user, today)
                .orElseGet(() -> {
                    WritingStatistic ws = new WritingStatistic();
                    ws.setUser(user);
                    ws.setActivityDate(today);
                    return ws;
                });

        if (isNew) {
            stat.setPoemsCreated(stat.getPoemsCreated() + 1);
            stat.setWordsWritten(stat.getWordsWritten() + poem.getWordCount());
            stat.setCharactersWritten(stat.getCharactersWritten() + poem.getCharacterCount());
        } else {
            stat.setPoemsEdited(stat.getPoemsEdited() + 1);
            stat.setWordsWritten(Math.max(stat.getWordsWritten(), poem.getWordCount()));
            stat.setCharactersWritten(Math.max(stat.getCharactersWritten(), poem.getCharacterCount()));
        }
        stat.setMinutesSpent(stat.getMinutesSpent() + 5);
        stat.setDominantGenre(poem.getGenre());
        stat.setDominantMood(poem.getMood());
        statisticRepository.save(stat);

        userService.recordWritingStreak(user);
    }

    @Transactional
    public Poem duplicatePoem(User user, Long poemId) {
        Poem original = getPoemForUser(user, poemId);
        Poem copy = new Poem();
        copy.setUser(user);
        copy.setTitle(original.getTitle() + " (Manuscript Copy)");
        copy.setSubtitle(original.getSubtitle());
        copy.setContent(original.getContent());
        copy.setHtmlContent(original.getHtmlContent());
        copy.setGenre(original.getGenre());
        copy.setMood(original.getMood());
        copy.setStatus("DRAFT");
        copy.setFontFamily(original.getFontFamily());
        copy.setFontSize(original.getFontSize());
        copy.setLineSpacing(original.getLineSpacing());
        copy.setTextAlign(original.getTextAlign());
        copy.setPaperStyle(original.getPaperStyle());
        copy.setFrameStyle(original.getFrameStyle());
        copy.setBackgroundImage(original.getBackgroundImage());
        copy.setCoverArtwork(original.getCoverArtwork());
        copy.setDedication(original.getDedication());
        copy.setAuthorNote(original.getAuthorNote());
        copy.setTags(new HashSet<>(original.getTags()));
        copy.setCollections(new HashSet<>(original.getCollections()));
        copy.recalculateMetrics();
        return poemRepository.save(copy);
    }

    @Transactional
    public Poem toggleArchive(User user, Long poemId) {
        Poem poem = getPoemForUser(user, poemId);
        poem.setArchived(!poem.isArchived());
        return poemRepository.save(poem);
    }

    @Transactional
    public void deletePoem(User user, Long poemId) {
        Poem poem = getPoemForUser(user, poemId);
        favoriteRepository.deleteByUserAndPoem(user, poem);
        poemRepository.delete(poem);
    }

    public List<String> getAllGenresForUser(User user) {
        Set<String> genres = new LinkedHashSet<>(Arrays.asList(
                "Free Verse", "Romantic Poetry", "Sonnet", "Haiku",
                "Nature Poetry", "Acrostic Poetry", "Inspirational Poetry",
                "Sad & Elegiac", "Life & Philosophy", "Lyric & Ode"
        ));
        poemRepository.findByUserOrderByUpdatedAtDesc(user).forEach(p -> {
            if (p.getGenre() != null && !p.getGenre().isBlank()) {
                genres.add(p.getGenre());
            }
        });
        return new ArrayList<>(genres);
    }

    public List<String> getAllMoodsForUser(User user) {
        Set<String> moods = new LinkedHashSet<>(Arrays.asList(
                "Contemplative", "Romantic", "Melancholic", "Serene",
                "Nostalgic", "Hopeful", "Wistful", "Ethereal", "Passionate", "Philosophical"
        ));
        poemRepository.findByUserOrderByUpdatedAtDesc(user).forEach(p -> {
            if (p.getMood() != null && !p.getMood().isBlank()) {
                moods.add(p.getMood());
            }
        });
        return new ArrayList<>(moods);
    }

    public List<Tag> getUserTags(User user) {
        return tagRepository.findByUserOrderByNameAsc(user);
    }
}
