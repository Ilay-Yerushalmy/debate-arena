package com.debatearena.analysis;

import com.debatearena.domain.Stance;

/**
 * Flat, read-only view of an argument together with its peer-rating aggregate.
 * Produced by a SQL query, consumed by {@link ScoreCalculator}.
 *
 * @param avgRating average 1-5 peer rating, or {@code null} if nobody rated it yet
 */
public record ScoredArgument(
        long argumentId,
        long sessionId,
        String sessionTitle,
        long claimId,
        long participantId,
        String participantName,
        Stance stance,
        int qualityScore,
        Double avgRating,
        int ratingCount) {

    public double combinedScore() {
        return ScoreCalculator.combinedScore(qualityScore, avgRating);
    }
}
