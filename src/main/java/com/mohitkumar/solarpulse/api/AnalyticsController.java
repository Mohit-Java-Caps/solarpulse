package com.mohitkumar.solarpulse.api;

import com.mohitkumar.solarpulse.api.dto.AnalyticsSummaryDto;
import com.mohitkumar.solarpulse.persistence.entity.SiteEntity;
import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import com.mohitkumar.solarpulse.persistence.repository.AnomalyFlagRepository;
import com.mohitkumar.solarpulse.persistence.repository.SiteRepository;
import com.mohitkumar.solarpulse.persistence.repository.TelemetryReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// Fleet-wide numbers derived from real DB state at request time — no
// caching, no precomputed rollups. At this data volume (a handful of
// sites) that's simpler and cheap enough to just compute on every call.
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Fleet-wide summary metrics across all sites")
public class AnalyticsController {

    private final SiteRepository siteRepository;
    private final TelemetryReadingRepository readingRepository;
    private final AnomalyFlagRepository anomalyFlagRepository;

    public AnalyticsController(SiteRepository siteRepository, TelemetryReadingRepository readingRepository,
                                AnomalyFlagRepository anomalyFlagRepository) {
        this.siteRepository = siteRepository;
        this.readingRepository = readingRepository;
        this.anomalyFlagRepository = anomalyFlagRepository;
    }

    @Operation(summary = "Fleet-wide KPIs: total capacity/output, active/stale site counts, anomalies in the last 24h")
    @GetMapping("/summary")
    public AnalyticsSummaryDto summary() {
        List<SiteEntity> sites = siteRepository.findAll();
        double totalCapacity = sites.stream().mapToDouble(SiteEntity::getCapacityKw).sum();

        double totalOutput = 0;
        int active = 0;
        int stale = 0;
        for (SiteEntity site : sites) {
            Optional<TelemetryReadingEntity> latest = readingRepository
                .findTop30BySiteIdOrderByObservedAtDesc(site.getId())
                .stream()
                .findFirst();
            if (latest.isPresent()) {
                totalOutput += latest.get().getEstimatedPowerKw();
                active++;
                if (latest.get().isStale()) {
                    stale++;
                }
            }
        }

        long anomalies24h = anomalyFlagRepository.countByFlaggedAtAfter(Instant.now().minus(Duration.ofHours(24)));

        return new AnalyticsSummaryDto(totalCapacity, totalOutput, sites.size(), active, stale, anomalies24h);
    }
}
