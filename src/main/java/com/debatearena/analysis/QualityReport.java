package com.debatearena.analysis;

import java.util.List;

/**
 * Result of the technical assessment of a single argument.
 *
 * @param score        0-100 quality score
 * @param hasExample   the argument backs the claim with a concrete example
 * @param hasStatistic the argument contains a number / percentage
 * @param hasSource    the argument cites a source (study, report, URL, ...)
 * @param wordCount    length of the argument
 * @param feedback     human-readable suggestions for improvement
 */
public record QualityReport(int score, boolean hasExample, boolean hasStatistic, boolean hasSource, int wordCount,
                            List<String> feedback) {
}
