package com.debatearena.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentQualityAssessorTest {

    private final ArgumentQualityAssessor assessor = new ArgumentQualityAssessor();

    @Test
    void fullySupportedArgumentGetsMaxScore() {
        QualityReport r = assessor.assess(
                "According to a Stanford study, remote employees were 13% more productive, "
                        + "for example they took fewer sick days and shorter breaks.");

        assertThat(r.hasExample()).isTrue();
        assertThat(r.hasStatistic()).isTrue();
        assertThat(r.hasSource()).isTrue();
        assertThat(r.score()).isEqualTo(100);
        assertThat(r.feedback()).isEmpty();
    }

    @Test
    void statisticWithoutSourceIsPenalised() {
        QualityReport r = assessor.assess(
                "Remote work saves around 40% of commute time which people reinvest in deep, focused work every day.");

        assertThat(r.hasStatistic()).isTrue();
        assertThat(r.hasSource()).isFalse();
        assertThat(r.score()).isEqualTo(30);
        assertThat(r.feedback()).anyMatch(f -> f.contains("no source"));
    }

    @Test
    void exampleRaisesScore() {
        String withExample = "Many people get distracted at home, for instance by family members and chores, so output drops.";
        String withoutExample = "Many people get distracted at home by family members and chores and so their output drops a lot.";

        assertThat(assessor.assess(withExample).score()).isGreaterThan(assessor.assess(withoutExample).score());
    }

    @Test
    void shortArgumentGetsFeedback() {
        QualityReport r = assessor.assess("AI is bad for students.");

        assertThat(r.wordCount()).isEqualTo(5);
        assertThat(r.feedback()).anyMatch(f -> f.contains("very short"));
    }

    @Test
    void urlCountsAsSource() {
        assertThat(assessor.assess("See https://example.org/report for details").hasSource()).isTrue();
    }

    @Test
    void scoreIsAlwaysWithinBounds() {
        assertThat(assessor.assess("").score()).isBetween(0, 100);
        assertThat(assessor.assess(null).score()).isBetween(0, 100);
    }
}
