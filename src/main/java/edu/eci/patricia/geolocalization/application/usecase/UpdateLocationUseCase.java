package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateLocationUseCase implements UpdateLocationPort {

    private final LocationRepositoryPort locationRepository;
    private final CampusZoneResolverPort campusZoneResolver;
    private final LocationEventPublisher eventPublisher;

    @Override
    public LocationResponseDto updateLocation(String userId, UpdateLocationRequestDto dto) {
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

        return toResponse(saved);
    }

    private LocationResponseDto toResponse(Location loc) {
        return new LocationResponseDto(
                loc.getUserId(), loc.getLatitude(), loc.getLongitude(),
                loc.getCampusZone(), loc.getAccuracy(), loc.getUpdatedAt()
        );
    }
}
