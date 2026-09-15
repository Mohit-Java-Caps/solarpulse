package com.mohitkumar.solarpulse.api;

import com.mohitkumar.solarpulse.resilience.CircuitBreakerEventListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/status")
@Tag(name = "Status", description = "Circuit-breaker state-transition history")
public class StatusController {

    private final CircuitBreakerEventListener eventListener;

    public StatusController(CircuitBreakerEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Operation(summary = "Recent circuit-breaker state transitions (most recent first), from Resilience4j's own event publisher")
    @GetMapping("/events")
    public List<CircuitBreakerEventListener.CircuitBreakerEvent> events() {
        return eventListener.recentEvents();
    }
}
