package com.mohitkumar.solarpulse.persistence.repository;

import com.mohitkumar.solarpulse.persistence.entity.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<SiteEntity, String> {
}
