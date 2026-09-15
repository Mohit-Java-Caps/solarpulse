package com.mohitkumar.solarpulse.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// Subscribes directly to Resilience4j's own event publisher — this is
// event-driven history, not a poller reconstructing state after the
// fact. Bounded to the most recent transitions; this is operational
// history for the live demo, not an audit log that needs to survive a
// restart.
@Component
public class CircuitBreakerEventListener {

    private static final int MAX_EVENTS = 20;

    private final LinkedList<CircuitBreakerEvent> events = new LinkedList<>();
    private final CircuitBreakerRegistry registry;

    public CircuitBreakerEventListener(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void subscribe() {
        registry.circuitBreaker(CircuitBreakerStatusService.OPEN_METEO_BREAKER)
            .getEventPublisher()
            .onStateTransition(event -> record(
                event.getStateTransition().getFromState().name(),
                event.getStateTransition().getToState().name()
            ));
    }

    private synchronized void record(String fromState, String toState) {
        events.addFirst(new CircuitBreakerEvent(Instant.now(), fromState, toState));
        while (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    public synchronized List<CircuitBreakerEvent> recentEvents() {
        return Collections.unmodifiableList(new LinkedList<>(events));
    }

    public record CircuitBreakerEvent(Instant timestamp, String fromState, String toState) {
    }
}
