package com.debatearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One debate event about a topic. The same claims can be debated across many sessions. */
@Entity
@Table(name = "debate_sessions")
public class DebateSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private String title;

    protected DebateSession() {
    }

    public DebateSession(Topic topic, String title) {
        this.topic = topic;
        this.title = title;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getTitle() {
        return title;
    }
}
