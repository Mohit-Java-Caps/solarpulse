package com.mohitkumar.solarpulse.ingestion;

import com.mohitkumar.solarpulse.config.SiteCatalog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Exercises the actual behavior the live "inject failure" demo relies on:
// repeated failures open the circuit (and, crucially, stop making real
// HTTP calls once open), and a successful call during the half-open probe
// closes it again. Uses MockRestServiceServer bound to the real
// RestClient.Builder bean so the Resilience4j annotations run through
// their real Spring AOP proxying, not a hand-rolled substitute.
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "solarpulse.seed-on-startup=false",
    "resilience4j.circuitbreaker.instances.openMeteo.minimumNumberOfCalls=2",
    "resilience4j.circuitbreaker.instances.openMeteo.slidingWindowSize=2",
    "resilience4j.circuitbreaker.instances.openMeteo.waitDurationInOpenState=1s",
    "resilience4j.circuitbreaker.instances.openMeteo.permittedNumberOfCallsInHalfOpenState=1",
    "resilience4j.retry.instances.openMeteo.maxAttempts=1"
})
class OpenMeteoClientResilienceTest {

    private static final String SUCCESS_BODY = """
        {"hourly":{"time":["2020-01-01T00:00"],
        "shortwave_radiation":[500.0],"global_tilted_irradiance":[600.0],"temperature_2m":[20.0]}}
        """;

    @Autowired
    private OpenMeteoClient openMeteoClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private MockRestServiceServer mockServer;

    private final com.mohitkumar.solarpulse.config.SiteDefinition site = SiteCatalog.SITES.get(0);

    @BeforeEach
    void resetState() {
        mockServer.reset();
        circuitBreakerRegistry.circuitBreaker("openMeteo").reset();
    }

    @Test
    void repeatedFailuresOpenTheCircuitAndStopFurtherHttpCalls() {
        mockServer.expect(method(org.springframework.http.HttpMethod.GET)).andRespond(withServerError());
        mockServer.expect(method(org.springframework.http.HttpMethod.GET)).andRespond(withServerError());

        assertThrows(OpenMeteoUnavailableException.class, () -> openMeteoClient.fetchCurrent(site));
        assertThrows(OpenMeteoUnavailableException.class, () -> openMeteoClient.fetchCurrent(site));

        assertEquals(CircuitBreaker.State.OPEN,
            circuitBreakerRegistry.circuitBreaker("openMeteo").getState());

        // A third call while OPEN must short-circuit locally, not make a
        // third HTTP call — mockServer only has 2 expectations registered,
        // so verify() below would fail if a third request were attempted.
        assertThrows(OpenMeteoUnavailableException.class, () -> openMeteoClient.fetchCurrent(site));
        mockServer.verify();
    }

    @Test
    void aSuccessfulProbeDuringHalfOpenClosesTheCircuitAgain() throws InterruptedException {
        circuitBreakerRegistry.circuitBreaker("openMeteo").transitionToOpenState();

        // waitDurationInOpenState is 1s in this test config.
        Thread.sleep(1100);

        mockServer.expect(method(org.springframework.http.HttpMethod.GET))
            .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

        WeatherReading reading = openMeteoClient.fetchCurrent(site);

        assertEquals(600.0, reading.irradianceWm2());
        assertEquals(CircuitBreaker.State.CLOSED,
            circuitBreakerRegistry.circuitBreaker("openMeteo").getState());
    }

    @TestConfiguration
    static class MockRestClientConfig {

        @Bean
        public RestClient.Builder testRestClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        public MockRestServiceServer mockServer(RestClient.Builder testRestClientBuilder) {
            return MockRestServiceServer.bindTo(testRestClientBuilder).build();
        }

        // Depends on mockServer (even though it doesn't use it directly)
        // purely to force Spring to create mockServer first — its bean
        // method mutates testRestClientBuilder's request factory, which
        // must happen before .build() is called here.
        @Bean
        @Primary
        public RestClient openMeteoRestClient(RestClient.Builder testRestClientBuilder, MockRestServiceServer mockServer) {
            return testRestClientBuilder.build();
        }
    }
}
