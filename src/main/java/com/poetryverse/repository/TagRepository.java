package com.poetryverse.repository;

import com.poetryverse.entity.Tag;
import com.poetryverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserOrderByNameAsc(User user);
    Optional<Tag> findByUserAndNameIgnoreCase(User user, String name);
}
