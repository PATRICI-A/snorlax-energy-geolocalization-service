package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLocationUseCase implements GetLocationPort {

    private final LocationRepositoryPort locationRepository;

    @Override
    public LocationResponseDto getLocation(String userId) {
        Location location = locationRepository.findByUserId(userId)
                .orElseThrow(() -> new LocationNotFoundException(userId));

        return new LocationResponseDto(
                location.getUserId(), location.getLatitude(), location.getLongitude(),
                location.getCampusZone(), location.getAccuracy(), location.getUpdatedAt(),
                location.isActive(), location.isLowPrecision()
        );
    }
}
