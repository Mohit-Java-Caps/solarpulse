# SolarPulse

A live solar-telemetry pipeline that mirrors the shape of a real production system — event-driven ingestion, transformation, persistence, and API delivery, wrapped in explicit resilience patterns — built end-to-end from scratch on public data.

**Live demo:** _(deployed on Render — link added once live)_

## Why this exists

Most backend portfolio projects are either a CRUD app with auth, or a folder of pattern-demo code nobody can actually run. This one is a real, live, single Spring Boot service: it polls public solar-irradiance data for five real locations, turns that into a simulated generation estimate, flags statistical anomalies, and — the actual point — wraps its one genuinely unreliable dependency in a Resilience4j circuit breaker you can watch open and recover in real time, not just read about in a diagram.

## What's real vs. simulated

| Claim | Status |
|---|---|
| Irradiance, temperature, weather data | **Real** — live/historical from [Open-Meteo](https://open-meteo.com), no API key required |
| Generation output (kW) | **Simulated** — derived from real irradiance/temperature via a simplified PV model (see `GenerationEstimatorService`), not measured panel output |
| Circuit breaker / retry / degradation | **Real** — actual [Resilience4j](https://resilience4j.readme.io/) behavior against a real external HTTP dependency |
| "Inject failure" demo control | **Real mechanism, deliberately triggered** — calls Resilience4j's own `CircuitBreakerRegistry` API directly, so the demo doesn't depend on Open-Meteo actually being down when someone clicks it |
| Anomaly detection | **Real math, simple on purpose** — rolling z-score against a per-site baseline; explicitly not machine learning (see below) |
| "Microservices architecture" | **Not claimed** — this is one deliberately modular Spring Boot service, not a multi-service saga; see `docs/ARCHITECTURE.md` for why |

## How it works

```
Scheduled poller (every few minutes, per site)
     │
     ▼
OpenMeteoClient  ──@CircuitBreaker + @Retry──▶  api.open-meteo.com
     │  success                                        │ failure (real or injected)
     ▼                                                  ▼
GenerationEstimatorService                    FallbackCacheService
 (irradiance+temp → simulated kW)              (last-known-good, tagged stale)
     │                                                  │
     └──────────────────┬───────────────────────────────┘
                         ▼
              TelemetryReadingEntity persisted (PostgreSQL / Neon)
                         │
                         ▼
              AnomalyDetectionService (rolling z-score vs. baseline)
                         │
                         ▼
        REST API (/api/dashboard, /api/sites/...) + Swagger UI
                         │
                         ▼
              Static dashboard (src/main/resources/static/index.html)
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full package structure and the reasoning behind each decision (why one service instead of a saga, why Open-Meteo, why Render+Neon instead of AWS for hosting this particular demo).

### Why anomaly detection stays simple

A rolling z-score against a per-site mean/stddev, nothing more. This project already has two AI-flavored siblings (a GenAI calorie tracker and a RAG chatbot) — its job is to prove backend/distributed-systems engineering, so anomaly detection here is deliberately rule-based statistics, not a model reaching for relevance it doesn't need.

## Tech stack

- **Backend:** Java 17, Spring Boot 3.3 (Web, Data JPA, Actuator, Validation)
- **Resilience:** Resilience4j (Circuit Breaker + Retry), configured in `application.yml`
- **Data:** PostgreSQL (Neon, free tier) in production; H2 in-memory for local dev/tests
- **Docs:** springdoc-openapi (Swagger UI at `/swagger-ui.html`)
- **Observability:** Spring Boot Actuator + Micrometer (Prometheus-format metrics at `/actuator/prometheus`)
- **Frontend:** a single static HTML/JS dashboard, no build step — served directly by the same Spring Boot app
- **Hosting:** Render (web service, Docker) + Neon (serverless Postgres)

## Running locally

Requires Java 17 and Maven.

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` for the dashboard, `http://localhost:8080/swagger-ui.html` for the API, `http://localhost:8080/actuator/health` for the circuit-breaker's own health indicator. Uses an in-memory H2 database by default — no setup needed.

### Troubleshooting: `PKIX path building failed` on a corporate network

If outbound HTTPS is intercepted by a corporate proxy (e.g. Zscaler) that re-signs certificates with its own root CA, the JVM's default truststore won't trust it even though your browser does — Open-Meteo calls will fail with `unable to find valid certification path to requested target`, and the circuit breaker will (correctly) open in response to that real failure. Fix by importing your organization's root CA into the JDK's truststore:

```bash
keytool -importcert -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -alias corp-root -file corp-root.cer -noprompt
```

This isn't a code bug — it's the same class of issue as configuring git to use the OS certificate store on a managed machine, and it doesn't affect the deployed version, since Render's servers aren't behind your corporate proxy.

## Deploying

1. Push this repo to GitHub.
2. Create a free [Neon](https://neon.tech) Postgres project; copy its connection details.
3. Create a free [Render](https://render.com) web service from this repo — it detects the `Dockerfile` automatically.
4. On Render, set environment variables: `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (from Neon).
5. Deploy. First request after idle may take up to ~60s (both Render's free tier and Neon's compute scale to zero when idle) — this is a real, honest characteristic of the free-tier demo, not a bug.

## Project structure

```
src/main/java/com/mohitkumar/solarpulse/
├── config/        Site catalog, startup seeder, HTTP client + OpenAPI config
├── ingestion/     OpenMeteoClient (the resilience-wrapped external call), scheduler, failure-injection endpoint
├── resilience/    Fallback cache, circuit-breaker status service
├── processing/    Generation estimator, ingest orchestration
├── analytics/     Rolling-baseline anomaly detection
├── persistence/   JPA entities + repositories
└── api/           REST controllers + DTOs
src/main/resources/static/index.html   The live dashboard (no build step)
src/test/java/...                       Resilience4j circuit-breaker/retry tests (MockRestServiceServer)
docs/ARCHITECTURE.md                    Full flow diagram + decision rationale
```

## Known limitations (v2, not built)

- Multi-source failover (e.g. NREL PVWatts as a second source) — see `docs/ARCHITECTURE.md`.
- WebSocket/SSE push instead of dashboard polling.
- Admin auth on the failure-injection endpoints (currently open — this is a demo, not a production admin surface).
- Configurable site catalog (currently five hardcoded locations).
- Grafana dashboard on top of the Prometheus-format metrics already exported.

## License

MIT
