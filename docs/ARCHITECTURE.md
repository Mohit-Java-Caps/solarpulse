# Architecture

## Package structure

```
com.mohitkumar.solarpulse
├── config/
│   ├── SiteDefinition, SiteCatalog     Fixed catalog of 5 demo sites (lat/lon/panel config)
│   ├── DataSeeder                       Startup ApplicationRunner: seeds sites + historical backfill
│   ├── HttpClientConfig                 RestClient bean with explicit connect/read timeouts
│   └── OpenApiConfig                    Swagger metadata
├── ingestion/
│   ├── OpenMeteoClient                  The one real external dependency — @CircuitBreaker + @Retry
│   ├── WeatherReading, OpenMeteoUnavailableException
│   ├── WeatherPollingScheduler          @Scheduled poll loop, one cycle per site
│   └── FailureInjectionController       Demo-only: forces the circuit breaker open/closed directly
├── resilience/
│   ├── FallbackCacheService             In-memory last-known-good reading per site
│   └── CircuitBreakerStatusService      Reads CircuitBreakerRegistry state for the API/dashboard
├── processing/
│   ├── GenerationEstimatorService       Irradiance+temp → simulated kW (simplified PV model)
│   └── TelemetryIngestService           Orchestrates fetch → estimate → persist → anomaly-check
├── analytics/
│   ├── BaselineStatsService             Rolling mean/stddev over the last 30 readings per site
│   └── AnomalyDetectionService          Rolling z-score flagging (rule-based, not ML)
├── persistence/
│   ├── entity/                          SiteEntity, TelemetryReadingEntity, AnomalyFlagEntity
│   └── repository/                      Spring Data JPA repositories
└── api/
    ├── SiteController                   /api/sites, /api/sites/{id}/readings, /api/sites/{id}/anomalies
    ├── DashboardController              /api/dashboard — aggregated view for the demo UI
    └── dto/                             Response DTOs (entities never leak past the service layer)
```

## Request flow

```
WeatherPollingScheduler (every openmeteo.poll-interval-ms, per site)
     │
     ▼
TelemetryIngestService.ingestLive(site)
     │
     ├─▶ BaselineStatsService.computeBaseline(siteId)   (from EXISTING readings, before this cycle's)
     │
     ▼
OpenMeteoClient.fetchCurrent(site)
     │  @CircuitBreaker(name="openMeteo") + @Retry(name="openMeteo")
     │
     ├─ success ──▶ GenerationEstimatorService.estimatePowerKw(...)
     │                   │
     │                   ▼
     │              persist TelemetryReadingEntity (stale=false, source="open-meteo-live")
     │                   │
     │                   ▼
     │              FallbackCacheService.record(siteId, reading)
     │
     └─ failure (real, or circuit OPEN) ──▶ fetchCurrentFallback throws OpenMeteoUnavailableException
                          │
                          ▼
                 FallbackCacheService.getLastGood(siteId)
                          │
                          ▼
                 persist a cloned reading (stale=true, source="fallback-cache")
                          │
                          ▼
              (either path) AnomalyDetectionService.evaluate(reading, baseline)
```

The `FailureInjectionController`'s `/api/admin/inject-failure` endpoint doesn't go through this flow at all — it calls `CircuitBreakerRegistry.circuitBreaker("openMeteo").transitionToOpenState()` directly, using Resilience4j's own registry API. This is deliberate: a live demo shouldn't depend on Open-Meteo actually being down when someone wants to see the degradation behavior, and using the library's real state-transition API (rather than, say, making `OpenMeteoClient` throw on a hidden flag) demonstrates the same mechanism a real operator would use to manually intervene.

## Why one Spring Boot service, not a multi-service saga

A genuinely distributed version of this (separate Order/Payment/Inventory-style services talking over SQS/SNS) was the first idea. It was deliberately scaled back to a single, internally-modular service for one reason: a live portfolio demo that a recruiter can hit at any time needs to actually stay up, and every additional independently-deployed service is another thing that can silently go to sleep, drift in config, or need its own IAM permissions. The architecture story (event-driven ingestion, resilience patterns, clear module boundaries) is still real and credible at this scope — it's just honest about being one deployable, not a claim of "microservices" it doesn't need.

## Why Open-Meteo instead of NREL PVWatts or a paid weather API

- No API key, no signup, ~10,000 requests/day free — genuinely zero-friction, which matters for "clone and run."
- Actively maintained, with both a live/forecast endpoint and a historical archive endpoint on the same API shape.
- NREL PVWatts was evaluated as a secondary/cross-check source (v2, not built) — its rate limits and TMY-based dataset make it less suited to a live polling loop, and its exact API surface should be re-verified directly against `developer.nrel.gov` before ever wiring it in, rather than trusted from a secondary source.

The forecast call requests `global_tilted_irradiance` (using each site's own tilt/azimuth) — the closest public proxy to "what this specific panel array receives." The historical archive call deliberately does **not** request that field: Open-Meteo's tilt/azimuth transposition is documented against the forecast model, and its exact support on the archive endpoint wasn't independently verified, so backfill uses `shortwave_radiation` (horizontal GHI) as a documented approximation instead of assuming untested behavior. `GenerationEstimatorService` accepts either.

## Why Render + Neon instead of AWS for hosting this demo

AWS would reinforce the resume's AWS claims more directly, but for a single demo service the operational risk (correct IAM setup, free-tier expiry after 12 months, surprise billing if misconfigured) outweighs that benefit here. Render's free web service (Docker-based) plus Neon's free Postgres (data persists indefinitely; only compute auto-suspends when idle) gets the same "genuinely live, genuinely free" outcome with far less setup surface. Render's own free Postgres was ruled out — it's hard-deleted 30 days after creation, which is a trap for a portfolio artifact meant to last.

## A real failure hit while building this — kept in, not smoothed over

The first version of `OpenMeteoClient`'s `HttpClientConfig` worked fine in production but failed every call when tested from behind a corporate proxy doing TLS interception, with `PKIX path building failed`. That's not a bug in this code — `curl` succeeds because `-k` skips certificate validation entirely, while the JVM's default truststore correctly refuses to trust an unrecognized proxy CA. It's the same category of issue as needing to point git at the OS certificate store on a managed machine. Worth knowing before assuming a "connection failed" error during local testing means the code is wrong — see the README's troubleshooting section.

## Known limitations (stated deliberately, not hidden)

- **Anomaly detection is rule-based statistics (rolling z-score), not machine learning.** Deliberate — see README.
- **The site catalog is a fixed list of five**, not admin-configurable. A v2 item, not a missing feature at this scope.
- **No conversation/session state or auth on the demo-only admin endpoints.** This is a portfolio demo, not a production admin surface — documented, not hidden.
- **Generation figures are simulated**, derived from real irradiance/temperature via a simplified PV model, not measured panel output. Labeled as such everywhere it's shown.
