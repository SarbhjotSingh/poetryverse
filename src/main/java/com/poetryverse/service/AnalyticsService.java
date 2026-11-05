package com.poetryverse.service;

import com.poetryverse.entity.*;
import com.poetryverse.repository.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AnalyticsService {

    private final PoemRepository poemRepository;
    private final CollectionRepository collectionRepository;
    private final FavoriteRepository favoriteRepository;
    private final JournalEntryRepository journalRepository;
    private final WritingStatisticRepository statisticRepository;
    private final UserService userService;

    public AnalyticsService(PoemRepository poemRepository,
                            CollectionRepository collectionRepository,
                            FavoriteRepository favoriteRepository,
                            JournalEntryRepository journalRepository,
                            WritingStatisticRepository statisticRepository,
                            UserService userService) {
        this.poemRepository = poemRepository;
        this.collectionRepository = collectionRepository;
        this.favoriteRepository = favoriteRepository;
        this.journalRepository = journalRepository;
        this.statisticRepository = statisticRepository;
        this.userService = userService;
    }

    public Map<String, Object> buildFullAnalytics(User user) {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Poem> activePoems = poemRepository.findByUserAndArchivedFalseOrderByUpdatedAtDesc(user);
        List<PoemCollection> collections = collectionRepository.findByUserOrderByCreatedAtAsc(user);
        long favoritesCount = favoriteRepository.countByUser(user);
        long journalCount = journalRepository.countByUser(user);

        long totalPoems = activePoems.size();
        long totalWords = poemRepository.sumWordCountByUser(user);
        long totalCharacters = poemRepository.sumCharacterCountByUser(user);
        long totalLines = activePoems.stream().mapToInt(Poem::getLineCount).sum();
        long publishedCount = activePoems.stream().filter(p -> "PUBLISHED".equalsIgnoreCase(p.getStatus())).count();
        long draftCount = totalPoems - publishedCount;

        data.put("totalPoems", totalPoems);
        data.put("totalCollections", collections.size());
        data.put("totalFavorites", favoritesCount);
        data.put("totalJournalEntries", journalCount);
        data.put("totalWords", totalWords);
        data.put("totalCharacters", totalCharacters);
        data.put("totalLines", totalLines);
        data.put("publishedCount", publishedCount);
        data.put("draftCount", draftCount);
        data.put("currentStreak", user.getCurrentStreak());
        data.put("longestStreak", user.getLongestStreak());
        data.put("avgWordsPerPoem", totalPoems > 0 ? Math.round((double) totalWords / totalPoems) : 0);

        // Genre distribution & Favorite Genre
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        Map<String, Integer> moodCounts = new LinkedHashMap<>();
        for (Poem p : activePoems) {
            String g = (p.getGenre() == null || p.getGenre().isBlank()) ? "Free Verse" : p.getGenre();
            genreCounts.put(g, genreCounts.getOrDefault(g, 0) + 1);

            String m = (p.getMood() == null || p.getMood().isBlank()) ? "Contemplative" : p.getMood();
            moodCounts.put(m, moodCounts.getOrDefault(m, 0) + 1);
        }

        String favoriteGenre = genreCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Free Verse");

        String dominantMood = moodCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Contemplative");

        data.put("favoriteGenre", favoriteGenre);
        data.put("dominantMood", dominantMood);
        data.put("genreLabels", new ArrayList<>(genreCounts.keySet()));
        data.put("genreValues", new ArrayList<>(genreCounts.values()));
        data.put("moodLabels", new ArrayList<>(moodCounts.keySet()));
        data.put("moodValues", new ArrayList<>(moodCounts.values()));

        // Collection distribution
        List<String> colLabels = new ArrayList<>();
        List<Integer> colValues = new ArrayList<>();
        List<Integer> colWordValues = new ArrayList<>();
        for (PoemCollection c : collections) {
            colLabels.add(c.getName());
            colValues.add(c.getPoemCount());
            colWordValues.add(c.getTotalWords());
        }
        data.put("collectionLabels", colLabels);
        data.put("collectionPoemCounts", colValues);
        data.put("collectionWordCounts", colWordValues);

        // Daily Writing Activity (Last 14 Days)
        LocalDate today = LocalDate.now();
        LocalDate start14 = today.minusDays(13);
        List<WritingStatistic> stats14 = statisticRepository
                .findByUserAndActivityDateBetweenOrderByActivityDateAsc(user, start14, today);
        Map<LocalDate, WritingStatistic> statByDate = new HashMap<>();
        for (WritingStatistic ws : stats14) {
            statByDate.put(ws.getActivityDate(), ws);
        }

        List<String> dailyLabels = new ArrayList<>();
        List<Integer> dailyWords = new ArrayList<>();
        List<Integer> dailyPoems = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MMM d");

        int weeklyWordsSum = 0;
        for (int i = 0; i < 14; i++) {
            LocalDate d = start14.plusDays(i);
            dailyLabels.add(d.format(dayFmt));
            WritingStatistic ws = statByDate.get(d);
            int w = ws != null ? ws.getWordsWritten() : 0;
            int pCount = ws != null ? (ws.getPoemsCreated() + ws.getPoemsEdited()) : 0;
            dailyWords.add(w);
            dailyPoems.add(pCount);
            if (!d.isBefore(today.minusDays(6))) {
                weeklyWordsSum += w;
            }
        }
        data.put("dailyLabels", dailyLabels);
        data.put("dailyWords", dailyWords);
        data.put("dailyPoems", dailyPoems);
        data.put("weeklyWordsSum", weeklyWordsSum);

        UserSettings settings = userService.getOrCreateSettings(user);
        int dailyGoal = Math.max(50, settings.getDailyWordGoal());
        int weeklyGoal = dailyGoal * 7;
        int todayWords = statByDate.containsKey(today) ? statByDate.get(today).getWordsWritten() : 0;
        int dailyGoalPct = Math.min(100, (int) Math.round((todayWords * 100.0) / dailyGoal));
        int weeklyGoalPct = Math.min(100, (int) Math.round((weeklyWordsSum * 100.0) / weeklyGoal));
        int publishedPct = totalPoems > 0 ? (int) Math.round((publishedCount * 100.0) / totalPoems) : 0;

        data.put("todayWords", todayWords);
        data.put("dailyWordGoal", dailyGoal);
        data.put("dailyGoalPct", dailyGoalPct);
        data.put("weeklyWordGoal", weeklyGoal);
        data.put("weeklyGoalPct", weeklyGoalPct);
        data.put("publishedPct", publishedPct);

        // Monthly Writing Activity (Last 6 Months) & Most Productive Day of Week
        List<WritingStatistic> allStats = statisticRepository.findByUserOrderByActivityDateAsc(user);
        Map<DayOfWeek, Integer> dayOfWeekWords = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dow : DayOfWeek.values()) {
            dayOfWeekWords.put(dow, 0);
        }
        for (WritingStatistic ws : allStats) {
            if (ws.getActivityDate() != null) {
                DayOfWeek dow = ws.getActivityDate().getDayOfWeek();
                dayOfWeekWords.put(dow, dayOfWeekWords.getOrDefault(dow, 0) + ws.getWordsWritten());
            }
        }
        // Also incorporate poem creation dates if stats are sparse
        for (Poem p : activePoems) {
            if (p.getCreatedAt() != null) {
                DayOfWeek dow = p.getCreatedAt().getDayOfWeek();
                dayOfWeekWords.put(dow, dayOfWeekWords.getOrDefault(dow, 0) + p.getWordCount());
            }
        }

        DayOfWeek bestDay = dayOfWeekWords.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.SUNDAY);
        data.put("mostProductiveDay", bestDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        List<String> weekdayLabels = new ArrayList<>();
        List<Integer> weekdayValues = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            weekdayLabels.add(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            weekdayValues.add(dayOfWeekWords.getOrDefault(dow, 0));
        }
        data.put("weekdayLabels", weekdayLabels);
        data.put("weekdayValues", weekdayValues);

        // Last 6 Months aggregation
        List<String> monthlyLabels = new ArrayList<>();
        List<Integer> monthlyWords = new ArrayList<>();
        List<Integer> monthlyPoems = new ArrayList<>();
        YearMonth currentYm = YearMonth.now();
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            monthlyLabels.add(ym.format(ymFmt));
            int mWords = 0;
            int mPoems = 0;
            for (WritingStatistic ws : allStats) {
                if (ws.getActivityDate() != null && YearMonth.from(ws.getActivityDate()).equals(ym)) {
                    mWords += ws.getWordsWritten();
                    mPoems += ws.getPoemsCreated() + ws.getPoemsEdited();
                }
            }
            for (Poem p : activePoems) {
                if (p.getCreatedAt() != null && YearMonth.from(p.getCreatedAt()).equals(ym)) {
                    mWords = Math.max(mWords, p.getWordCount());
                    mPoems = Math.max(mPoems, 1);
                }
            }
            monthlyWords.add(mWords);
            monthlyPoems.add(mPoems);
        }
        data.put("monthlyLabels", monthlyLabels);
        data.put("monthlyWords", monthlyWords);
        data.put("monthlyPoems", monthlyPoems);

        return data;
    }
}
