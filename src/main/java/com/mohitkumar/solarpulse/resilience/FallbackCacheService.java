package com.mohitkumar.solarpulse.resilience;

import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// The "last-known-good" half of the graceful-degradation story: when the
// circuit is open, TelemetryIngestService serves whatever was cached here
// instead of failing the request outright, tagged stale=true so the API
// and dashboard can show it honestly. Deliberately in-memory — this is a
// short-lived operational cache, not a data store; losing it on restart
// just means "no cached fallback yet," which is a safe default.
@Component
public class FallbackCacheService {

    private final Map<String, TelemetryReadingEntity> lastGoodBySite = new ConcurrentHashMap<>();

    public void record(String siteId, TelemetryReadingEntity reading) {
        lastGoodBySite.put(siteId, reading);
    }

    public Optional<TelemetryReadingEntity> getLastGood(String siteId) {
        return Optional.ofNullable(lastGoodBySite.get(siteId));
    }
}
