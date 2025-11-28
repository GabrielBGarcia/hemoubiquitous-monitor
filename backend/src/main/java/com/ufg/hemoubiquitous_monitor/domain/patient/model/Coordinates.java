package com.ufg.hemoubiquitous_monitor.domain.patient.model;

public class Coordinates {
    private final Double latitude;
    private final Double longitude;

    public Coordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isValid() {
        return latitude != null && longitude != null;
    }
}
