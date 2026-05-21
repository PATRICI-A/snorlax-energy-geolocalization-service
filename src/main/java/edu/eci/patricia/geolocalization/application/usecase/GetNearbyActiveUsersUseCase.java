package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyActiveUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNearbyActiveUsersUseCase implements GetNearbyActiveUsersPort {

    private static final double MAX_RADIUS_METERS = 5000;
    private static final int ACTIVE_MINUTES = 5;

    private final LocationRepositoryPort locationRepository;

    @Override
    public List<NearbyUserResponseDto> getNearbyUsers(String userId, double radiusMeters, boolean soloActivos) {
        if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
            throw new InvalidRadiusException("Radius must be between 1 and " + MAX_RADIUS_METERS + " meters");
        }

        Location userLocation = locationRepository.findByUserId(userId)
                .orElseThrow(() -> new LocationNotFoundException(userId));

        Coordinates origin = new Coordinates(userLocation.getLatitude(), userLocation.getLongitude());
        List<Location> nearby;

        if (soloActivos) {
            LocalDateTime activeSince = LocalDateTime.now().minusMinutes(ACTIVE_MINUTES);
            nearby = locationRepository.findNearbyActive(
                    userLocation.getLatitude(), userLocation.getLongitude(), radiusMeters, activeSince);
        } else {
            nearby = locationRepository.findNearby(
                    userLocation.getLatitude(), userLocation.getLongitude(), radiusMeters);
        }

        return nearby.stream()
                .filter(loc -> !loc.getUserId().equals(userId))
                .map(loc -> {
                    Coordinates target = new Coordinates(loc.getLatitude(), loc.getLongitude());
                    double distance = origin.distanceMetersTo(target);
                    return new NearbyUserResponseDto(
                            loc.getUserId(), loc.getLatitude(), loc.getLongitude(),
                            loc.getCampusZone(), distance, loc.getUpdatedAt(),
                            loc.isActive(), loc.isLowPrecision()
                    );
                })
                .sorted((a, b) -> Double.compare(a.distanceMeters(), b.distanceMeters()))
                .toList();
    }
}
