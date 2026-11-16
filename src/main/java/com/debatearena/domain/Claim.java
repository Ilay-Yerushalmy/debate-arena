package com.debatearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A statement inside a topic that participants argue for (PRO) or against (CON). */
@Entity
@Table(name = "claims")
public class Claim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private String text;

    protected Claim() {
    }

    public Claim(Topic topic, String text) {
        this.topic = topic;
        this.text = text;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }
}
