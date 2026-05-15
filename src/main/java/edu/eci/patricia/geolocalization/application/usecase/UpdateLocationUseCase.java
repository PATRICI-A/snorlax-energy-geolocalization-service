package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.GeoLocationDisabledException;
import edu.eci.patricia.geolocalization.domain.exceptions.OutOfCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.UserProfilePort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import edu.eci.patricia.geolocalization.infrastructure.config.CampusProperties;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateLocationUseCase implements UpdateLocationPort {

    private static final int STALE_THRESHOLD_SECONDS = 30;

    private final LocationRepositoryPort locationRepository;
    private final CampusZoneResolverPort campusZoneResolver;
    private final LocationEventPublisher eventPublisher;
    private final UserProfilePort userProfilePort;
    private final CampusProperties campusProperties;

    @Override
    public LocationResponseDto updateLocation(String userId, UpdateLocationRequestDto dto) {
        if (!userProfilePort.isGeoLocationEnabled(userId)) {
            throw new GeoLocationDisabledException(userId);
        }

        if (dto.timestamp() != null) {
            Instant threshold = Instant.now().minusSeconds(STALE_THRESHOLD_SECONDS);
            if (dto.timestamp().isBefore(threshold)) {
                throw new StaleTimestampException();
            }
        }

        Coordinates incoming = new Coordinates(dto.latitude(), dto.longitude());
        Coordinates campusCenter = new Coordinates(
                campusProperties.getCenterLat(), campusProperties.getCenterLng());
        if (incoming.distanceMetersTo(campusCenter) > campusProperties.getPerimeterRadiusMeters()) {
            throw new OutOfCampusException();
        }

        Location location = locationRepository.findByUserId(userId)
                .orElse(new Location(null, userId, dto.latitude(), dto.longitude(),
                        dto.campusZone(), dto.accuracy(), LocalDateTime.now()));

        location.updateCoordinates(dto.latitude(), dto.longitude(), dto.accuracy());

        String zone = campusZoneResolver.resolveZone(dto.latitude(), dto.longitude())
                .orElse(dto.campusZone());
        location.setCampusZone(zone);

        Location saved = locationRepository.save(location);
        eventPublisher.publishLocationUpdated(saved);

        return toResponse(saved);
    }

    private LocationResponseDto toResponse(Location loc) {
        return new LocationResponseDto(
                loc.getUserId(), loc.getLatitude(), loc.getLongitude(),
                loc.getCampusZone(), loc.getAccuracy(), loc.getUpdatedAt(),
                loc.isActive(), loc.isLowPrecision()
        );
    }
}
