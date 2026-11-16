package com.debatearena.domain;

import com.debatearena.analysis.QualityReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "arguments")
public class Argument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private DebateSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stance stance;

    @Column(nullable = false)
    private String content;

    // Results of the technical assessment, stored so analytics can be done in SQL.
    @Column(name = "has_example", nullable = false)
    private boolean hasExample;

    @Column(name = "has_statistic", nullable = false)
    private boolean hasStatistic;

    @Column(name = "has_source", nullable = false)
    private boolean hasSource;

    @Column(name = "quality_score", nullable = false)
    private int qualityScore;

    protected Argument() {
    }

    public Argument(DebateSession session, Claim claim, Participant participant, Stance stance, String content,
                    QualityReport quality) {
        this.session = session;
        this.claim = claim;
        this.participant = participant;
        this.stance = stance;
        this.content = content;
        this.hasExample = quality.hasExample();
        this.hasStatistic = quality.hasStatistic();
        this.hasSource = quality.hasSource();
        this.qualityScore = quality.score();
    }

    public DebateSession getSession() {
        return session;
    }

    public Claim getClaim() {
        return claim;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Stance getStance() {
        return stance;
    }

    public String getContent() {
        return content;
    }

    public boolean hasExample() {
        return hasExample;
    }

    public boolean hasStatistic() {
        return hasStatistic;
    }

    public boolean hasSource() {
        return hasSource;
    }

    public int getQualityScore() {
        return qualityScore;
    }
}
