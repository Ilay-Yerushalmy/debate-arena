package com.debatearena.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full stack: HTTP -> controller -> service -> JPA -> SQLite (fresh temp file, seeded by data.sql). */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws IOException {
        Path file = Files.createTempFile("debate-arena-test", ".db");
        file.toFile().deleteOnExit();
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + file.toAbsolutePath() + "?foreign_keys=true");
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void seededTopicsAreReturned() throws Exception {
        mvc.perform(get("/api/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].claims", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void newArgumentIsAssessedAndStored() throws Exception {
        String body = """
                {"sessionId": 1, "claimId": 1, "participantId": 3, "stance": "PRO",
                 "content": "According to a 2023 survey, 62% of developers prefer remote work, for example at Shopify."}
                """;
        mvc.perform(post("/api/arguments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasExample").value(true))
                .andExpect(jsonPath("$.hasSource").value(true))
                .andExpect(jsonPath("$.qualityScore").value(100));
    }

    @Test
    void claimFromAnotherTopicIsRejected() throws Exception {
        String body = """
                {"sessionId": 1, "claimId": 3, "participantId": 1, "stance": "PRO",
                 "content": "This claim belongs to the AI topic, not remote work."}
                """;
        mvc.perform(post("/api/arguments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void participantsCannotRateThemselves() throws Exception {
        // Argument 1 was written by participant 1 (seed data).
        mvc.perform(post("/api/arguments/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"raterId\": 1, \"score\": 5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Participants cannot rate their own arguments"));
    }

    @Test
    void invalidRatingFailsValidation() throws Exception {
        mvc.perform(post("/api/arguments/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"raterId\": 2, \"score\": 9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownClaimReturns404() throws Exception {
        mvc.perform(get("/api/analytics/claims/9999/strength"))
                .andExpect(status().isNotFound());
    }

    @Test
    void claimStrengthIsComputedFromSeedData() throws Exception {
        mvc.perform(get("/api/analytics/claims/1/strength"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.sessions", hasSize(2)))
                .andExpect(jsonPath("$.analysis.verdict").exists());
    }

    @Test
    void leaderboardRanksAllParticipants() throws Exception {
        mvc.perform(get("/api/analytics/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1));
    }
}
