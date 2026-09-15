package com.mohitkumar.solarpulse.api;

import com.mohitkumar.solarpulse.api.dto.AnomalyDto;
import com.mohitkumar.solarpulse.api.dto.ReadingDto;
import com.mohitkumar.solarpulse.api.dto.SiteSummaryDto;
import com.mohitkumar.solarpulse.persistence.entity.SiteEntity;
import com.mohitkumar.solarpulse.persistence.repository.AnomalyFlagRepository;
import com.mohitkumar.solarpulse.persistence.repository.SiteRepository;
import com.mohitkumar.solarpulse.persistence.repository.TelemetryReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@Tag(name = "Sites", description = "Solar site catalog, telemetry history, and anomaly flags")
public class SiteController {

    private final SiteRepository siteRepository;
    private final TelemetryReadingRepository readingRepository;
    private final AnomalyFlagRepository anomalyFlagRepository;

    public SiteController(SiteRepository siteRepository, TelemetryReadingRepository readingRepository,
                           AnomalyFlagRepository anomalyFlagRepository) {
        this.siteRepository = siteRepository;
        this.readingRepository = readingRepository;
        this.anomalyFlagRepository = anomalyFlagRepository;
    }

    @Operation(summary = "List all demo sites with their latest reading")
    @GetMapping
    public List<SiteSummaryDto> listSites() {
        return siteRepository.findAll().stream()
            .map(site -> SiteSummaryDto.from(site, latestReadingFor(site.getId())))
            .toList();
    }

    @Operation(summary = "Get a single site's recent telemetry history, most recent first (default 30, capped at 500)")
    @GetMapping("/{siteId}/readings")
    public ResponseEntity<List<ReadingDto>> readings(@PathVariable String siteId,
                                                       @RequestParam(defaultValue = "30") int limit) {
        if (!siteRepository.existsById(siteId)) {
            return ResponseEntity.notFound().build();
        }
        int cappedLimit = Math.min(Math.max(limit, 1), 500);
        List<ReadingDto> readings = readingRepository.findBySiteIdOrderByObservedAtDesc(siteId).stream()
            .limit(cappedLimit)
            .map(ReadingDto::from)
            .toList();
        return ResponseEntity.ok(readings);
    }

    @Operation(summary = "Get a single site's recent anomaly flags")
    @GetMapping("/{siteId}/anomalies")
    public ResponseEntity<List<AnomalyDto>> anomalies(@PathVariable String siteId) {
        if (!siteRepository.existsById(siteId)) {
            return ResponseEntity.notFound().build();
        }
        List<AnomalyDto> anomalies = anomalyFlagRepository.findTop20BySiteIdOrderByFlaggedAtDesc(siteId).stream()
            .map(AnomalyDto::from)
            .toList();
        return ResponseEntity.ok(anomalies);
    }

    private ReadingDto latestReadingFor(String siteId) {
        return readingRepository.findTop30BySiteIdOrderByObservedAtDesc(siteId).stream()
            .findFirst()
            .map(ReadingDto::from)
            .orElse(null);
    }
}
