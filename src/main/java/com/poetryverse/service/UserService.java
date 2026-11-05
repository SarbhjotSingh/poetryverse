package com.poetryverse.service;

import com.poetryverse.entity.PoemCollection;
import com.poetryverse.entity.User;
import com.poetryverse.entity.UserSettings;
import com.poetryverse.repository.CollectionRepository;
import com.poetryverse.repository.UserRepository;
import com.poetryverse.repository.UserSettingsRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final CollectionRepository collectionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserSettingsRepository settingsRepository,
                       CollectionRepository collectionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.collectionRepository = collectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String username = auth.getName();
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username);
    }

    public User requireAuthenticatedUser() {
        return getAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("Authenticated poet session is required"));
    }

    @Transactional
    public User registerNewPoet(String fullName, String username, String email, String rawPassword, String penName) {
        String cleanUsername = username.trim().toLowerCase();
        String cleanEmail = email.trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new IllegalArgumentException("Username '" + cleanUsername + "' is already claimed by another poet.");
        }
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("An account with email '" + cleanEmail + "' already exists.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPenName((penName != null && !penName.isBlank()) ? penName.trim() : fullName.trim());
        user.setBio("Poet & curator of quiet verses on PoetryVerse.");
        user.setAvatarUrl("/images/avatar-poet.svg");
        user.setLocation("The Writing Sanctuary");
        user.setFavoritePoet("Emily Dickinson & Constantine P. Cavafy");
        user.setSignatureQuote("Where words find their forever.");
        user.setCurrentStreak(1);
        user.setLongestStreak(1);
        user.setLastWritingDate(LocalDate.now());

        User saved = userRepository.save(user);

        // Initialize 1-to-1 UserSettings
        UserSettings settings = new UserSettings();
        settings.setUser(saved);
        settingsRepository.save(settings);

        // Initialize default curated collections for the new poet's private library
        createDefaultCollectionsForUser(saved);

        return saved;
    }

    @Transactional
    public void createDefaultCollectionsForUser(User user) {
        if (collectionRepository.countByUser(user) > 0) {
            return;
        }
        String[][] defaults = {
                {"Love & Romance", "Letters Sealed in Amber Wax", "Intimate lyrics of devotion, whispered aubades, and enduring affection.", "#353626", "ORNATE_GOLD", "To love is to press a wildflower into the book of time."},
                {"Heartbreak", "Elegies for Unsent Letters", "Tender reckonings with absence, silence, and the porcelain fracture of parting.", "#4A3E36", "MINIMAL_HAIRLINE", "The wound is the place where the Light enters you."},
                {"Nature & Seasons", "Herbarium of the Changing Year", "Botanical meditations on pine ridgelines, autumn rain, olive orchards, and coastal tides.", "#60604D", "BOTANICAL_VINE", "Every leaf speaks bliss to me, fluttering from the autumn tree."},
                {"Life & Philosophy", "Voyages Toward Ithaca", "Stoic and lyrical reflections on time, mortality, purpose, and the long odyssey of the soul.", "#25251E", "CLASSICAL_ARCH", "Keep Ithaca always in your mind. Arriving there is what you are destined for."},
                {"Dreams & Memories", "Nocturnes in Warm Ivory", "Half-remembered corridors, childhood gardens, and lantern-lit reveries written after midnight.", "#A18B50", "OVAL_ANTIQUE", "We are such stuff as dreams are made on."},
                {"Spirituality", "Sanctuary of Stillness", "Sacred hymns of quietude, candlelit vigils, and the unseen grace binding all living things.", "#353626", "ORNATE_GOLD", "Be still, and listen to the music behind the breath."},
                {"Untitled Thoughts", "Fragments & Marginalia", "Raw ink sketches, unpolished couplets, and fleeting verses captured in the margins of the day.", "#60604D", "BOTANICAL_VINE", "Even a single line can hold an entire season."}
        };

        for (int i = 0; i < defaults.length; i++) {
            String[] d = defaults[i];
            PoemCollection col = new PoemCollection();
            col.setUser(user);
            col.setName(d[0]);
            col.setSubtitle(d[1]);
            col.setDescription(d[2]);
            col.setAccentColor(d[3]);
            col.setFrameMotif(d[4]);
            col.setEpigraph(d[5]);
            col.setFeatured(i < 3);
            collectionRepository.save(col);
        }
    }

    @Transactional
    public UserSettings getOrCreateSettings(User user) {
        return settingsRepository.findByUser(user).orElseGet(() -> {
            UserSettings s = new UserSettings();
            s.setUser(user);
            return settingsRepository.save(s);
        });
    }

    @Transactional
    public User updateProfile(User user, String fullName, String penName, String username,
                              String email, String bio, String location, String website,
                              String favoritePoet, String signatureQuote, String avatarUrl) {
        if (username != null && !username.isBlank() && !username.equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
                throw new IllegalArgumentException("Username '" + username + "' is already taken.");
            }
            user.setUsername(username.trim().toLowerCase());
        }
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("Email '" + email + "' is already registered.");
            }
            user.setEmail(email.trim().toLowerCase());
        }
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (penName != null) user.setPenName(penName.trim());
        if (bio != null) user.setBio(bio.trim());
        if (location != null) user.setLocation(location.trim());
        if (website != null) user.setWebsite(website.trim());
        if (favoritePoet != null) user.setFavoritePoet(favoritePoet.trim());
        if (signatureQuote != null) user.setSignatureQuote(signatureQuote.trim());
        if (avatarUrl != null && !avatarUrl.isBlank()) user.setAvatarUrl(avatarUrl.trim());

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match our records.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPasswordByIdentifier(String usernameOrEmail, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(usernameOrEmail.trim(), usernameOrEmail.trim())
                .orElseThrow(() -> new IllegalArgumentException("No account found matching '" + usernameOrEmail + "'."));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserSettings updateSettings(User user, UserSettings incoming) {
        UserSettings s = getOrCreateSettings(user);
        if (incoming.getThemeMode() != null) s.setThemeMode(incoming.getThemeMode());
        if (incoming.getEditorFontFamily() != null) s.setEditorFontFamily(incoming.getEditorFontFamily());
        if (incoming.getEditorFontSize() > 10) s.setEditorFontSize(incoming.getEditorFontSize());
        if (incoming.getEditorLineSpacing() >= 1.0) s.setEditorLineSpacing(incoming.getEditorLineSpacing());
        if (incoming.getPaperTexture() != null) s.setPaperTexture(incoming.getPaperTexture());
        if (incoming.getDefaultGenre() != null) s.setDefaultGenre(incoming.getDefaultGenre());
        if (incoming.getDefaultAlignment() != null) s.setDefaultAlignment(incoming.getDefaultAlignment());
        s.setAutoSaveEnabled(incoming.isAutoSaveEnabled());
        if (incoming.getAutoSaveIntervalSec() >= 5) s.setAutoSaveIntervalSec(incoming.getAutoSaveIntervalSec());
        s.setAmbientBotanicals(incoming.isAmbientBotanicals());
        s.setShowLiveStats(incoming.isShowLiveStats());
        if (incoming.getDailyWordGoal() >= 10) s.setDailyWordGoal(incoming.getDailyWordGoal());
        return settingsRepository.save(s);
    }

    @Transactional
    public void recordWritingStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastWritingDate();
        if (last == null) {
            user.setCurrentStreak(1);
        } else if (last.isEqual(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else if (last.isBefore(today.minusDays(1))) {
            user.setCurrentStreak(1);
        }
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }
        user.setLastWritingDate(today);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }
}
