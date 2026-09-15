package com.mohitkumar.solarpulse.api.dto;

public record SystemInfoDto(
    String version,
    String activeProfile,
    long uptimeSeconds,
    ResilienceConfig resilience
) {
    public record ResilienceConfig(
        float failureRateThreshold,
        int slidingWindowSize,
        int minimumNumberOfCalls,
        String waitDurationInOpenState
    ) {
    }
}
