package edu.eci.patricia.geolocalization.domain.ports.out;

import edu.eci.patricia.geolocalization.domain.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepositoryPort {
    Location save(Location location);
    Optional<Location> findByUserId(String userId);
    List<Location> findNearby(double latitude, double longitude, double radiusMeters);
    void deleteByUserId(String userId);
}
