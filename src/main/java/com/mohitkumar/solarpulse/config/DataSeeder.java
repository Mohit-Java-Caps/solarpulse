package com.mohitkumar.solarpulse.config;

import com.mohitkumar.solarpulse.ingestion.OpenMeteoClient;
import com.mohitkumar.solarpulse.ingestion.OpenMeteoUnavailableException;
import com.mohitkumar.solarpulse.ingestion.WeatherReading;
import com.mohitkumar.solarpulse.persistence.entity.SiteEntity;
import com.mohitkumar.solarpulse.persistence.repository.SiteRepository;
import com.mohitkumar.solarpulse.processing.TelemetryIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Runs once on startup: makes sure every SiteEntity row exists, then
// backfills a couple of weeks of history per site so the dashboard has
// real historical variation from the very first request instead of a
// single flat starting point. Safe to run on every restart — both steps
// are idempotent (existsById / existsBySiteIdAndObservedAt checks).
// Disabled in tests (solarpulse.seed-on-startup=false) so resilience
// tests can control exactly what the mocked Open-Meteo dependency does
// without racing a startup-time backfill call.
@Component
@ConditionalOnProperty(name = "solarpulse.seed-on-startup", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SiteRepository siteRepository;
    private final OpenMeteoClient openMeteoClient;
    private final TelemetryIngestService telemetryIngestService;
    private final int backfillDays;

    public DataSeeder(SiteRepository siteRepository, OpenMeteoClient openMeteoClient,
                       TelemetryIngestService telemetryIngestService,
                       @Value("${openmeteo.backfill-days}") int backfillDays) {
        this.siteRepository = siteRepository;
        this.openMeteoClient = openMeteoClient;
        this.telemetryIngestService = telemetryIngestService;
        this.backfillDays = backfillDays;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedSites();
        seedHistoricalReadings();
    }

    private void seedSites() {
        for (SiteDefinition def : SiteCatalog.SITES) {
            if (siteRepository.existsById(def.id())) {
                continue;
            }
            siteRepository.save(new SiteEntity(def.id(), def.name(), def.latitude(), def.longitude(),
                def.capacityKw(), def.tilt(), def.azimuth(), def.derateFactor()));
            log.info("Seeded site {}", def.id());
        }
    }

    private void seedHistoricalReadings() {
        LocalDate start = OpenMeteoClient.daysAgo(backfillDays);
        LocalDate end = OpenMeteoClient.daysAgo(1);

        for (SiteDefinition site : SiteCatalog.SITES) {
            try {
                List<WeatherReading> historical = openMeteoClient.fetchHistorical(site, start, end);
                int saved = telemetryIngestService.backfill(site, historical);
                if (saved > 0) {
                    log.info("Backfilled {} historical readings for site {}", saved, site.id());
                }
            } catch (OpenMeteoUnavailableException e) {
                // Backfill is a nice-to-have at startup, not a hard
                // dependency — the live poller will still populate data
                // going forward even if this one-time call fails.
                log.warn("Historical backfill unavailable for site {} at startup: {}", site.id(), e.getMessage());
            }
        }
    }
}
