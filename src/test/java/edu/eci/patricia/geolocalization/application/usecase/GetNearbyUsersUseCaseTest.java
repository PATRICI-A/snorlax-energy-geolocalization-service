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
    void getNearbyUsers_validRadius_returnsSortedList() {
        Location close = new Location("1", "user-close", 4.629, -74.064, "Bloque A", null, LocalDateTime.now());
        Location far   = new Location("2", "user-far",   4.635, -74.064, "Bloque B", null, LocalDateTime.now());
        when(locationRepository.findNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(far, close));

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers(4.628, -74.064, 1000);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo("user-close");
        assertThat(result.get(1).userId()).isEqualTo("user-far");
        assertThat(result.get(0).distanceMeters()).isLessThan(result.get(1).distanceMeters());
    }

    @Test
    void getNearbyUsers_zeroRadius_throwsInvalidRadiusException() {
        assertThatThrownBy(() -> useCase.getNearbyUsers(4.628, -74.064, 0))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void getNearbyUsers_negativeRadius_throwsInvalidRadiusException() {
        assertThatThrownBy(() -> useCase.getNearbyUsers(4.628, -74.064, -1))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void getNearbyUsers_radiusExceedsMax_throwsInvalidRadiusException() {
        assertThatThrownBy(() -> useCase.getNearbyUsers(4.628, -74.064, 5001))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void getNearbyUsers_emptyResult_returnsEmptyList() {
        when(locationRepository.findNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        List<NearbyUserResponseDto> result = useCase.getNearbyUsers(4.628, -74.064, 500);
        assertThat(result).isEmpty();
    }
}
