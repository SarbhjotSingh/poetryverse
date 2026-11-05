package com.poetryverse.service;

import com.poetryverse.entity.JournalEntry;
import com.poetryverse.entity.User;
import com.poetryverse.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalService {

    private final JournalEntryRepository journalRepository;
    private final UserService userService;

    public JournalService(JournalEntryRepository journalRepository, UserService userService) {
        this.journalRepository = journalRepository;
        this.userService = userService;
    }

    public List<JournalEntry> getJournalEntries(User user, String search, String mood, LocalDate dateFilter) {
        List<JournalEntry> entries = journalRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user);
        return entries.stream()
                .filter(e -> {
                    if (dateFilter != null && !dateFilter.equals(e.getEntryDate())) {
                        return false;
                    }
                    if (mood != null && !mood.isBlank() && !"ALL".equalsIgnoreCase(mood)) {
                        if (e.getMood() == null || !e.getMood().equalsIgnoreCase(mood.trim())) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        boolean mTitle = e.getTitle() != null && e.getTitle().toLowerCase().contains(q);
                        boolean mContent = e.getContent() != null && e.getContent().toLowerCase().contains(q);
                        boolean mSpark = e.getInspirationSpark() != null && e.getInspirationSpark().toLowerCase().contains(q);
                        return mTitle || mContent || mSpark;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public JournalEntry getEntryForUser(User user, Long id) {
        return journalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found."));
    }

    @Transactional
    public JournalEntry saveOrUpdateEntry(User user, Long id, LocalDate entryDate, String title,
                                          String content, String mood, String weather,
                                          String inspirationSpark, String locationNote, Boolean pinned) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Journal entry title is required.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Journal entry reflection is required.");
        }

        JournalEntry entry = (id == null)
                ? new JournalEntry()
                : journalRepository.findByIdAndUser(id, user).orElseGet(JournalEntry::new);

        entry.setUser(user);
        entry.setEntryDate(entryDate != null ? entryDate : LocalDate.now());
        entry.setTitle(title.trim());
        entry.setContent(content.trim());
        entry.setMood((mood != null && !mood.isBlank()) ? mood.trim() : "Serene");
        entry.setWeather((weather != null && !weather.isBlank()) ? weather.trim() : "Mist & Morning Dew");
        entry.setInspirationSpark(inspirationSpark != null ? inspirationSpark.trim() : null);
        entry.setLocationNote((locationNote != null && !locationNote.isBlank()) ? locationNote.trim() : "Writing Desk");
        if (pinned != null) entry.setPinned(pinned);

        entry.recalculateWords();
        userService.recordWritingStreak(user);
        return journalRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(User user, Long id) {
        JournalEntry entry = getEntryForUser(user, id);
        journalRepository.delete(entry);
    }
}
