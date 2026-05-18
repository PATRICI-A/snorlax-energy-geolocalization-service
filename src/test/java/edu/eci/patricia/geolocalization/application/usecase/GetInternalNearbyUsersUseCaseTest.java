package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUsersResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInternalNearbyUsersUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @InjectMocks
    private GetInternalNearbyUsersUseCase useCase;

    @Test
    void shouldReturnNearbyUsersSortedByDistance() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        Location near = new Location("2", "user-A", 4.6036, -74.0656, "Bloque A", 10.0, LocalDateTime.now());
        Location far = new Location("3", "user-B", 4.6100, -74.0700, "Bloque C", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearbyActiveSharing(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(List.of(far, near));

        InternalNearbyUsersResponseDto result = useCase.getNearbyUsers("ref-user", 200, true);

        assertThat(result.status()).isEqualTo(200);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.usuarios()).hasSize(2);
        assertThat(result.usuarios().get(0).distanciaMetros())
                .isLessThan(result.usuarios().get(1).distanciaMetros());
    }

    @Test
    void shouldNeverExposeLatLng() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        Location other = new Location("2", "user-A", 4.6036, -74.0656, "Bloque B", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearbyActiveSharing(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(List.of(other));

        InternalNearbyUsersResponseDto result = useCase.getNearbyUsers("ref-user", 200, true);

        // InternalNearbyUserDto only has userId, distanciaMetros, zona — no lat/lng fields
        assertThat(result.usuarios().get(0).userId()).isEqualTo("user-A");
        assertThat(result.usuarios().get(0).zona()).isEqualTo("Bloque B");
    }

    @Test
    void shouldExcludeRequestingUserFromResults() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearbyActiveSharing(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(List.of(refLoc));

        InternalNearbyUsersResponseDto result = useCase.getNearbyUsers("ref-user", 200, true);

        assertThat(result.total()).isEqualTo(0);
        assertThat(result.usuarios()).isEmpty();
    }

    @Test
    void shouldThrowInvalidRadiusWhenBelowMin() {
        assertThatThrownBy(() -> useCase.getNearbyUsers("user", 30, true))
                .isInstanceOf(InvalidRadiusException.class)
                .hasMessageContaining("50");
    }

    @Test
    void shouldThrowInvalidRadiusWhenAboveMax() {
        assertThatThrownBy(() -> useCase.getNearbyUsers("user", 600, true))
                .isInstanceOf(InvalidRadiusException.class)
                .hasMessageContaining("500");
    }

    @Test
    void shouldThrowLocationNotFoundWhenUserHasNoLocation() {
        when(locationRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getNearbyUsers("unknown", 200, true))
                .isInstanceOf(LocationNotFoundException.class);
    }
}
