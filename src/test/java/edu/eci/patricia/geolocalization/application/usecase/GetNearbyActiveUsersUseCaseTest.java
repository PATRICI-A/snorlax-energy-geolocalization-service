package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
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
class GetNearbyActiveUsersUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @InjectMocks
    private GetNearbyActiveUsersUseCase useCase;

    @Test
    void shouldReturnActiveNearbyUsersSortedByDistance() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        Location near = new Location("2", "user-B", 4.6036, -74.0656, "Bloque B", 10.0, LocalDateTime.now());
        Location far = new Location("3", "user-C", 4.6100, -74.0700, "Bloque C", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearbyActive(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(List.of(far, near));

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers("ref-user", 500, true);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).distanceMeters()).isLessThan(result.get(1).distanceMeters());
    }

    @Test
    void shouldReturnAllNearbyUsersWhenSoloActivosFalse() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        Location other = new Location("2", "user-B", 4.6036, -74.0656, "Bloque B", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(other));

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers("ref-user", 500, false);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldExcludeRequestingUserFromResults() {
        Location refLoc = new Location("1", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        Location selfLoc = new Location("2", "ref-user", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("ref-user")).thenReturn(Optional.of(refLoc));
        when(locationRepository.findNearbyActive(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(List.of(refLoc, selfLoc));

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers("ref-user", 500, true);

        assertThat(result).noneMatch(u -> u.userId().equals("ref-user"));
    }

    @Test
    void shouldThrowInvalidRadiusWhenExceedsMax() {
        assertThatThrownBy(() -> useCase.getNearbyUsers("user", 6000, true))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void shouldThrowInvalidRadiusWhenZero() {
        assertThatThrownBy(() -> useCase.getNearbyUsers("user", 0, true))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void shouldThrowLocationNotFoundWhenUserHasNoLocation() {
        when(locationRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getNearbyUsers("unknown", 500, true))
                .isInstanceOf(LocationNotFoundException.class);
    }
}
