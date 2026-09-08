package com.mohitkumar.solarpulse.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
    name = "telemetry_readings",
    indexes = @Index(name = "idx_reading_site_time", columnList = "site_id, observed_at")
)
public class TelemetryReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id", nullable = false)
    private String siteId;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "shortwave_radiation")
    private double shortwaveRadiationWm2;

    @Column(name = "global_tilted_irradiance")
    private double globalTiltedIrradianceWm2;

    @Column(name = "temperature_c")
    private double temperatureC;

    @Column(name = "estimated_power_kw")
    private double estimatedPowerKw;

    @Column(nullable = false)
    private boolean stale;

    @Column(nullable = false)
    private String source;

    protected TelemetryReadingEntity() {
    }

    public TelemetryReadingEntity(String siteId, Instant observedAt, double shortwaveRadiationWm2,
                                   double globalTiltedIrradianceWm2, double temperatureC,
                                   double estimatedPowerKw, boolean stale, String source) {
        this.siteId = siteId;
        this.observedAt = observedAt;
        this.shortwaveRadiationWm2 = shortwaveRadiationWm2;
        this.globalTiltedIrradianceWm2 = globalTiltedIrradianceWm2;
        this.temperatureC = temperatureC;
        this.estimatedPowerKw = estimatedPowerKw;
        this.stale = stale;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public double getShortwaveRadiationWm2() {
        return shortwaveRadiationWm2;
    }

    public double getGlobalTiltedIrradianceWm2() {
        return globalTiltedIrradianceWm2;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public double getEstimatedPowerKw() {
        return estimatedPowerKw;
    }

    public boolean isStale() {
        return stale;
    }

    public String getSource() {
        return source;
    }
}
