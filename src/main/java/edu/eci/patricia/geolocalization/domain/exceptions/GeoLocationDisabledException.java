package edu.eci.patricia.geolocalization.domain.exceptions;

public class GeoLocationDisabledException extends RuntimeException {
    public GeoLocationDisabledException(String userId) {
        super("Geolocation is disabled for user: " + userId);
    }
}
