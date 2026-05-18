package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.PlaceGeocoderPort;
import edu.eci.patricia.geolocalization.infrastructure.external.CampusEventsFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.ParcheFeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMapDataUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;
    @Mock
    private ParcheFeignClient parcheFeignClient;
    @Mock
    private CampusEventsFeignClient campusEventsFeignClient;
    @Mock
    private PlaceGeocoderPort placeGeocoder;

    @InjectMocks
    private GetMapDataUseCase useCase;

    @Test
    void shouldReturnUserPositionWhenMiPosicionRequested() {
        Location myLoc = new Location("1", "user-1", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(myLoc));
        when(parcheFeignClient.getActiveParches(any())).thenReturn(List.of());
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("mi_posicion", "parches", "eventos"), 500);

        assertThat(result.status()).isEqualTo(200);
        assertThat(result.usuario()).isNotNull();
        assertThat(result.usuario().lat()).isEqualTo(4.6035);
        assertThat(result.usuario().lng()).isEqualTo(-74.0655);
        assertThat(result.usuario().zona()).isEqualTo("Bloque A");
    }

    @Test
    void shouldReturnNullUsuarioWhenMiPosicionNotRequested() {
        when(parcheFeignClient.getActiveParches(any())).thenReturn(List.of());
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("parches", "eventos"), 500);

        assertThat(result.usuario()).isNull();
    }

    @Test
    void shouldReturnEmptyParchesWhenLayerNotRequested() {
        Location myLoc = new Location("1", "user-1", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(myLoc));
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("mi_posicion", "eventos"), 500);

        assertThat(result.parches()).isEmpty();
    }

    @Test
    void shouldGracefullyHandleParcheServiceFailure() {
        Location myLoc = new Location("1", "user-1", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(myLoc));
        when(parcheFeignClient.getActiveParches(any())).thenThrow(new RuntimeException("Service down"));
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("mi_posicion", "parches", "eventos"), 500);

        assertThat(result.parches()).isEmpty();
        assertThat(result.eventos()).isEmpty();
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void shouldGracefullyHandleEventServiceFailure() {
        Location myLoc = new Location("1", "user-1", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(myLoc));
        when(parcheFeignClient.getActiveParches(any())).thenReturn(List.of());
        when(campusEventsFeignClient.getActiveEvents()).thenThrow(new RuntimeException("Service down"));

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("mi_posicion", "parches", "eventos"), 500);

        assertThat(result.eventos()).isEmpty();
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void shouldReturnUserPositionAsNullWhenUserHasNoStoredLocation() {
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(parcheFeignClient.getActiveParches(any())).thenReturn(List.of());
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("mi_posicion", "parches", "eventos"), 500);

        assertThat(result.usuario()).isNull();
    }

    @Test
    void shouldAlwaysReturnEmptyZonas() {
        when(parcheFeignClient.getActiveParches(any())).thenReturn(List.of());
        when(campusEventsFeignClient.getActiveEvents()).thenReturn(List.of());

        MapDataResponseDto result = useCase.getMapData("user-1",
                List.of("parches", "eventos"), 500);

        assertThat(result.zonas()).isEmpty();
    }
}
