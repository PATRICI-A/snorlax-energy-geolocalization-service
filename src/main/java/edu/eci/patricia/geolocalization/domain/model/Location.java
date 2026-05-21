package edu.eci.patricia.geolocalization.domain.model;

import java.time.LocalDateTime;

public class Location {

    private String id;
    private String userId;
    private double latitude;
    private double longitude;
    private String campusZone;
    private Double accuracy;
    private LocalDateTime updatedAt;
    private boolean lowPrecision;
    private boolean sharingEnabled = true;

    public Location(String id, String userId, double latitude, double longitude,
                    String campusZone, Double accuracy, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.campusZone = campusZone;
        this.accuracy = accuracy;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.lowPrecision = accuracy == null || accuracy > 100.0;
    }

    public void updateCoordinates(double latitude, double longitude, Double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.updatedAt = LocalDateTime.now();
        this.lowPrecision = accuracy == null || accuracy > 100.0;
    }

    public boolean isActive() {
        return updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCampusZone() { return campusZone; }
    public Double getAccuracy() { return accuracy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isLowPrecision() { return lowPrecision; }

    public void setCampusZone(String campusZone) { this.campusZone = campusZone; }
    public void setId(String id) { this.id = id; }
    public void setLowPrecision(boolean lowPrecision) { this.lowPrecision = lowPrecision; }
    public boolean isSharingEnabled() { return sharingEnabled; }
    public void setSharingEnabled(boolean sharingEnabled) { this.sharingEnabled = sharingEnabled; }
}
