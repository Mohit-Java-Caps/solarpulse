package com.mohitkumar.solarpulse.api;

import com.mohitkumar.solarpulse.api.dto.SystemInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;

// Reports real, currently-configured values only: BuildProperties comes
// from Maven's build-info goal (pom.xml) and is only present once that
// goal has actually run (e.g. after `mvn package`) — injected via
// ObjectProvider so local `spring-boot:run`/tests still start cleanly
// without it, falling back to "dev". Uptime comes from the JVM's own
// runtime bean, and the resilience numbers are bound from the exact
// same application.yml properties Resilience4j itself uses — nothing
// here is a separately-maintained copy that could drift out of sync.
@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "Build/runtime info and the live resilience configuration")
public class SystemInfoController {

    private final ObjectProvider<BuildProperties> buildProperties;
    private final Environment environment;
    private final float failureRateThreshold;
    private final int slidingWindowSize;
    private final int minimumNumberOfCalls;
    private final String waitDurationInOpenState;

    public SystemInfoController(
        ObjectProvider<BuildProperties> buildProperties,
        Environment environment,
        @Value("${resilience4j.circuitbreaker.instances.openMeteo.failureRateThreshold}") float failureRateThreshold,
        @Value("${resilience4j.circuitbreaker.instances.openMeteo.slidingWindowSize}") int slidingWindowSize,
        @Value("${resilience4j.circuitbreaker.instances.openMeteo.minimumNumberOfCalls}") int minimumNumberOfCalls,
        @Value("${resilience4j.circuitbreaker.instances.openMeteo.waitDurationInOpenState}") String waitDurationInOpenState
    ) {
        this.buildProperties = buildProperties;
        this.environment = environment;
        this.failureRateThreshold = failureRateThreshold;
        this.slidingWindowSize = slidingWindowSize;
        this.minimumNumberOfCalls = minimumNumberOfCalls;
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    @Operation(summary = "App version/build time, JVM uptime, and the live Resilience4j configuration")
    @GetMapping("/info")
    public SystemInfoDto info() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        String[] profiles = environment.getActiveProfiles();
        String activeProfile = profiles.length > 0 ? profiles[0] : "default";

        String version = buildProperties.getIfAvailable() != null
            ? buildProperties.getIfAvailable().getVersion()
            : "dev";

        return new SystemInfoDto(
            version,
            activeProfile,
            uptimeSeconds,
            new SystemInfoDto.ResilienceConfig(
                failureRateThreshold, slidingWindowSize, minimumNumberOfCalls, waitDurationInOpenState)
        );
    }
}
