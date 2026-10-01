package com.mohitkumar.solarpulse.config;

import java.util.List;

// Real locations chosen for genuine irradiance diversity, with two picked
// for personal resonance: Miami mirrors the Florida/NextEra Energy domain
// of the real flagship work project, and Pune/Kolkata are where the
// trainee/associate roles (see the Journey chapter) are based. Everything
// else about each reading is still derived from live public data — this
// only picks *where*. Tilt is roughly each site's own latitude (a common
// simplification for a fixed-tilt array), azimuth 180 (south-facing) since
// every site here is in the Northern Hemisphere.
//
// Ten sites, not more: Open-Meteo's free tier (~10k requests/day) handles
// this comfortably, and it's enough to read as a genuine fleet on the
// dashboard without the per-site frontend polling load growing past what
// a demo actually needs.
public final class SiteCatalog {

    public static final List<SiteDefinition> SITES = List.of(
        new SiteDefinition("phoenix-az", "Phoenix, AZ", 33.4484, -112.0740, 500, 20, 180, 0.84),
        new SiteDefinition("miami-fl", "Miami, FL", 25.7617, -80.1918, 500, 15, 180, 0.84),
        new SiteDefinition("san-diego-ca", "San Diego, CA", 32.7157, -117.1611, 500, 20, 180, 0.84),
        new SiteDefinition("austin-tx", "Austin, TX", 30.2672, -97.7431, 500, 20, 180, 0.84),
        new SiteDefinition("pune-in", "Pune, India", 18.5204, 73.8567, 500, 15, 180, 0.84),
        new SiteDefinition("kolkata-in", "Kolkata, India", 22.5726, 88.3639, 450, 20, 180, 0.84),
        new SiteDefinition("las-vegas-nv", "Las Vegas, NV", 36.1699, -115.1398, 650, 35, 180, 0.84),
        new SiteDefinition("denver-co", "Denver, CO", 39.7392, -104.9903, 550, 40, 180, 0.84),
        new SiteDefinition("berlin-de", "Berlin, Germany", 52.5200, 13.4050, 350, 50, 180, 0.84),
        new SiteDefinition("dubai-ae", "Dubai, UAE", 25.2048, 55.2708, 700, 20, 180, 0.84)
    );

    private SiteCatalog() {
    }
}
