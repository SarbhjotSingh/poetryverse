package com.poetryverse.service;

import com.poetryverse.entity.Poem;
import com.poetryverse.entity.PoemCollection;
import com.poetryverse.entity.User;
import com.poetryverse.repository.CollectionRepository;
import com.poetryverse.repository.FavoriteRepository;
import com.poetryverse.repository.PoemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final PoemRepository poemRepository;
    private final FavoriteRepository favoriteRepository;

    public CollectionService(CollectionRepository collectionRepository,
                             PoemRepository poemRepository,
                             FavoriteRepository favoriteRepository) {
        this.collectionRepository = collectionRepository;
        this.poemRepository = poemRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public List<PoemCollection> getUserCollections(User user) {
        return collectionRepository.findByUserOrderByCreatedAtAsc(user);
    }

    public PoemCollection getCollectionForUser(User user, Long collectionId) {
        return collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found in your library."));
    }

    public List<Poem> getPoemsInCollection(User user, Long collectionId) {
        PoemCollection col = getCollectionForUser(user, collectionId);
        Set<Long> favIds = favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(f -> f.getPoem().getId())
                .collect(Collectors.toSet());

        List<Poem> list = new ArrayList<>(col.getPoems());
        list.forEach(p -> p.setFavorite(favIds.contains(p.getId())));
        list.sort(Comparator.comparing(Poem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Transactional
    public PoemCollection saveOrUpdateCollection(User user, Long id, String name, String subtitle,
                                                 String description, String coverImage,
                                                 String accentColor, String frameMotif,
                                                 String epigraph, Boolean featured,
                                                 List<Long> assignedPoemIds) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Collection name is required.");
        }
        PoemCollection col = (id == null)
                ? new PoemCollection()
                : collectionRepository.findByIdAndUser(id, user).orElseGet(PoemCollection::new);

        col.setUser(user);
        col.setName(name.trim());
        col.setSubtitle(subtitle != null ? subtitle.trim() : null);
        col.setDescription(description != null ? description.trim() : null);
        if (coverImage != null) col.setCoverImage(coverImage.trim());
        if (accentColor != null && !accentColor.isBlank()) col.setAccentColor(accentColor.trim());
        if (frameMotif != null && !frameMotif.isBlank()) col.setFrameMotif(frameMotif.trim());
        if (epigraph != null) col.setEpigraph(epigraph.trim());
        if (featured != null) col.setFeatured(featured);

        PoemCollection saved = collectionRepository.save(col);

        if (assignedPoemIds != null) {
            Set<Long> targetIds = new HashSet<>(assignedPoemIds);
            List<Poem> allUserPoems = poemRepository.findByUserOrderByUpdatedAtDesc(user);
            for (Poem p : allUserPoems) {
                boolean currentlyIn = p.getCollections().stream().anyMatch(c -> Objects.equals(c.getId(), saved.getId()));
                boolean shouldBeIn = targetIds.contains(p.getId());
                if (shouldBeIn && !currentlyIn) {
                    p.getCollections().add(saved);
                    poemRepository.save(p);
                } else if (!shouldBeIn && currentlyIn) {
                    p.getCollections().removeIf(c -> Objects.equals(c.getId(), saved.getId()));
                    poemRepository.save(p);
                }
            }
        }

        return collectionRepository.findById(saved.getId()).orElse(saved);
    }

    @Transactional
    public void assignPoemToCollection(User user, Long collectionId, Long poemId) {
        PoemCollection col = getCollectionForUser(user, collectionId);
        Poem poem = poemRepository.findByIdAndUser(poemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Poem not found."));
        poem.getCollections().add(col);
        poemRepository.save(poem);
    }

    @Transactional
    public void removePoemFromCollection(User user, Long collectionId, Long poemId) {
        Poem poem = poemRepository.findByIdAndUser(poemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Poem not found."));
        poem.getCollections().removeIf(c -> Objects.equals(c.getId(), collectionId));
        poemRepository.save(poem);
    }

    @Transactional
    public void deleteCollection(User user, Long collectionId) {
        PoemCollection col = getCollectionForUser(user, collectionId);
        for (Poem p : new ArrayList<>(col.getPoems())) {
            p.getCollections().removeIf(c -> Objects.equals(c.getId(), collectionId));
            poemRepository.save(p);
        }
        collectionRepository.delete(col);
    }
}
