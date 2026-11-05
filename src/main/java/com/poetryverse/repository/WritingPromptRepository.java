package com.poetryverse.repository;

import com.poetryverse.entity.WritingPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WritingPromptRepository extends JpaRepository<WritingPrompt, Long> {
    List<WritingPrompt> findByCategoryOrderByIdAsc(String category);
    List<WritingPrompt> findByDailyFeaturedTrue();
}
