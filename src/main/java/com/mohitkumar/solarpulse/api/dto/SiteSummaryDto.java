package com.mohitkumar.solarpulse.api.dto;

import com.mohitkumar.solarpulse.persistence.entity.SiteEntity;

public record SiteSummaryDto(
    String id,
    String name,
    double latitude,
    double longitude,
    double capacityKw,
    ReadingDto latestReading
) {
    public static SiteSummaryDto from(SiteEntity entity, ReadingDto latestReading) {
        return new SiteSummaryDto(
            entity.getId(), entity.getName(), entity.getLatitude(), entity.getLongitude(),
            entity.getCapacityKw(), latestReading
        );
    }
}
