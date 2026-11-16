package com.debatearena.analysis;

import com.debatearena.domain.Stance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ScoreCalculatorTest {

    private static ScoredArgument arg(long id, long session, long participant, String name, Stance stance,
                                      int quality, Double rating, int ratingCount) {
        return new ScoredArgument(id, session, "Session " + session, 1, participant, name, stance, quality, rating,
                ratingCount);
    }

    @Test
    void combinedScoreBlendsRatingAndQuality() {
        // rating 5/5 -> 100, quality 100 -> 100
        assertThat(ScoreCalculator.combinedScore(100, 5.0)).isCloseTo(100.0, within(0.001));
        // rating 1/5 -> 0, quality 0 -> 0
        assertThat(ScoreCalculator.combinedScore(0, 1.0)).isCloseTo(0.0, within(0.001));
        // unrated -> neutral 3/5 = 50 -> 0.7*50 + 0.3*60 = 53
        assertThat(ScoreCalculator.combinedScore(60, null)).isCloseTo(53.0, within(0.001));
    }

    @Test
    void claimWithOnlyProArgumentsIsStrong() {
        ClaimStrength s = ScoreCalculator.claimStrength(List.of(
                arg(1, 1, 1, "Ilay", Stance.PRO, 80, 4.0, 2)));

        assertThat(s.strength()).isEqualTo(100.0);
        assertThat(s.verdict()).isEqualTo("STRONG");
        assertThat(s.proCount()).isEqualTo(1);
    }

    @Test
    void balancedArgumentsAreContested() {
        ClaimStrength s = ScoreCalculator.claimStrength(List.of(
                arg(1, 1, 1, "Ilay", Stance.PRO, 60, 3.0, 1),
                arg(2, 1, 2, "Dana", Stance.CON, 60, 3.0, 1)));

        assertThat(s.strength()).isEqualTo(50.0);
        assertThat(s.verdict()).isEqualTo("CONTESTED");
    }

    @Test
    void strengthIsBrokenDownPerSession() {
        ClaimStrength s = ScoreCalculator.claimStrength(List.of(
                arg(1, 1, 1, "Ilay", Stance.PRO, 90, 5.0, 3),
                arg(2, 2, 2, "Dana", Stance.CON, 90, 5.0, 3),
                arg(3, 2, 3, "Noam", Stance.CON, 90, 5.0, 3)));

        assertThat(s.sessions()).hasSize(2);
        assertThat(s.sessions().get(0).strength()).isEqualTo(100.0);
        assertThat(s.sessions().get(1).strength()).isEqualTo(0.0);
        assertThat(s.verdict()).isEqualTo("WEAK");
    }

    @Test
    void noArgumentsMeansNotDebated() {
        assertThat(ScoreCalculator.claimStrength(List.of()).verdict()).isEqualTo("NOT_DEBATED");
    }

    @Test
    void participantsAreRankedByScoreWithTies() {
        List<ParticipantRanking> ranking = ScoreCalculator.rankParticipants(List.of(
                arg(1, 1, 1, "Ilay", Stance.PRO, 100, 5.0, 2),
                arg(2, 1, 2, "Dana", Stance.CON, 50, 3.0, 2),
                arg(3, 1, 3, "Noam", Stance.CON, 50, 3.0, 2)));

        assertThat(ranking).extracting(ParticipantRanking::participantName).containsExactly("Ilay", "Dana", "Noam");
        assertThat(ranking).extracting(ParticipantRanking::rank).containsExactly(1, 2, 2);
    }

    @Test
    void averageRatingIsWeightedByNumberOfRatings() {
        List<ParticipantRanking> ranking = ScoreCalculator.rankParticipants(List.of(
                arg(1, 1, 1, "Ilay", Stance.PRO, 50, 5.0, 3),
                arg(2, 1, 1, "Ilay", Stance.PRO, 50, 1.0, 1),
                arg(3, 1, 1, "Ilay", Stance.PRO, 50, null, 0)));

        // (5*3 + 1*1) / 4 = 4.0
        assertThat(ranking.get(0).avgRating()).isEqualTo(4.0);
        assertThat(ranking.get(0).argumentCount()).isEqualTo(3);
    }
}
