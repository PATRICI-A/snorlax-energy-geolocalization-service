package edu.eci.patricia.geolocalization.domain.exceptions;

public class LocationOutsideCampusException extends RuntimeException {
    public LocationOutsideCampusException(double latitude, double longitude) {
        super("Coordinates (" + latitude + ", " + longitude + ") are outside the campus perimeter");
    }
}
