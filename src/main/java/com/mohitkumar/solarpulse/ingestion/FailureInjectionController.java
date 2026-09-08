package com.mohitkumar.solarpulse.ingestion;

import com.mohitkumar.solarpulse.resilience.CircuitBreakerStatusService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Demo-only control: forces the Open-Meteo circuit breaker open/closed
// directly via Resilience4j's own registry API, rather than faking a
// network failure inside OpenMeteoClient. This is the *reliable* trigger
// the live dashboard's "inject failure" button calls — real Open-Meteo
// flakiness would work too, but isn't something a recruiter should have
// to wait on. There's no auth on these endpoints: this is a portfolio
// demo, not a production admin surface (documented explicitly in the
// README's known-limitations section, not hidden).
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Demo controls", description = "Manually drive the circuit breaker for the live resilience demo")
public class FailureInjectionController {

    private final CircuitBreakerRegistry registry;

    public FailureInjectionController(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    @Operation(summary = "Force the Open-Meteo circuit breaker open, simulating an outage")
    @PostMapping("/inject-failure")
    public CircuitBreakerStatusService.CircuitBreakerSnapshot injectFailure() {
        CircuitBreaker breaker = registry.circuitBreaker(CircuitBreakerStatusService.OPEN_METEO_BREAKER);
        breaker.transitionToOpenState();
        return snapshotOf(breaker);
    }

    @Operation(summary = "Force the Open-Meteo circuit breaker closed, ending the simulated outage immediately")
    @PostMapping("/reset")
    public CircuitBreakerStatusService.CircuitBreakerSnapshot reset() {
        CircuitBreaker breaker = registry.circuitBreaker(CircuitBreakerStatusService.OPEN_METEO_BREAKER);
        breaker.transitionToClosedState();
        return snapshotOf(breaker);
    }

    private CircuitBreakerStatusService.CircuitBreakerSnapshot snapshotOf(CircuitBreaker breaker) {
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        return new CircuitBreakerStatusService.CircuitBreakerSnapshot(
            breaker.getState().name(),
            metrics.getFailureRate(),
            metrics.getNumberOfBufferedCalls(),
            metrics.getNumberOfFailedCalls(),
            metrics.getNumberOfSuccessfulCalls()
        );
    }
}
