package com.mohitkumar.solarpulse.ingestion;

import com.mohitkumar.solarpulse.config.SiteCatalog;
import com.mohitkumar.solarpulse.config.SiteDefinition;
import com.mohitkumar.solarpulse.processing.TelemetryIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeatherPollingScheduler.class);

    private final TelemetryIngestService telemetryIngestService;

    public WeatherPollingScheduler(TelemetryIngestService telemetryIngestService) {
        this.telemetryIngestService = telemetryIngestService;
    }

    @Scheduled(initialDelayString = "${openmeteo.poll-interval-ms}", fixedRateString = "${openmeteo.poll-interval-ms}")
    public void pollAllSites() {
        for (SiteDefinition site : SiteCatalog.SITES) {
            try {
                telemetryIngestService.ingestLive(site);
            } catch (Exception e) {
                // One site's unexpected failure must not skip the rest of
                // the batch — each site is independent.
                log.error("Unexpected error polling site {}: {}", site.id(), e.toString());
            }
        }
    }
}
