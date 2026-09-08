package com.mohitkumar.solarpulse.resilience;

import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackCacheServiceTest {

    @Test
    void returnsEmptyWhenNothingCachedYet() {
        FallbackCacheService cache = new FallbackCacheService();

        Optional<TelemetryReadingEntity> result = cache.getLastGood("no-such-site");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsTheLastRecordedReadingForThatSiteOnly() {
        FallbackCacheService cache = new FallbackCacheService();
        TelemetryReadingEntity siteAReading = new TelemetryReadingEntity(
            "site-a", Instant.now(), 400, 450, 22.0, 180.0, false, "open-meteo-live");
        TelemetryReadingEntity siteBReading = new TelemetryReadingEntity(
            "site-b", Instant.now(), 100, 120, 15.0, 40.0, false, "open-meteo-live");

        cache.record("site-a", siteAReading);
        cache.record("site-b", siteBReading);

        assertEquals(180.0, cache.getLastGood("site-a").orElseThrow().getEstimatedPowerKw());
        assertEquals(40.0, cache.getLastGood("site-b").orElseThrow().getEstimatedPowerKw());
    }

    @Test
    void overwritesThePreviousReadingForTheSameSite() {
        FallbackCacheService cache = new FallbackCacheService();
        cache.record("site-a", new TelemetryReadingEntity(
            "site-a", Instant.now(), 400, 450, 22.0, 180.0, false, "open-meteo-live"));
        cache.record("site-a", new TelemetryReadingEntity(
            "site-a", Instant.now(), 410, 460, 23.0, 190.0, false, "open-meteo-live"));

        assertEquals(190.0, cache.getLastGood("site-a").orElseThrow().getEstimatedPowerKw());
    }
}
