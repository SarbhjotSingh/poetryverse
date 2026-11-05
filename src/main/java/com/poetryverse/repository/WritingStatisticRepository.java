package com.poetryverse.repository;

import com.poetryverse.entity.User;
import com.poetryverse.entity.WritingStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WritingStatisticRepository extends JpaRepository<WritingStatistic, Long> {
    Optional<WritingStatistic> findByUserAndActivityDate(User user, LocalDate activityDate);
    List<WritingStatistic> findByUserOrderByActivityDateAsc(User user);
    List<WritingStatistic> findByUserAndActivityDateBetweenOrderByActivityDateAsc(User user, LocalDate start, LocalDate end);
}
