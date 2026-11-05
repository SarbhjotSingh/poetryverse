package com.poetryverse.repository;

import com.poetryverse.entity.Favorite;
import com.poetryverse.entity.Poem;
import com.poetryverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favorite> findByUserAndPoem(User user, Poem poem);
    boolean existsByUserAndPoem(User user, Poem poem);
    void deleteByUserAndPoem(User user, Poem poem);
    long countByUser(User user);
}
