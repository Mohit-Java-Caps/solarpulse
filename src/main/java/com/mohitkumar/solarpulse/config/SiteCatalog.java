package com.mohitkumar.solarpulse.config;

import java.util.List;

// Real locations chosen for genuine irradiance diversity, with two picked
// for personal resonance: Miami mirrors the Florida/NextEra Energy domain
// of the real flagship work project, and Pune is where the trainee role
// (see the Journey chapter) started. Everything else about each reading is
// still derived from live public data — this only picks *where*.
public final class SiteCatalog {

    public static final List<SiteDefinition> SITES = List.of(
        new SiteDefinition("phoenix-az", "Phoenix, AZ", 33.4484, -112.0740, 500, 20, 180, 0.84),
        new SiteDefinition("miami-fl", "Miami, FL", 25.7617, -80.1918, 500, 15, 180, 0.84),
        new SiteDefinition("san-diego-ca", "San Diego, CA", 32.7157, -117.1611, 500, 20, 180, 0.84),
        new SiteDefinition("austin-tx", "Austin, TX", 30.2672, -97.7431, 500, 20, 180, 0.84),
        new SiteDefinition("pune-in", "Pune, India", 18.5204, 73.8567, 500, 15, 180, 0.84)
    );

    private SiteCatalog() {
    }
}
