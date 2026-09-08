package com.mohitkumar.solarpulse.api.dto;

import com.mohitkumar.solarpulse.persistence.entity.AnomalyFlagEntity;

import java.time.Instant;

public record AnomalyDto(String siteId, Instant flaggedAt, double zScore, String reason) {
    public static AnomalyDto from(AnomalyFlagEntity entity) {
        return new AnomalyDto(entity.getSiteId(), entity.getFlaggedAt(), entity.getZScore(), entity.getReason());
    }
}
