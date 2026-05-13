package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLocationUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @InjectMocks
    private GetLocationUseCase useCase;

    @Test
    void shouldReturnLocationWhenExists() {
        Location location = new Location("loc-1", "user-123", 4.6035, -74.0655, "Bloque B", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.of(location));

        LocationResponseDto result = useCase.getLocation("user-123");

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.latitude()).isEqualTo(4.6035);
        assertThat(result.longitude()).isEqualTo(-74.0655);
    }

    @Test
    void shouldThrowLocationNotFoundWhenMissing() {
        when(locationRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getLocation("unknown"))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
