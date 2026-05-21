package edu.eci.patricia.geolocalization.domain.ports.out;

public interface UserProfilePort {
    boolean isGeoLocationEnabled(String userId);
}
