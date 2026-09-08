package com.mohitkumar.solarpulse.api.dto;

import java.util.List;

// Aggregates everything the single-page dashboard needs into one call —
// site list with latest reading, circuit-breaker status, recent anomalies
// across all sites — so the demo UI doesn't have to make N+2 requests
// just to render its first paint.
public record DashboardResponseDto(
    List<SiteSummaryDto> sites,
    StatusDto status,
    List<AnomalyDto> recentAnomalies
) {
}
