package com.poetryverse.service;

import com.poetryverse.entity.Favorite;
import com.poetryverse.entity.Poem;
import com.poetryverse.entity.User;
import com.poetryverse.repository.FavoriteRepository;
import com.poetryverse.repository.PoemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PoemRepository poemRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, PoemRepository poemRepository) {
        this.favoriteRepository = favoriteRepository;
        this.poemRepository = poemRepository;
    }

    public List<Favorite> getUserFavorites(User user, String search, String sort) {
        List<Favorite> favorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        favorites.forEach(f -> {
            if (f.getPoem() != null) {
                f.getPoem().setFavorite(true);
            }
        });

        List<Favorite> filtered = favorites.stream()
                .filter(f -> f.getPoem() != null && !f.getPoem().isArchived())
                .filter(f -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    Poem p = f.getPoem();
                    boolean matchTitle = p.getTitle() != null && p.getTitle().toLowerCase().contains(q);
                    boolean matchContent = p.getContent() != null && p.getContent().toLowerCase().contains(q);
                    boolean matchGenre = p.getGenre() != null && p.getGenre().toLowerCase().contains(q);
                    boolean matchNote = f.getPersonalNote() != null && f.getPersonalNote().toLowerCase().contains(q);
                    return matchTitle || matchContent || matchGenre || matchNote;
                })
                .collect(Collectors.toList());

        String sortMode = (sort == null || sort.isBlank()) ? "recent_favorited" : sort.toLowerCase();
        switch (sortMode) {
            case "title":
                filtered.sort(Comparator.comparing(f -> f.getPoem().getTitle().toLowerCase()));
                break;
            case "oldest":
                filtered.sort(Comparator.comparing(Favorite::getCreatedAt));
                break;
            case "words":
                filtered.sort(Comparator.comparingInt((Favorite f) -> f.getPoem().getWordCount()).reversed());
                break;
            case "recent_favorited":
            default:
                filtered.sort(Comparator.comparing(Favorite::getCreatedAt).reversed());
                break;
        }
        return filtered;
    }

    @Transactional
    public boolean toggleFavorite(User user, Long poemId) {
        Poem poem = poemRepository.findByIdAndUser(poemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Poem not found in your library."));

        Optional<Favorite> existing = favoriteRepository.findByUserAndPoem(user, poem);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        } else {
            Favorite fav = new Favorite(user, poem);
            favoriteRepository.save(fav);
            return true;
        }
    }

    @Transactional
    public Favorite updateFavoriteNote(User user, Long poemId, String note) {
        Poem poem = poemRepository.findByIdAndUser(poemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Poem not found."));
        Favorite fav = favoriteRepository.findByUserAndPoem(user, poem)
                .orElseGet(() -> new Favorite(user, poem));
        fav.setPersonalNote(note != null ? note.trim() : null);
        return favoriteRepository.save(fav);
    }

    public long countFavorites(User user) {
        return favoriteRepository.countByUser(user);
    }
}
