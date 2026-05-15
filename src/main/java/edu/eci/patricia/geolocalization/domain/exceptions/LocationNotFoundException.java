package edu.eci.patricia.geolocalization.domain.exceptions;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String userId) {
        super("No location found for user: " + userId);
    }
}
