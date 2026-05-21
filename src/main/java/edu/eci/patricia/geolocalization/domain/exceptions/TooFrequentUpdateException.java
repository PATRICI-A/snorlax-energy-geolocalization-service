package edu.eci.patricia.geolocalization.domain.exceptions;

public class TooFrequentUpdateException extends RuntimeException {
    public TooFrequentUpdateException(long waitSeconds) {
        super("Location updated too recently. Please wait " + waitSeconds + " more second(s).");
    }
}
