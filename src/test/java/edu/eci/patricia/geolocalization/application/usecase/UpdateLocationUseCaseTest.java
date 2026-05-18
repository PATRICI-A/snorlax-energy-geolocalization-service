package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationOutsideCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseTest {

    @Mock private LocationRepositoryPort locationRepository;
    @Mock private CampusZoneResolverPort campusZoneResolver;
    @Mock private LocationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateLocationUseCase useCase;

    // ECI campus coordinates
    private static final double LAT = 4.630;
    private static final double LON = -74.063;

    @Test
    void updateLocation_newUser_createsAndReturns() {
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(LAT, LON, 10.0, "Bloque A", null);
        Location saved = new Location("id1", "user-1", LAT, LON, "Bloque A", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(campusZoneResolver.resolveZone(LAT, LON)).thenReturn(Optional.of("Bloque de Ingeniería"));
        when(locationRepository.save(any())).thenReturn(saved);

        LocationResponseDto result = useCase.updateLocation("user-1", dto);

        assertThat(result.userId()).isEqualTo("user-1");
        verify(eventPublisher).publishLocationUpdated(saved);
    }

    @Test
    void updateLocation_existingUser_updatesAndReturns() {
        Location existing = new Location("id1", "user-1", LAT, LON, "Bloque A", 10.0, LocalDateTime.now());
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(LAT, LON, 12.0, "Cafetería", null);

        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(existing));
        when(campusZoneResolver.resolveZone(LAT, LON)).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(existing);

        LocationResponseDto result = useCase.updateLocation("user-1", dto);

        assertThat(result).isNotNull();
        verify(locationRepository).save(any());
    }

    @Test
    void updateLocation_googleMapsFails_fallsBackToClientZone() {
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(LAT, LON, 10.0, "Bloque Cliente", null);
        Location saved = new Location("id1", "user-1", LAT, LON, "Bloque Cliente", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(campusZoneResolver.resolveZone(LAT, LON)).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(saved);

        LocationResponseDto result = useCase.updateLocation("user-1", dto);
        assertThat(result.campusZone()).isEqualTo("Bloque Cliente");
    }

    @Test
    void updateLocation_staleTimestamp_throwsStaleTimestampException() {
        Instant stale = Instant.now().minusSeconds(60);
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(LAT, LON, 10.0, "Bloque A", stale);

        assertThatThrownBy(() -> useCase.updateLocation("user-1", dto))
                .isInstanceOf(StaleTimestampException.class);
    }

    @Test
    void updateLocation_freshTimestamp_passes() {
        Instant fresh = Instant.now().minusSeconds(5);
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(LAT, LON, 10.0, "Bloque A", fresh);
        Location saved = new Location("id1", "user-1", LAT, LON, "Bloque A", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(campusZoneResolver.resolveZone(LAT, LON)).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(saved);

        LocationResponseDto result = useCase.updateLocation("user-1", dto);
        assertThat(result).isNotNull();
    }

    @Test
    void updateLocation_coordinatesOutsideCampus_throwsLocationOutsideCampusException() {
        // New York coordinates
        UpdateLocationRequestDto dto = new UpdateLocationRequestDto(40.712, -74.006, 10.0, null, null);

        assertThatThrownBy(() -> useCase.updateLocation("user-1", dto))
                .isInstanceOf(LocationOutsideCampusException.class);
    }
}
