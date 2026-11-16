package com.debatearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "topics")
public class Topic extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String title;

    private String description;

    protected Topic() {
    }

    public Topic(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
