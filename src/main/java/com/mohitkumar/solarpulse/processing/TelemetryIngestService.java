package com.mohitkumar.solarpulse.processing;

import com.mohitkumar.solarpulse.analytics.AnomalyDetectionService;
import com.mohitkumar.solarpulse.analytics.BaselineStatsService;
import com.mohitkumar.solarpulse.config.SiteDefinition;
import com.mohitkumar.solarpulse.ingestion.OpenMeteoClient;
import com.mohitkumar.solarpulse.ingestion.OpenMeteoUnavailableException;
import com.mohitkumar.solarpulse.ingestion.WeatherReading;
import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import com.mohitkumar.solarpulse.persistence.repository.TelemetryReadingRepository;
import com.mohitkumar.solarpulse.resilience.FallbackCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

// Orchestrates one poll cycle for one site: fetch -> estimate -> persist
// -> anomaly-check, with the fetch step wrapped in the fallback-to-cache
// degradation path. This is the class that turns "Open-Meteo call failed"
// into "system keeps answering, just with a flagged stale reading" —
// the actual point of the whole resilience story.
@Service
public class TelemetryIngestService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestService.class);
    private static final String LIVE_SOURCE = "open-meteo-live";
    private static final String BACKFILL_SOURCE = "open-meteo-archive";
    private static final String STALE_FALLBACK_SOURCE = "fallback-cache";

    private final OpenMeteoClient openMeteoClient;
    private final GenerationEstimatorService generationEstimatorService;
    private final TelemetryReadingRepository readingRepository;
    private final FallbackCacheService fallbackCacheService;
    private final BaselineStatsService baselineStatsService;
    private final AnomalyDetectionService anomalyDetectionService;

    public TelemetryIngestService(OpenMeteoClient openMeteoClient,
                                   GenerationEstimatorService generationEstimatorService,
                                   TelemetryReadingRepository readingRepository,
                                   FallbackCacheService fallbackCacheService,
                                   BaselineStatsService baselineStatsService,
                                   AnomalyDetectionService anomalyDetectionService) {
        this.openMeteoClient = openMeteoClient;
        this.generationEstimatorService = generationEstimatorService;
        this.readingRepository = readingRepository;
        this.fallbackCacheService = fallbackCacheService;
        this.baselineStatsService = baselineStatsService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    public void ingestLive(SiteDefinition site) {
        // Baseline must come from readings already in the DB, before this
        // cycle's reading is added — otherwise a genuine spike would
        // widen its own comparison baseline and could never look anomalous.
        BaselineStatsService.Baseline baseline = baselineStatsService.computeBaseline(site.id());

        TelemetryReadingEntity reading;
        try {
            WeatherReading weather = openMeteoClient.fetchCurrent(site);
            double powerKw = generationEstimatorService.estimatePowerKw(site, weather.irradianceWm2(), weather.temperatureC());
            reading = persistReading(site, weather, powerKw, false, LIVE_SOURCE);
            fallbackCacheService.record(site.id(), reading);
        } catch (OpenMeteoUnavailableException e) {
            log.info("Serving last-known-good reading for site {} (circuit open or retries exhausted)", site.id());
            reading = fallbackCacheService.getLastGood(site.id())
                .map(lastGood -> cloneAsStale(lastGood))
                .map(readingRepository::save)
                .orElse(null);
            if (reading == null) {
                log.warn("No cached fallback available yet for site {} — skipping this cycle.", site.id());
                return;
            }
        }

        anomalyDetectionService.evaluate(reading, baseline);
    }

    public int backfill(SiteDefinition site, java.util.List<WeatherReading> historicalReadings) {
        int saved = 0;
        for (WeatherReading weather : historicalReadings) {
            if (readingRepository.existsBySiteIdAndObservedAt(site.id(), weather.observedAt())) {
                continue;
            }
            double powerKw = generationEstimatorService.estimatePowerKw(site, weather.irradianceWm2(), weather.temperatureC());
            persistReading(site, weather, powerKw, false, BACKFILL_SOURCE);
            saved++;
        }
        return saved;
    }

    private TelemetryReadingEntity persistReading(SiteDefinition site, WeatherReading weather, double powerKw,
                                                    boolean stale, String source) {
        TelemetryReadingEntity entity = new TelemetryReadingEntity(
            site.id(), weather.observedAt(), weather.shortwaveRadiationWm2(),
            weather.irradianceWm2(), weather.temperatureC(), powerKw, stale, source);
        return readingRepository.save(entity);
    }

    private TelemetryReadingEntity cloneAsStale(TelemetryReadingEntity lastGood) {
        return new TelemetryReadingEntity(
            lastGood.getSiteId(), Instant.now(), lastGood.getShortwaveRadiationWm2(),
            lastGood.getGlobalTiltedIrradianceWm2(), lastGood.getTemperatureC(),
            lastGood.getEstimatedPowerKw(), true, STALE_FALLBACK_SOURCE);
    }
}
