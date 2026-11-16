package com.debatearena.web;

import com.debatearena.analysis.ParticipantRanking;
import com.debatearena.service.AnalyticsService;
import com.debatearena.web.dto.Responses.ClaimRankingDto;
import com.debatearena.web.dto.Responses.ClaimStrengthDto;
import com.debatearena.web.dto.Responses.ParticipantStatsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API for statistics and rankings. */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/claims/{id}/strength")
    public ClaimStrengthDto claimStrength(@PathVariable Long id) {
        return analytics.claimStrength(id);
    }

    @GetMapping("/claims/{id}/rankings")
    public ClaimRankingDto claimRankings(@PathVariable Long id) {
        return analytics.claimRankings(id);
    }

    @GetMapping("/topics/{id}/claims")
    public List<ClaimStrengthDto> topicClaims(@PathVariable Long id) {
        return analytics.topicClaimStrengths(id);
    }

    @GetMapping("/participants/{id}")
    public ParticipantStatsDto participant(@PathVariable Long id) {
        return analytics.participantStats(id);
    }

    @GetMapping("/leaderboard")
    public List<ParticipantRanking> leaderboard() {
        return analytics.leaderboard();
    }
}
