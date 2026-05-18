package edu.eci.patricia.geolocalization.domain.ports.out;

import edu.eci.patricia.geolocalization.domain.model.Location;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LocationRepositoryPort {
    Location save(Location location);
    Optional<Location> findByUserId(String userId);
    List<Location> findNearby(double latitude, double longitude, double radiusMeters);
    List<Location> findNearbyActive(double latitude, double longitude, double radiusMeters, LocalDateTime activeSince);
    List<Location> findNearbyActiveSharing(double latitude, double longitude, double radiusMeters, LocalDateTime activeSince);
    List<Location> findAllActive(LocalDateTime activeSince);
    void deleteByUserId(String userId);
}
