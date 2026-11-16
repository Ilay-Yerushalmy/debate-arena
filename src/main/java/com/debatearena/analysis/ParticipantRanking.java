package com.debatearena.analysis;

/**
 * @param rank        competition rank (1, 2, 2, 4 ...)
 * @param avgRating   average peer rating received, {@code null} if never rated
 * @param score       mean combined score of the participant's arguments (0-100)
 */
public record ParticipantRanking(int rank, long participantId, String participantName, int argumentCount,
                                 Double avgRating, double avgQuality, double score) {
}
