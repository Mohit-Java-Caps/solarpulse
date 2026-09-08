package com.mohitkumar.solarpulse.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "anomaly_flags")
public class AnomalyFlagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id", nullable = false)
    private String siteId;

    @Column(name = "reading_id", nullable = false)
    private Long readingId;

    @Column(name = "flagged_at", nullable = false)
    private Instant flaggedAt;

    @Column(name = "z_score")
    private double zScore;

    private String reason;

    protected AnomalyFlagEntity() {
    }

    public AnomalyFlagEntity(String siteId, Long readingId, Instant flaggedAt, double zScore, String reason) {
        this.siteId = siteId;
        this.readingId = readingId;
        this.flaggedAt = flaggedAt;
        this.zScore = zScore;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    public Long getReadingId() {
        return readingId;
    }

    public Instant getFlaggedAt() {
        return flaggedAt;
    }

    public double getZScore() {
        return zScore;
    }

    public String getReason() {
        return reason;
    }
}
