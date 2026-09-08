package com.mohitkumar.solarpulse.api.dto;

import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;

import java.time.Instant;

public record ReadingDto(
    Instant observedAt,
    double irradianceWm2,
    double temperatureC,
    double estimatedPowerKw,
    boolean stale,
    String source
) {
    public static ReadingDto from(TelemetryReadingEntity entity) {
        return new ReadingDto(
            entity.getObservedAt(),
            entity.getGlobalTiltedIrradianceWm2(),
            entity.getTemperatureC(),
            entity.getEstimatedPowerKw(),
            entity.isStale(),
            entity.getSource()
        );
    }
}
