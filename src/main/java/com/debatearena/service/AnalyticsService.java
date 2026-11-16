package com.debatearena.service;

import com.debatearena.analysis.ParticipantRanking;
import com.debatearena.analysis.ScoreCalculator;
import com.debatearena.analysis.ScoredArgument;
import com.debatearena.domain.Argument;
import com.debatearena.domain.Claim;
import com.debatearena.domain.Participant;
import com.debatearena.domain.Stance;
import com.debatearena.exception.NotFoundException;
import com.debatearena.repository.ArgumentRepository;
import com.debatearena.repository.ClaimRepository;
import com.debatearena.repository.RatingRepository;
import com.debatearena.repository.ScoredArgumentQueries;
import com.debatearena.web.dto.Responses.ClaimRankingDto;
import com.debatearena.web.dto.Responses.ClaimStrengthDto;
import com.debatearena.web.dto.Responses.ParticipantStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Statistics: claim strength across sessions and participant rankings per claim. */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ScoredArgumentQueries scoredQueries;
    private final ArgumentRepository arguments;
    private final ClaimRepository claims;
    private final RatingRepository ratings;
    private final DebateService debateService;

    public AnalyticsService(ScoredArgumentQueries scoredQueries, ArgumentRepository arguments, ClaimRepository claims,
                            RatingRepository ratings, DebateService debateService) {
        this.scoredQueries = scoredQueries;
        this.arguments = arguments;
        this.claims = claims;
        this.ratings = ratings;
        this.debateService = debateService;
    }

    /** Strength of a claim, overall and per debate session. */
    public ClaimStrengthDto claimStrength(Long claimId) {
        final Claim claim = findClaim(claimId);
        final List<ScoredArgument> scored = scoredQueries.byClaim(claimId);
        return new ClaimStrengthDto(claim.getId(), claim.getText(), ScoreCalculator.claimStrength(scored));
    }

    /** All claims of a topic, strongest first. */
    public List<ClaimStrengthDto> topicClaimStrengths(Long topicId) {
        debateService.findTopic(topicId);
        return claims.findByTopicIdOrderByIdAsc(topicId).stream()
                .map(c -> new ClaimStrengthDto(c.getId(), c.getText(),
                        ScoreCalculator.claimStrength(scoredQueries.byClaim(c.getId()))))
                .sorted(Comparator.comparingDouble((ClaimStrengthDto d) -> d.analysis().strength()).reversed())
                .toList();
    }

    /** Participant ranking for one specific claim. */
    public ClaimRankingDto claimRankings(Long claimId) {
        final Claim claim = findClaim(claimId);
        return new ClaimRankingDto(claim.getId(), claim.getText(),
                ScoreCalculator.rankParticipants(scoredQueries.byClaim(claimId)));
    }

    /** Overall leaderboard across every debate. */
    public List<ParticipantRanking> leaderboard() {
        return ScoreCalculator.rankParticipants(scoredQueries.all());
    }

    public ParticipantStatsDto participantStats(Long participantId) {
        final Participant p = debateService.findParticipant(participantId);
        final List<ScoredArgument> scored = scoredQueries.byParticipant(participantId);
        final List<ParticipantRanking> ranking = ScoreCalculator.rankParticipants(scored);

        final int pro = (int) scored.stream().filter(a -> a.stance() == Stance.PRO).count();
        final double avgQuality = scored.stream().mapToInt(ScoredArgument::qualityScore).average().orElse(0);
        final double avgCombined = scored.stream().mapToDouble(ScoredArgument::combinedScore).average().orElse(0);
        final Double avgRating = ranking.isEmpty() ? null : ranking.get(0).avgRating();

        final List<Argument> entities = arguments.findAllById(scored.stream().map(ScoredArgument::argumentId).toList());
        final int examples = (int) entities.stream().filter(Argument::hasExample).count();
        final int sources = (int) entities.stream().filter(Argument::hasSource).count();

        return new ParticipantStatsDto(p.getId(), p.getName(), scored.size(), pro, scored.size() - pro,
                round(avgQuality), avgRating, ratings.countByRaterId(participantId), round(avgCombined),
                examples, sources);
    }

    private Claim findClaim(Long id) {
        return claims.findById(id).orElseThrow(() -> new NotFoundException("Claim", id));
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
