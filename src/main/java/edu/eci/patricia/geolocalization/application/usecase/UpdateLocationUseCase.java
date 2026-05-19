package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationOutsideCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.infrastructure.external.GamificationClient;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLocationUseCase implements UpdateLocationPort {

    // ECI campus bounding box (Bogotá, Colombia)
    private static final double CAMPUS_LAT_MIN = 4.620;
    private static final double CAMPUS_LAT_MAX = 4.645;
    private static final double CAMPUS_LON_MIN = -74.075;
    private static final double CAMPUS_LON_MAX = -74.050;
    private static final long MAX_TIMESTAMP_AGE_SECONDS = 30;

    private final LocationRepositoryPort locationRepository;
    private final CampusZoneResolverPort campusZoneResolver;
    private final LocationEventPublisher eventPublisher;
    private final GamificationClient gamificationClient;

    @Override
    public LocationResponseDto updateLocation(String userId, UpdateLocationRequestDto dto) {
        validateTimestamp(dto.timestamp());
        validateCampusBoundary(dto.latitude(), dto.longitude());

        Location location = locationRepository.findByUserId(userId)
                .orElse(new Location(null, userId, dto.latitude(), dto.longitude(),
                        dto.campusZone(), dto.accuracy(), LocalDateTime.now()));

        location.updateCoordinates(dto.latitude(), dto.longitude(), dto.accuracy());

        // Auto-detect zone via Google Maps; fall back to client-provided value
        String zone = campusZoneResolver.resolveZone(dto.latitude(), dto.longitude())
                .orElse(dto.campusZone());
        location.setCampusZone(zone);

        Location saved = locationRepository.save(location);
        eventPublisher.publishLocationUpdated(saved);
        notifyGamification(saved.getCampusZone());

        return toResponse(saved);
    }

    private void notifyGamification(String campusZone) {
        if (campusZone == null || campusZone.isBlank()) return;
        try {
            gamificationClient.reportZoneVisited(Map.of("campusZone", campusZone));
        } catch (FeignException ex) {
            log.warn("[Geo] Gamification notification failed — campusZone={} error={}", campusZone, ex.getMessage());
        }
    }

    private void validateTimestamp(Instant timestamp) {
        if (timestamp == null) return;
        long ageSeconds = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
        if (ageSeconds > MAX_TIMESTAMP_AGE_SECONDS) {
            throw new StaleTimestampException();
        }
    }

    private void validateCampusBoundary(double latitude, double longitude) {
        if (latitude < CAMPUS_LAT_MIN || latitude > CAMPUS_LAT_MAX
                || longitude < CAMPUS_LON_MIN || longitude > CAMPUS_LON_MAX) {
            throw new LocationOutsideCampusException(latitude, longitude);
        }
    }

    private LocationResponseDto toResponse(Location loc) {
        return new LocationResponseDto(
                loc.getUserId(), loc.getLatitude(), loc.getLongitude(),
                loc.getCampusZone(), loc.getAccuracy(), loc.getUpdatedAt(),
                loc.isActive(), loc.isLowPrecision()
        );
    }
}
