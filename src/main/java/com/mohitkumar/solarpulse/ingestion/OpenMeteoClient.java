package com.mohitkumar.solarpulse.ingestion;

import com.mohitkumar.solarpulse.config.SiteDefinition;
import com.mohitkumar.solarpulse.ingestion.dto.OpenMeteoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

// The one genuinely unreliable external dependency in this system. Every
// public call goes through Resilience4j: @Retry re-attempts a handful of
// times on transient failures, and @CircuitBreaker (configured in
// application.yml under resilience4j.circuitbreaker.instances.openMeteo)
// stops hammering Open-Meteo once failures cross the threshold, tripping
// the fallback methods below instead. See docs/ARCHITECTURE.md for the
// full flow and FailureInjectionController for how the live demo triggers
// this deterministically rather than waiting on real flakiness.
@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestClient restClient;
    private final String forecastBaseUrl;
    private final String archiveBaseUrl;

    public OpenMeteoClient(RestClient openMeteoRestClient,
                            @Value("${openmeteo.base-url}") String forecastBaseUrl,
                            @Value("${openmeteo.archive-base-url}") String archiveBaseUrl) {
        this.restClient = openMeteoRestClient;
        this.forecastBaseUrl = forecastBaseUrl;
        this.archiveBaseUrl = archiveBaseUrl;
    }

    // Live reading for "now". Requests global_tilted_irradiance — the
    // closest public proxy to "what this specific, tilted panel array
    // receives" — using the site's own tilt/azimuth.
    @CircuitBreaker(name = "openMeteo", fallbackMethod = "fetchCurrentFallback")
    @Retry(name = "openMeteo")
    public WeatherReading fetchCurrent(SiteDefinition site) {
        OpenMeteoResponse response = restClient.get()
            .uri(forecastBaseUrl + "/v1/forecast?latitude={lat}&longitude={lon}" +
                    "&hourly=shortwave_radiation,global_tilted_irradiance,temperature_2m" +
                    "&tilt={tilt}&azimuth={azimuth}&forecast_days=1&timezone=UTC",
                site.latitude(), site.longitude(), site.tilt(), site.azimuth())
            .retrieve()
            .body(OpenMeteoResponse.class);

        return latestHourlyReading(response, true);
    }

    // Fallback method signature must mirror fetchCurrent's params plus a
    // trailing Throwable — this is Resilience4j's contract, not optional
    // boilerplate. It intentionally re-throws rather than returning a
    // guessed value: TelemetryIngestService is the layer that decides
    // what "unavailable" means (serve last-known-good, mark stale).
    private WeatherReading fetchCurrentFallback(SiteDefinition site, Throwable t) {
        log.warn("Open-Meteo current-reading call failed for site {}: {}", site.id(), t.toString());
        throw new OpenMeteoUnavailableException(site.id(), t);
    }

    // Historical backfill. Deliberately does NOT request
    // global_tilted_irradiance — Open-Meteo's tilt/azimuth transposition
    // is documented against the forecast model, not guaranteed identical
    // on the archive endpoint, so backfill uses shortwave_radiation (GHI,
    // horizontal) as a documented approximation instead of assuming GTI
    // support that hasn't been verified. GenerationEstimatorService is
    // written to accept either.
    @CircuitBreaker(name = "openMeteo", fallbackMethod = "fetchHistoricalFallback")
    @Retry(name = "openMeteo")
    public List<WeatherReading> fetchHistorical(SiteDefinition site, LocalDate start, LocalDate end) {
        OpenMeteoResponse response = restClient.get()
            .uri(archiveBaseUrl + "/v1/archive?latitude={lat}&longitude={lon}" +
                    "&hourly=shortwave_radiation,temperature_2m" +
                    "&start_date={start}&end_date={end}&timezone=UTC",
                site.latitude(), site.longitude(), start, end)
            .retrieve()
            .body(OpenMeteoResponse.class);

        return allHourlyReadings(response);
    }

    private List<WeatherReading> fetchHistoricalFallback(SiteDefinition site, LocalDate start, LocalDate end, Throwable t) {
        log.warn("Open-Meteo historical backfill failed for site {}: {}", site.id(), t.toString());
        throw new OpenMeteoUnavailableException(site.id(), t);
    }

    private WeatherReading latestHourlyReading(OpenMeteoResponse response, boolean preferTilted) {
        OpenMeteoResponse.Hourly hourly = response.hourly();
        List<String> times = hourly.time();
        int idx = closestPastHourIndex(times);

        double shortwave = valueAt(hourly.shortwaveRadiation(), idx);
        double tilted = valueAt(hourly.globalTiltedIrradiance(), idx);
        double irradiance = preferTilted && tilted > 0 ? tilted : shortwave;
        double temperature = valueAt(hourly.temperature2m(), idx);

        Instant observedAt = Instant.parse(times.get(idx) + ":00Z");
        return new WeatherReading(observedAt, shortwave, irradiance, temperature);
    }

    private List<WeatherReading> allHourlyReadings(OpenMeteoResponse response) {
        OpenMeteoResponse.Hourly hourly = response.hourly();
        List<String> times = hourly.time();
        return java.util.stream.IntStream.range(0, times.size())
            .mapToObj(i -> {
                double shortwave = valueAt(hourly.shortwaveRadiation(), i);
                double temperature = valueAt(hourly.temperature2m(), i);
                Instant observedAt = Instant.parse(times.get(i) + ":00Z");
                return new WeatherReading(observedAt, shortwave, shortwave, temperature);
            })
            .toList();
    }

    private int closestPastHourIndex(List<String> isoHours) {
        Instant now = Instant.now();
        int best = 0;
        for (int i = 0; i < isoHours.size(); i++) {
            Instant hour = Instant.parse(isoHours.get(i) + ":00Z");
            if (!hour.isAfter(now)) {
                best = i;
            } else {
                break;
            }
        }
        return best;
    }

    private double valueAt(List<Double> values, int idx) {
        if (values == null || idx >= values.size() || values.get(idx) == null) {
            return 0.0;
        }
        return values.get(idx);
    }

    // Exposed so a startup seeder can decide how far back to backfill
    // without duplicating the "N days ago" calculation.
    public static LocalDate daysAgo(int days) {
        return LocalDate.now(ZoneOffset.UTC).minusDays(days);
    }
}
