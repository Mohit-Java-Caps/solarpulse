package com.mohitkumar.solarpulse.ingestion;

// Thrown by OpenMeteoClient's fallback methods once the circuit is open
// (or retries are exhausted) so callers have a single, simple contract:
// either a real WeatherReading, or this. TelemetryIngestService catches
// it and switches to FallbackCacheService's last-known-good reading.
public class OpenMeteoUnavailableException extends RuntimeException {

    public OpenMeteoUnavailableException(String siteId, Throwable cause) {
        super("Open-Meteo unavailable for site " + siteId, cause);
    }
}
