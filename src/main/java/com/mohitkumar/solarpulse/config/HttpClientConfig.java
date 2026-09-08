package com.mohitkumar.solarpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

// A short, explicit timeout matters here: without one, a slow-but-not-yet-
// failed Open-Meteo response would hang the scheduled poll thread instead
// of triggering Resilience4j's Retry/CircuitBreaker — timeouts are what
// make "unavailable" actually observable to the resilience layer.
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient openMeteoRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        return builder.requestFactory(requestFactory).build();
    }
}
