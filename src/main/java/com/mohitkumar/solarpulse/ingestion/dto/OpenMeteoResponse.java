package com.mohitkumar.solarpulse.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Maps the subset of Open-Meteo's forecast/archive response we actually
// use. Radiation fields are W/m^2 (instantaneous, hourly-averaged);
// temperature_2m is degrees C. See docs/ARCHITECTURE.md for the exact
// endpoints and why global_tilted_irradiance is only requested on the
// forecast call, not the historical archive call (see OpenMeteoClient).
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
        List<String> time,
        @JsonProperty("shortwave_radiation") List<Double> shortwaveRadiation,
        @JsonProperty("global_tilted_irradiance") List<Double> globalTiltedIrradiance,
        @JsonProperty("temperature_2m") List<Double> temperature2m
    ) {
    }
}
