package com.mohitkumar.solarpulse.analytics;

import com.mohitkumar.solarpulse.persistence.entity.AnomalyFlagEntity;
import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import com.mohitkumar.solarpulse.persistence.repository.AnomalyFlagRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

// Deliberately rule-based statistics, not machine learning — a rolling
// z-score against BaselineStatsService's mean/stddev. The candidate
// already has two AI-flavored projects; this project's job is to prove
// backend/distributed-systems engineering, so anomaly detection here
// stays honestly simple rather than reaching for an LLM to look more
// impressive than it is.
@Service
public class AnomalyDetectionService {

    private static final double Z_SCORE_THRESHOLD = 2.5;

    private final BaselineStatsService baselineStatsService;
    private final AnomalyFlagRepository anomalyFlagRepository;

    public AnomalyDetectionService(BaselineStatsService baselineStatsService,
                                    AnomalyFlagRepository anomalyFlagRepository) {
        this.baselineStatsService = baselineStatsService;
        this.anomalyFlagRepository = anomalyFlagRepository;
    }

    // Baseline must be computed from readings *before* this one — see
    // TelemetryIngestService, which fetches the baseline prior to
    // persisting the new reading so it can't skew its own comparison.
    public Optional<AnomalyFlagEntity> evaluate(TelemetryReadingEntity reading,
                                                 BaselineStatsService.Baseline baseline) {
        if (!baseline.hasEnoughData() || baseline.stdDev() < 0.01) {
            return Optional.empty();
        }

        double zScore = (reading.getEstimatedPowerKw() - baseline.mean()) / baseline.stdDev();
        if (Math.abs(zScore) <= Z_SCORE_THRESHOLD) {
            return Optional.empty();
        }

        String reason = zScore < 0
            ? "Output %.2f kW is far below the site's recent baseline (%.2f kW avg) for comparable conditions."
                .formatted(reading.getEstimatedPowerKw(), baseline.mean())
            : "Output %.2f kW is far above the site's recent baseline (%.2f kW avg) for comparable conditions."
                .formatted(reading.getEstimatedPowerKw(), baseline.mean());

        AnomalyFlagEntity flag = new AnomalyFlagEntity(
            reading.getSiteId(), reading.getId(), Instant.now(), zScore, reason);
        return Optional.of(anomalyFlagRepository.save(flag));
    }
}
