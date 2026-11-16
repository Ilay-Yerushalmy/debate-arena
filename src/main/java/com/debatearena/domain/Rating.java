package com.debatearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A 1-5 peer rating of an argument. */
@Entity
@Table(name = "ratings")
public class Rating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "argument_id")
    private Argument argument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rater_id")
    private Participant rater;

    @Column(nullable = false)
    private int score;

    protected Rating() {
    }

    public Rating(Argument argument, Participant rater, int score) {
        this.argument = argument;
        this.rater = rater;
        this.score = score;
    }

    public Argument getArgument() {
        return argument;
    }

    public Participant getRater() {
        return rater;
    }

    public int getScore() {
        return score;
    }
}
