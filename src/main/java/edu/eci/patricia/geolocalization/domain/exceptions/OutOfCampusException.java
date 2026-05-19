package edu.eci.patricia.geolocalization.domain.exceptions;

public class OutOfCampusException extends RuntimeException {
    public OutOfCampusException() {
        super("Coordinates are outside the campus perimeter");
    }
}
