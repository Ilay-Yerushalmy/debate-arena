package com.debatearena.web.dto;

import com.debatearena.analysis.ClaimStrength;
import com.debatearena.analysis.ParticipantRanking;
import com.debatearena.domain.Stance;

import java.util.List;

/** Outgoing JSON shapes. Entities are never exposed directly. */
public final class Responses {

    private Responses() {
    }

    public record ParticipantDto(long id, String name) {
    }

    public record ClaimDto(long id, long topicId, String text) {
    }

    public record TopicDto(long id, String title, String description, List<ClaimDto> claims) {
    }

    public record SessionDto(long id, long topicId, String topicTitle, String title, String createdAt) {
    }

    public record ArgumentDto(
            long id,
            long sessionId,
            long claimId,
            String claimText,
            long participantId,
            String participantName,
            Stance stance,
            String content,
            int qualityScore,
            boolean hasExample,
            boolean hasStatistic,
            boolean hasSource,
            Double avgRating,
            int ratingCount,
            double combinedScore) {
    }

    public record SessionDetailDto(SessionDto session, List<ClaimDto> claims, List<ArgumentDto> arguments) {
    }

    public record ClaimStrengthDto(long claimId, String claimText, ClaimStrength analysis) {
    }

    public record ClaimRankingDto(long claimId, String claimText, List<ParticipantRanking> rankings) {
    }

    public record ParticipantStatsDto(
            long participantId,
            String name,
            int argumentCount,
            int proCount,
            int conCount,
            double avgQuality,
            Double avgRatingReceived,
            long ratingsGiven,
            double avgCombinedScore,
            int examplesUsed,
            int sourcesCited) {
    }
}
