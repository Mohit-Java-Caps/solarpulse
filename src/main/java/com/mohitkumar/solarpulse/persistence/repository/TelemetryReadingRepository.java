package com.mohitkumar.solarpulse.persistence.repository;

import com.mohitkumar.solarpulse.persistence.entity.TelemetryReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReadingEntity, Long> {

    List<TelemetryReadingEntity> findBySiteIdOrderByObservedAtDesc(String siteId);

    List<TelemetryReadingEntity> findTop30BySiteIdOrderByObservedAtDesc(String siteId);

    boolean existsBySiteIdAndObservedAt(String siteId, Instant observedAt);

    long countBySiteId(String siteId);
}
