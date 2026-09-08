package com.mohitkumar.solarpulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI solarPulseOpenApi() {
        return new OpenAPI().info(new Info()
            .title("SolarPulse API")
            .description("Live solar-telemetry pipeline with Resilience4j circuit-breaker/retry protection. " +
                "See /api/dashboard for the aggregated view backing the live demo UI.")
            .version("v0.1"));
    }
}
