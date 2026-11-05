package com.poetryverse.repository;

import com.poetryverse.entity.JournalEntry;
import com.poetryverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserOrderByEntryDateDescCreatedAtDesc(User user);
    List<JournalEntry> findByUserAndEntryDateOrderByCreatedAtDesc(User user, LocalDate entryDate);
    Optional<JournalEntry> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}
