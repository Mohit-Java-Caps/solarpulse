package com.mohitkumar.solarpulse.api.dto;

import com.mohitkumar.solarpulse.resilience.CircuitBreakerStatusService;

public record StatusDto(String circuitBreakerState, float failureRate,
                         int bufferedCalls, int failedCalls, int successfulCalls) {
    public static StatusDto from(CircuitBreakerStatusService.CircuitBreakerSnapshot snapshot) {
        return new StatusDto(snapshot.state(), snapshot.failureRate(),
            snapshot.bufferedCalls(), snapshot.failedCalls(), snapshot.successfulCalls());
    }
}
