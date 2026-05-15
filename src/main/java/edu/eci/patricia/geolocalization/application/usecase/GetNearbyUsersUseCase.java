package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNearbyUsersUseCase implements GetNearbyUsersPort {

    private static final double MAX_RADIUS_METERS = 5000;

    private final LocationRepositoryPort locationRepository;

    @Override
    public List<NearbyUserResponseDto> getNearbyUsers(double latitude, double longitude, double radiusMeters) {
        if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
            throw new InvalidRadiusException(
                    "Radius must be between 1 and " + MAX_RADIUS_METERS + " meters");
        }

        Coordinates origin = new Coordinates(latitude, longitude);
        List<Location> nearby = locationRepository.findNearby(latitude, longitude, radiusMeters);

        return nearby.stream()
                .map(loc -> {
                    Coordinates target = new Coordinates(loc.getLatitude(), loc.getLongitude());
                    double distance = origin.distanceMetersTo(target);
                    return new NearbyUserResponseDto(
                            loc.getUserId(), loc.getLatitude(), loc.getLongitude(),
                            loc.getCampusZone(), distance, loc.getUpdatedAt()
                    );
                })
                .sorted((a, b) -> Double.compare(a.distanceMeters(), b.distanceMeters()))
                .toList();
    }
}
