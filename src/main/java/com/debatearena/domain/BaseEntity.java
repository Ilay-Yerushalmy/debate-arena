package com.debatearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Common id + creation timestamp for all entities. */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ISO-8601 text - SQLite has no native timestamp type. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
