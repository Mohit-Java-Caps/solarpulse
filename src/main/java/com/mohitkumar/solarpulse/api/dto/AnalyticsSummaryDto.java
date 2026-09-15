package com.mohitkumar.solarpulse.api.dto;

public record AnalyticsSummaryDto(
    double totalCapacityKw,
    double totalCurrentOutputKw,
    int siteCount,
    int activeSiteCount,
    int staleSiteCount,
    long anomaliesLast24h
) {
}
