package com.debatearena.web.dto;

import com.debatearena.domain.Stance;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Incoming request bodies, validated with Bean Validation. */
public final class Requests {

    private Requests() {
    }

    public record CreateParticipant(@NotBlank @Size(max = 40) String name) {
    }

    public record CreateTopic(@NotBlank @Size(max = 120) String title, @Size(max = 500) String description) {
    }

    public record CreateClaim(@NotBlank @Size(max = 300) String text) {
    }

    public record CreateSession(@NotNull Long topicId, @NotBlank @Size(max = 120) String title) {
    }

    public record CreateArgument(
            @NotNull Long sessionId,
            @NotNull Long claimId,
            @NotNull Long participantId,
            @NotNull Stance stance,
            @NotBlank @Size(min = 10, max = 2000) String content) {
    }

    public record RateArgument(@NotNull Long raterId, @NotNull @Min(1) @Max(5) Integer score) {
    }

    public record AssessText(@NotBlank @Size(max = 2000) String text) {
    }
}
