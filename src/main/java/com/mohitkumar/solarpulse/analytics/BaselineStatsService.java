package com.mohitkumar.solarpulse.analytics;

import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import com.mohitkumar.solarpulse.persistence.repository.TelemetryReadingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Rolling mean/stddev of the last N readings for a site, used by
// AnomalyDetectionService. Deliberately recomputed from the DB each call
// rather than maintained incrementally in memory — at this data volume
// (a handful of sites, one reading every few minutes) the query is cheap,
// and recomputing avoids a whole class of "cache went stale after a
// restart" bugs for a service this small.
@Service
public class BaselineStatsService {

    private static final int WINDOW_SIZE = 30;

    private final TelemetryReadingRepository readingRepository;

    public BaselineStatsService(TelemetryReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public Baseline computeBaseline(String siteId) {
        List<TelemetryReadingEntity> recent = readingRepository.findTop30BySiteIdOrderByObservedAtDesc(siteId);
        if (recent.size() < 5) {
            return Baseline.insufficientData();
        }

        double mean = recent.stream().mapToDouble(TelemetryReadingEntity::getEstimatedPowerKw).average().orElse(0);
        double variance = recent.stream()
            .mapToDouble(r -> Math.pow(r.getEstimatedPowerKw() - mean, 2))
            .average()
            .orElse(0);
        double stdDev = Math.sqrt(variance);

        return new Baseline(true, mean, stdDev, recent.size());
    }

    public record Baseline(boolean hasEnoughData, double mean, double stdDev, int sampleSize) {
        public static Baseline insufficientData() {
            return new Baseline(false, 0, 0, 0);
        }
    }

    public static int windowSize() {
        return WINDOW_SIZE;
    }
}
