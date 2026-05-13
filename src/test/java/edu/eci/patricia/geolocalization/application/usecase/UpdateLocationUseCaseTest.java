package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @Mock
    private LocationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateLocationUseCase useCase;

    private UpdateLocationRequestDto dto;
    private Location savedLocation;

    @BeforeEach
    void setUp() {
        dto = new UpdateLocationRequestDto(4.6035, -74.0655, 10.0, "Bloque B");
        savedLocation = new Location("loc-1", "user-123", 4.6035, -74.0655, "Bloque B", 10.0, LocalDateTime.now());
    }

    @Test
    void shouldCreateNewLocationWhenNoneExists() {
        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(savedLocation);
        doNothing().when(eventPublisher).publishLocationUpdated(any());

        LocationResponseDto result = useCase.updateLocation("user-123", dto);

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.latitude()).isEqualTo(4.6035);
        assertThat(result.longitude()).isEqualTo(-74.0655);
        assertThat(result.campusZone()).isEqualTo("Bloque B");
        verify(locationRepository).save(any());
        verify(eventPublisher).publishLocationUpdated(any());
    }

    @Test
    void shouldUpdateExistingLocation() {
        Location existing = new Location("loc-1", "user-123", 4.60, -74.06, "Bloque A", 5.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.of(existing));
        when(locationRepository.save(any())).thenReturn(savedLocation);
        doNothing().when(eventPublisher).publishLocationUpdated(any());

        LocationResponseDto result = useCase.updateLocation("user-123", dto);

        assertThat(result.latitude()).isEqualTo(4.6035);
        assertThat(result.campusZone()).isEqualTo("Bloque B");
        verify(locationRepository).save(any());
    }
}
