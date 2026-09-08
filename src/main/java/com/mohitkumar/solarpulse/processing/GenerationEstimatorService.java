package com.mohitkumar.solarpulse.processing;

import com.mohitkumar.solarpulse.config.SiteDefinition;
import org.springframework.stereotype.Service;

// Converts real public irradiance/temperature readings into a simulated
// AC power output, using a simplified PV model in the same spirit as
// PVWatts' own simplified equations (not a claim of matching it exactly):
//
//   1. Normalize irradiance against STC (1000 W/m^2).
//   2. Estimate cell temperature from ambient temperature + irradiance
//      (a simplified NOCT-style delta — real modules run hotter than
//      ambient air under load, roughly proportional to irradiance).
//   3. Apply a temperature de-rating (crystalline silicon loses ~0.4%
//      output per degree above 25C).
//   4. Apply the site's derate factor (inverter/wiring/soiling losses).
//
// This is deliberately documented as simulated, not measured, generation
// — see README's "what's real vs. simulated" section.
@Service
public class GenerationEstimatorService {

    private static final double STC_IRRADIANCE_WM2 = 1000.0;
    private static final double CELL_TEMP_RISE_COEFFICIENT = 25.0;
    private static final double TEMP_COEFFICIENT_PER_C = -0.004;
    private static final double STC_CELL_TEMP_C = 25.0;

    public double estimatePowerKw(SiteDefinition site, double irradianceWm2, double ambientTempC) {
        double normalizedIrradiance = Math.max(irradianceWm2, 0) / STC_IRRADIANCE_WM2;
        double cellTempC = ambientTempC + CELL_TEMP_RISE_COEFFICIENT * normalizedIrradiance;
        double tempFactor = 1 + TEMP_COEFFICIENT_PER_C * (cellTempC - STC_CELL_TEMP_C);

        double dcPowerKw = site.capacityKw() * normalizedIrradiance * Math.max(tempFactor, 0);
        double acPowerKw = dcPowerKw * site.derateFactor();
        return Math.max(acPowerKw, 0);
    }
}
