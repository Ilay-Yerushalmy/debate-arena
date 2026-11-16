package com.debatearena.analysis;

import com.debatearena.domain.Stance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pure statistics functions (no Spring, no database) - easy to unit test.
 *
 * <p><b>Combined argument score</b> (0-100) = 70% peer rating + 30% technical quality.
 * An argument nobody rated yet gets a neutral rating (3/5) so it is neither rewarded nor punished.
 *
 * <p><b>Claim strength</b> (0-100) = share of combined score on the PRO side:
 * {@code 100 * sum(PRO scores) / (sum(PRO scores) + sum(CON scores))}.
 */
public final class ScoreCalculator {

    static final double RATING_WEIGHT = 0.7;
    static final double QUALITY_WEIGHT = 0.3;
    static final double NEUTRAL_RATING = 3.0;

    private ScoreCalculator() {
    }

    /** Converts a 1-5 rating to 0-100 and blends it with the 0-100 quality score. */
    public static double combinedScore(int qualityScore, Double avgRating) {
        final double rating = avgRating == null ? NEUTRAL_RATING : avgRating;
        final double ratingPercent = (rating - 1.0) / 4.0 * 100.0;
        return RATING_WEIGHT * ratingPercent + QUALITY_WEIGHT * qualityScore;
    }

    public static ClaimStrength claimStrength(List<ScoredArgument> arguments) {
        if (arguments.isEmpty()) {
            return new ClaimStrength(50.0, "NOT_DEBATED", 0, 0, List.of());
        }

        final int pro = (int) arguments.stream().filter(a -> a.stance() == Stance.PRO).count();
        final int con = arguments.size() - pro;

        // Group by session, keeping chronological order of first appearance.
        final Map<Long, List<ScoredArgument>> bySession = arguments.stream()
                .collect(Collectors.groupingBy(ScoredArgument::sessionId, LinkedHashMap::new, Collectors.toList()));

        final List<ClaimStrength.SessionBreakdown> sessions = new ArrayList<>();
        bySession.forEach((sessionId, list) -> sessions.add(new ClaimStrength.SessionBreakdown(
                sessionId, list.get(0).sessionTitle(), round(strengthOf(list)), list.size())));

        final double strength = round(strengthOf(arguments));
        return new ClaimStrength(strength, verdictFor(strength), pro, con, sessions);
    }

    public static List<ParticipantRanking> rankParticipants(List<ScoredArgument> arguments) {
        final Map<Long, List<ScoredArgument>> byParticipant = arguments.stream()
                .collect(Collectors.groupingBy(ScoredArgument::participantId, LinkedHashMap::new, Collectors.toList()));

        record Row(long id, String name, int count, Double avgRating, double avgQuality, double score) {
        }

        final List<Row> rows = new ArrayList<>();
        byParticipant.forEach((id, list) -> {
            final double score = list.stream().mapToDouble(ScoredArgument::combinedScore).average().orElse(0);
            final double quality = list.stream().mapToInt(ScoredArgument::qualityScore).average().orElse(0);
            rows.add(new Row(id, list.get(0).participantName(), list.size(), weightedAvgRating(list), quality, score));
        });
        rows.sort(Comparator.comparingDouble(Row::score).reversed().thenComparing(Row::name));

        final List<ParticipantRanking> ranking = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            final Row r = rows.get(i);
            // Competition ranking: equal (rounded) scores share the same rank.
            final int rank = (i > 0 && round(rows.get(i - 1).score()) == round(r.score()))
                    ? ranking.get(i - 1).rank()
                    : i + 1;
            ranking.add(new ParticipantRanking(rank, r.id(), r.name(), r.count(),
                    r.avgRating() == null ? null : round(r.avgRating()), round(r.avgQuality()), round(r.score())));
        }
        return ranking;
    }

    static String verdictFor(double strength) {
        if (strength >= 60.0) return "STRONG";
        if (strength <= 40.0) return "WEAK";
        return "CONTESTED";
    }

    private static double strengthOf(List<ScoredArgument> arguments) {
        double pro = 0;
        double con = 0;
        for (ScoredArgument a : arguments) {
            if (a.stance() == Stance.PRO) {
                pro += a.combinedScore();
            } else {
                con += a.combinedScore();
            }
        }
        return pro + con == 0 ? 50.0 : 100.0 * pro / (pro + con);
    }

    /** Average rating weighted by number of ratings per argument (each individual rating counts once). */
    private static Double weightedAvgRating(List<ScoredArgument> list) {
        double sum = 0;
        int count = 0;
        for (ScoredArgument a : list) {
            if (a.avgRating() != null && a.ratingCount() > 0) {
                sum += a.avgRating() * a.ratingCount();
                count += a.ratingCount();
            }
        }
        return count == 0 ? null : sum / count;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
