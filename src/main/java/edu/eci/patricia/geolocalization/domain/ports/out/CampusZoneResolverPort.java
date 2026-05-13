package edu.eci.patricia.geolocalization.domain.ports.out;

import java.util.Optional;

public interface CampusZoneResolverPort {
    Optional<String> resolveZone(double latitude, double longitude);
}
