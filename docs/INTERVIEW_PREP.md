# Talking about SolarPulse in an interview

## The 30-second version

"SolarPulse is a live Spring Boot application that polls real public solar-irradiance data for ten sites worldwide, turns that into an estimated power output, and — the actual point of the project — wraps its one external dependency in a Resilience4j circuit breaker that you can watch open and recover in real time. It's not a diagram of resilience patterns, it's a running system you can break on purpose and watch heal itself."

Say that. Then let them ask follow-ups — the rest of this doc is your ammunition.

## Explain it to someone non-technical

A company with solar panel farms in ten cities needs each farm to report "how much power am I making right now?" To know that, the system asks a weather service "how sunny is it there?" every few minutes. Two real problems follow:

1. That weather service is someone else's system — it can go down or get slow, and that's out of your control.
2. When it does, you don't want the whole dashboard to go blank. You want it to keep showing the *last good number*, clearly marked as a bit old, while it quietly keeps checking in the background — and the moment the dependency is healthy again, it should recover on its own, no one needing to restart anything.

That "protect yourself from a flaky dependency and heal without a human" pattern is a **circuit breaker** — same idea as the electrical kind. There's a button on the dashboard that forces the weather connection to fail on purpose, and you can watch the whole cycle happen live: a red "OPEN" badge, numbers switch to "last known good," and it quietly recovers about 30 seconds later.

## Walk-through, in your own words

1. **The problem.** A portfolio can show a resilience-pattern diagram, but not what happens when the one real dependency actually fails. I wanted something live where that failure — and the recovery — is something you watch happen, not read about.
2. **The flow.** A scheduler polls Open-Meteo every 5 minutes per site. The call is wrapped in `@CircuitBreaker` + `@Retry`. On success, a simplified PV model turns irradiance + temperature into an estimated kW output, which gets persisted and cached as "last known good." On failure — real or the demo button — the circuit breaker's fallback serves that cached reading instead, tagged stale, so the system keeps answering rather than failing outright.
3. **Why a circuit breaker at all?** Without one, a slow or failing dependency would make every request to my own API slow too, and I'd keep hammering a service that's already struggling. The breaker stops that: after enough failures it trips open immediately, and only probes occasionally to see if it's safe to close again.
4. **The anomaly detection.** A rolling z-score against each site's own 30-reading baseline — deliberately rule-based statistics, not machine learning, because I already have two AI-flavored projects elsewhere and this one's job is proving backend engineering.
5. **The frontend.** React + Vite + Tailwind + Recharts, but still deployed as one container — the frontend build is just another stage in the same Docker build, and Spring Boot serves the final static assets directly. Upgrading the UI never meant adding a second thing to keep alive.

## Likely interviewer questions, and how to answer them

**"Why not real microservices?"**
> "I considered a genuinely distributed version — separate services talking over a queue. I scaled it back deliberately: a portfolio demo that has to stay up for a recruiter at any time shouldn't have more independently-deployed pieces than it needs, each one a new way to silently go to sleep or drift in config. The module boundaries inside the code are still real — ingestion, resilience, processing, analytics, API are cleanly separated — I just didn't inflate the deployment topology to match."

**"Why Open-Meteo instead of a paid weather API?"**
> "No API key, no signup, a generous free tier — genuinely zero-friction for something meant to be cloned and run by anyone. It also exposes both a live/forecast endpoint and a historical archive endpoint in the same shape, which is what lets the same client code both backfill history on startup and poll live going forward."

**"Why Render + Neon instead of AWS, given your resume is AWS-heavy?"**
> "AWS would reinforce the resume story more directly, but for a single demo service the operational risk — correct IAM, free-tier expiry, surprise billing if misconfigured — outweighs that benefit here. Render plus Neon gets the same 'genuinely live, genuinely free' outcome with far less setup surface to get wrong."

**"Why rebuild the dashboard in React instead of keeping the simple static page?"**
> "The original was one static HTML file, deliberately simple. When I wanted a real enterprise-looking dashboard with live charts, I rebuilt it in React but kept the one-deployable principle — the frontend build is just another Docker stage, and the same container still serves everything. More build pipeline, same deployment footprint."

**"How do you prevent the anomaly detection from flagging its own spike?"**
> "The baseline is computed from existing readings *before* the new one is persisted, so a genuine spike can't widen its own comparison window and hide itself."

**"What would you change if this had to scale further?"**
> "Multi-source failover — a second weather provider as a fallback before resorting to cached data — and WebSocket/SSE push instead of dashboard polling. Both are documented as deliberate v2 items, not gaps I didn't notice."

**"Tell me about a real bug you hit."**
> Pick one of the three below — they're real, and showing how you diagnosed them (not just that you fixed them) is the actual signal an interviewer is looking for.

## Real bugs hit while building this (don't smooth these over — they're good material)

1. **`PKIX path building failed`** testing locally behind a corporate proxy doing TLS interception — the JVM's truststore didn't trust the proxy's certificate even though the browser did. Not a code bug: `curl -k` skips validation entirely, the JVM correctly doesn't. Fixed by importing the corporate root CA into the JDK's truststore. Same category of issue as pointing git at the OS certificate store on a managed machine.
2. **Wrong database URL format on first deploy** — Neon's connection string embeds `user:password@` in the URL, but the JDBC driver needs them as separate properties. The actual fix came from reading the stack trace closely (`PGPropertyUtil` complaining about an "invalid port number") rather than guessing.
3. **Verifying static-resource serving across a multi-stage Docker build** — before trusting a full Docker build, I ran the packaged jar directly with `--spring.web.resources.static-locations` pointed at the frontend's build output, to confirm Spring Boot would actually serve it from outside the JAR's classpath. Caught the mechanism working before spending a full Docker build cycle on it.

## What you should NOT claim

- Don't say this is "microservices" — it's one deliberately modular service. Say exactly that if pushed.
- Don't imply the circuit breaker only ever triggers on real outages — the demo button forces it directly via Resilience4j's own registry API. That's the more impressive detail, not something to hide.
- Don't call the anomaly detection "AI" — it's rolling statistics, on purpose.
- Generation figures are a simulated estimate derived from real irradiance data, not measured panel output. Say that plainly if asked.
