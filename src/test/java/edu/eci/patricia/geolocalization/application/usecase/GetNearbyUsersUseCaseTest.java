package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNearbyUsersUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @InjectMocks
    private GetNearbyUsersUseCase useCase;

    @Test
    void shouldReturnNearbyUsersSortedByDistance() {
        Location loc1 = new Location("1", "user-A", 4.6036, -74.0656, "Bloque A", null, LocalDateTime.now());
        Location loc2 = new Location("2", "user-B", 4.6100, -74.0700, "Bloque C", null, LocalDateTime.now());
        when(locationRepository.findNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(loc2, loc1));

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers(4.6035, -74.0655, 500);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).distanceMeters())
                .isLessThan(result.get(1).distanceMeters());
    }

    @Test
    void shouldThrowInvalidRadiusWhenZero() {
        assertThatThrownBy(() -> useCase.getNearbyUsers(4.6035, -74.0655, 0))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void shouldThrowInvalidRadiusWhenExceedsMax() {
        assertThatThrownBy(() -> useCase.getNearbyUsers(4.6035, -74.0655, 6000))
                .isInstanceOf(InvalidRadiusException.class);
    }
}
