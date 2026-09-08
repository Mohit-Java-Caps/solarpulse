package com.mohitkumar.solarpulse.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// A fixed, hand-curated catalog of demo sites — deliberately not
// admin-configurable in v1 (see plan: "configurable site catalog UI" is
// a documented v2 item, not a missing feature here).
@Entity
@Table(name = "sites")
public class SiteEntity {

    @Id
    private String id;

    private String name;
    private double latitude;
    private double longitude;

    @Column(name = "capacity_kw")
    private double capacityKw;

    private double tilt;
    private double azimuth;

    @Column(name = "derate_factor")
    private double derateFactor;

    protected SiteEntity() {
    }

    public SiteEntity(String id, String name, double latitude, double longitude,
                       double capacityKw, double tilt, double azimuth, double derateFactor) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacityKw = capacityKw;
        this.tilt = tilt;
        this.azimuth = azimuth;
        this.derateFactor = derateFactor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getCapacityKw() {
        return capacityKw;
    }

    public double getTilt() {
        return tilt;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getDerateFactor() {
        return derateFactor;
    }
}
