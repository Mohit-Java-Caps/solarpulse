package com.mohitkumar.solarpulse.config;

// Fixed panel/site configuration used both to seed SiteEntity rows and to
// drive the GenerationEstimatorService's per-site PV math. Deliberately a
// hardcoded catalog for v1 — see the plan's "configurable site catalog UI"
// v2 note.
public record SiteDefinition(
    String id,
    String name,
    double latitude,
    double longitude,
    double capacityKw,
    double tilt,
    double azimuth,
    double derateFactor
) {
}
