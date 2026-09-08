package com.mohitkumar.solarpulse.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Service;

@Service
public class CircuitBreakerStatusService {

    public static final String OPEN_METEO_BREAKER = "openMeteo";

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerStatusService(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    public CircuitBreakerSnapshot snapshot() {
        CircuitBreaker breaker = registry.circuitBreaker(OPEN_METEO_BREAKER);
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        return new CircuitBreakerSnapshot(
            breaker.getState().name(),
            metrics.getFailureRate(),
            metrics.getNumberOfBufferedCalls(),
            metrics.getNumberOfFailedCalls(),
            metrics.getNumberOfSuccessfulCalls()
        );
    }

    public record CircuitBreakerSnapshot(
        String state,
        float failureRate,
        int bufferedCalls,
        int failedCalls,
        int successfulCalls
    ) {
    }
}
