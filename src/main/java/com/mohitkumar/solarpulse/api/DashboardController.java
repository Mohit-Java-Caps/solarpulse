package com.mohitkumar.solarpulse.api;

import com.mohitkumar.solarpulse.api.dto.AnomalyDto;
import com.mohitkumar.solarpulse.api.dto.DashboardResponseDto;
import com.mohitkumar.solarpulse.api.dto.ReadingDto;
import com.mohitkumar.solarpulse.api.dto.SiteSummaryDto;
import com.mohitkumar.solarpulse.api.dto.StatusDto;
import com.mohitkumar.solarpulse.persistence.repository.AnomalyFlagRepository;
import com.mohitkumar.solarpulse.persistence.repository.SiteRepository;
import com.mohitkumar.solarpulse.persistence.repository.TelemetryReadingRepository;
import com.mohitkumar.solarpulse.resilience.CircuitBreakerStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Dashboard", description = "Aggregated view backing the live demo dashboard")
public class DashboardController {

    private final SiteRepository siteRepository;
    private final TelemetryReadingRepository readingRepository;
    private final AnomalyFlagRepository anomalyFlagRepository;
    private final CircuitBreakerStatusService circuitBreakerStatusService;

    public DashboardController(SiteRepository siteRepository, TelemetryReadingRepository readingRepository,
                                AnomalyFlagRepository anomalyFlagRepository,
                                CircuitBreakerStatusService circuitBreakerStatusService) {
        this.siteRepository = siteRepository;
        this.readingRepository = readingRepository;
        this.anomalyFlagRepository = anomalyFlagRepository;
        this.circuitBreakerStatusService = circuitBreakerStatusService;
    }

    @Operation(summary = "Everything the dashboard needs in one call: sites+latest reading, circuit-breaker status, recent anomalies")
    @GetMapping("/api/dashboard")
    public DashboardResponseDto dashboard() {
        var sites = siteRepository.findAll().stream()
            .map(site -> {
                ReadingDto latest = readingRepository.findTop30BySiteIdOrderByObservedAtDesc(site.getId()).stream()
                    .findFirst()
                    .map(ReadingDto::from)
                    .orElse(null);
                return SiteSummaryDto.from(site, latest);
            })
            .toList();

        var recentAnomalies = anomalyFlagRepository.findTop20ByOrderByFlaggedAtDesc().stream()
            .map(AnomalyDto::from)
            .toList();

        StatusDto status = StatusDto.from(circuitBreakerStatusService.snapshot());

        return new DashboardResponseDto(sites, status, recentAnomalies);
    }
}
