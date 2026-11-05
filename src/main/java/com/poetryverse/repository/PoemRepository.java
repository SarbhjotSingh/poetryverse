package com.poetryverse.repository;

import com.poetryverse.entity.Poem;
import com.poetryverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoemRepository extends JpaRepository<Poem, Long> {

    List<Poem> findByUserAndArchivedFalseOrderByUpdatedAtDesc(User user);

    List<Poem> findByUserAndArchivedTrueOrderByUpdatedAtDesc(User user);

    List<Poem> findByUserOrderByUpdatedAtDesc(User user);

    List<Poem> findTop6ByUserAndArchivedFalseOrderByUpdatedAtDesc(User user);

    Optional<Poem> findByIdAndUser(Long id, User user);

    long countByUserAndArchivedFalse(User user);

    @Query("SELECT COALESCE(SUM(p.wordCount), 0) FROM Poem p WHERE p.user = :user AND p.archived = false")
    long sumWordCountByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(p.characterCount), 0) FROM Poem p WHERE p.user = :user AND p.archived = false")
    long sumCharacterCountByUser(@Param("user") User user);
}
