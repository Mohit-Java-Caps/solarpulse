package com.mohitkumar.solarpulse.ingestion;

import java.time.Instant;

// irradianceWm2 is global_tilted_irradiance when the caller requested it
// (live polling), or shortwave_radiation (horizontal, GHI) as a fallback
// when GTI wasn't available (historical backfill — see OpenMeteoClient).
// GenerationEstimatorService treats both the same way; the difference is
// a documented simplification, not a bug.
public record WeatherReading(Instant observedAt, double shortwaveRadiationWm2,
                              double irradianceWm2, double temperatureC) {
}
