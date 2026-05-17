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
    void getLocation_found_returnsDto() {
        Location location = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(location));

        LocationResponseDto result = useCase.getLocation("user-1");

        assertThat(result.userId()).isEqualTo("user-1");
        assertThat(result.latitude()).isEqualTo(4.628);
        assertThat(result.longitude()).isEqualTo(-74.064);
        assertThat(result.campusZone()).isEqualTo("Bloque A");
    }

    @Test
    void getLocation_notFound_throwsLocationNotFoundException() {
        when(locationRepository.findByUserId("user-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getLocation("user-x"))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("user-x");
    }
}
