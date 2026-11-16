package com.debatearena.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Rule-based technical assessment of argument quality.
 *
 * <p>Checks that claims are supported by examples and that statistics come with a source.
 * Scoring (clamped to 0-100):
 * <pre>
 *   base                              40
 *   concrete example                 +20
 *   cites a source                   +20
 *   statistic WITH a source          +15
 *   statistic WITHOUT a source       -15   (unsupported numbers are penalised)
 *   at least 15 words                 +5   otherwise -10
 * </pre>
 */
@Component
public class ArgumentQualityAssessor {

    static final int BASE_SCORE = 40;
    static final int EXAMPLE_BONUS = 20;
    static final int SOURCE_BONUS = 20;
    static final int SOURCED_STATISTIC_BONUS = 15;
    static final int UNSOURCED_STATISTIC_PENALTY = 15;
    static final int MIN_WORDS = 15;

    private static final Pattern EXAMPLE = Pattern.compile(
            "\\b(for example|for instance|e\\.g\\.|such as|consider the case|a case in point)", Pattern.CASE_INSENSITIVE);

    private static final Pattern STATISTIC = Pattern.compile(
            "\\d+(\\.\\d+)?\\s?%"
                    + "|\\b\\d+(\\.\\d+)?\\s?(percent|million|billion|thousand)\\b"
                    + "|\\b\\d+\\s+(out of|in)\\s+\\d+\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SOURCE = Pattern.compile(
            "https?://\\S+"
                    + "|\\b(according to|study|studies|survey|report|research|published|journal|source:)"
                    + "|\\[\\d+\\]",
            Pattern.CASE_INSENSITIVE);

    public QualityReport assess(String text) {
        final String content = text == null ? "" : text.trim();
        final boolean hasExample = EXAMPLE.matcher(content).find();
        final boolean hasStatistic = STATISTIC.matcher(content).find();
        final boolean hasSource = SOURCE.matcher(content).find();
        final int words = content.isEmpty() ? 0 : content.split("\\s+").length;

        int score = BASE_SCORE;
        final List<String> feedback = new ArrayList<>();

        if (hasExample) {
            score += EXAMPLE_BONUS;
        } else {
            feedback.add("Add a concrete example that supports the claim.");
        }

        if (hasSource) {
            score += SOURCE_BONUS;
        }

        if (hasStatistic && hasSource) {
            score += SOURCED_STATISTIC_BONUS;
        } else if (hasStatistic) {
            score -= UNSOURCED_STATISTIC_PENALTY;
            feedback.add("The statistic has no source - cite where the number comes from.");
        } else if (!hasSource) {
            feedback.add("Consider citing a source or data to strengthen the argument.");
        }

        if (words >= MIN_WORDS) {
            score += 5;
        } else {
            score -= 10;
            feedback.add("The argument is very short - explain the reasoning in more detail.");
        }

        score = Math.max(0, Math.min(100, score));
        return new QualityReport(score, hasExample, hasStatistic, hasSource, words, List.copyOf(feedback));
    }
}
