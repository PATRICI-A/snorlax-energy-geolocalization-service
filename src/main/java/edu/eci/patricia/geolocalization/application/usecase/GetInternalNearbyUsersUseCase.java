package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUserDto;
import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUsersResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetInternalNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetInternalNearbyUsersUseCase implements GetInternalNearbyUsersPort {

    private static final double MIN_RADIUS_METERS = 50;
    private static final double MAX_RADIUS_METERS = 500;
    private static final int ACTIVE_MINUTES = 5;

    private final LocationRepositoryPort locationRepository;

    @Override
    public InternalNearbyUsersResponseDto getNearbyUsers(String userId, double radius, boolean soloActivos) {
        if (radius < MIN_RADIUS_METERS || radius > MAX_RADIUS_METERS) {
            throw new InvalidRadiusException(
                    "Internal radius must be between " + (int) MIN_RADIUS_METERS
                    + " and " + (int) MAX_RADIUS_METERS + " meters");
        }

        Location reference = locationRepository.findByUserId(userId)
                .orElseThrow(() -> new LocationNotFoundException(userId));

        LocalDateTime activeSince = LocalDateTime.now().minusMinutes(ACTIVE_MINUTES);
        List<Location> nearby = locationRepository.findNearbyActiveSharing(
                reference.getLatitude(), reference.getLongitude(), radius, activeSince);

        Coordinates origin = new Coordinates(reference.getLatitude(), reference.getLongitude());

        List<InternalNearbyUserDto> usuarios = nearby.stream()
                .filter(loc -> !loc.getUserId().equals(userId))
                .map(loc -> {
                    double distance = origin.distanceMetersTo(
                            new Coordinates(loc.getLatitude(), loc.getLongitude()));
                    return new InternalNearbyUserDto(loc.getUserId(), distance, loc.getCampusZone());
                })
                .sorted((a, b) -> Double.compare(a.distanciaMetros(), b.distanciaMetros()))
                .toList();

        return new InternalNearbyUsersResponseDto(200, usuarios.size(), usuarios);
    }
}
