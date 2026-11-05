package com.poetryverse.repository;

import com.poetryverse.entity.PoemCollection;
import com.poetryverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<PoemCollection, Long> {
    List<PoemCollection> findByUserOrderByCreatedAtAsc(User user);
    Optional<PoemCollection> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}
