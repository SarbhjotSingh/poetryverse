package com.poetryverse;

import com.poetryverse.entity.Poem;
import com.poetryverse.entity.PoemCollection;
import com.poetryverse.entity.User;
import com.poetryverse.repository.CollectionRepository;
import com.poetryverse.repository.PoemRepository;
import com.poetryverse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PoetryVerseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PoemRepository poemRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Test
    void contextLoadsAndSeedsDefaultData() {
        User clara = userRepository.findByUsernameIgnoreCase("clara_vance").orElse(null);
        assertThat(clara).isNotNull();
        assertThat(poemRepository.countByUserAndArchivedFalse(clara)).isGreaterThanOrEqualTo(6);
        assertThat(collectionRepository.countByUser(clara)).isGreaterThanOrEqualTo(7);
    }

    @Test
    void publicPagesRenderSuccessfully() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/about")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("clara_vance")
    void allProtectedWorkspacePagesAndExportsRenderSuccessfully() throws Exception {
        String[] routes = {
                "/dashboard",
                "/library",
                "/editor",
                "/collections",
                "/favorites",
                "/journal",
                "/templates",
                "/discover",
                "/statistics",
                "/profile",
                "/settings",
                "/about"
        };
        for (String route : routes) {
            mockMvc.perform(get(route)).andExpect(status().isOk());
        }

        // Verify REST API endpoints
        mockMvc.perform(get("/api/discover/inspire")).andExpect(status().isOk());
        mockMvc.perform(get("/api/stats/summary")).andExpect(status().isOk());
        mockMvc.perform(get("/api/backup/download")).andExpect(status().isOk());

        User clara = userRepository.findByUsernameIgnoreCase("clara_vance").orElseThrow();
        List<Poem> poems = poemRepository.findByUserOrderByUpdatedAtDesc(clara);
        Long poemId = poems.get(0).getId();

        mockMvc.perform(get("/api/poems/" + poemId + "/export/pdf")).andExpect(status().isOk());
        mockMvc.perform(get("/api/poems/" + poemId + "/export/docx")).andExpect(status().isOk());
        mockMvc.perform(get("/api/poems/" + poemId + "/export/txt")).andExpect(status().isOk());

        List<PoemCollection> cols = collectionRepository.findByUserOrderByCreatedAtAsc(clara);
        mockMvc.perform(get("/api/collections/" + cols.get(0).getId() + "/export/pdf")).andExpect(status().isOk());

        // Test saving a new poem via REST API
        String jsonPayload = """
                {
                  "title": "Test Nocturne in Olive",
                  "subtitle": "Automated Verification Verse",
                  "content": "Soft rain upon the olive bough,\\nThe lantern glows in ivory light.",
                  "genre": "Free Verse",
                  "mood": "Serene",
                  "status": "PUBLISHED",
                  "tagsCsv": "olive, rain, test"
                }
                """;
        mockMvc.perform(post("/api/poems/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }
}
