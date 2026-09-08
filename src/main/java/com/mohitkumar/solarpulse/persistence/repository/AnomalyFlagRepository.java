package com.mohitkumar.solarpulse.persistence.repository;

import com.mohitkumar.solarpulse.persistence.entity.AnomalyFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnomalyFlagRepository extends JpaRepository<AnomalyFlagEntity, Long> {

    List<AnomalyFlagEntity> findTop20BySiteIdOrderByFlaggedAtDesc(String siteId);

    List<AnomalyFlagEntity> findTop20ByOrderByFlaggedAtDesc();
}
