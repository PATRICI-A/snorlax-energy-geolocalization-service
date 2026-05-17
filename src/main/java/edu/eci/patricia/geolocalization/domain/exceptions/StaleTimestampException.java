package edu.eci.patricia.geolocalization.domain.exceptions;

public class StaleTimestampException extends RuntimeException {
    public StaleTimestampException() {
        super("Location timestamp is older than 30 seconds and cannot be accepted");
    }
}
