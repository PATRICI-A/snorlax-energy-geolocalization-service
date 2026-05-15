package edu.eci.patricia.geolocalization.domain.ports.out;

import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;

import java.util.Optional;

public interface PlaceGeocoderPort {
    Optional<Coordinates> geocode(String place);
}
