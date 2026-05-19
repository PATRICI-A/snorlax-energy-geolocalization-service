package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyParchesResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.ExternalServiceException;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.PlaceGeocoderPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import edu.eci.patricia.geolocalization.infrastructure.external.ParcheFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.ParceDto;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNearbyParchesUseCaseTest {

    @Mock private LocationRepositoryPort locationRepository;
    @Mock private ParcheFeignClient parcheFeignClient;
    @Mock private PlaceGeocoderPort placeGeocoder;

    @InjectMocks
    private GetNearbyParchesUseCase useCase;

    private static final Location USER_LOC =
            new Location("1", "user-1", 4.6035, -74.0655, "Bloque A", 10.0, LocalDateTime.now());

    @Test
    void shouldThrowInvalidRadiusWhenRadiusTooLarge() {
        assertThatThrownBy(() -> useCase.getNearbyParches("user-1", 6000))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void shouldThrowInvalidRadiusWhenRadiusIsZero() {
        assertThatThrownBy(() -> useCase.getNearbyParches("user-1", 0))
                .isInstanceOf(InvalidRadiusException.class);
    }

    @Test
    void shouldThrowLocationNotFoundWhenUserHasNoLocation() {
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getNearbyParches("user-1", 500))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenFeignFails() {
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString()))
                .thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> useCase.getNearbyParches("user-1", 500))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldReturnNearbyParchesWithDirectCoordinates() {
        ParceDto parce = buildParce(4.6036, -74.0656, null);
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(parce));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).hasSize(1);
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.nearbyPatches().get(0).distanceMeters()).isLessThan(500);
    }

    @Test
    void shouldReturnEmptyWhenNoParchesWithinRadius() {
        ParceDto farParce = buildParce(4.700, -74.100, null);
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(farParce));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).isEmpty();
        assertThat(result.count()).isEqualTo(0);
        assertThat(result.message()).contains("Amplía");
    }

    @Test
    void shouldReturnEmptyWhenParcheListIsEmpty() {
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of());

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).isEmpty();
    }

    @Test
    void shouldGeocodeParcheWhenNoDirectCoordinates() {
        ParceDto parce = buildParce(null, null, "Cafetería ECI");
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(parce));
        when(placeGeocoder.geocode("Cafetería ECI"))
                .thenReturn(Optional.of(new Coordinates(4.6036, -74.0656)));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).hasSize(1);
    }

    @Test
    void shouldSkipParcheWhenNoCoordinatesAndGeocodeFails() {
        ParceDto parce = buildParce(null, null, "Lugar desconocido");
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(parce));
        when(placeGeocoder.geocode(anyString())).thenReturn(Optional.empty());

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).isEmpty();
    }

    @Test
    void shouldSkipParcheWhenNoCoordinatesAndNoPlace() {
        ParceDto parce = buildParce(null, null, null);
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(parce));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).isEmpty();
    }

    @Test
    void shouldSortParchesByDistanceAscending() {
        ParceDto close = buildParce(4.6036, -74.0656, null);
        ParceDto far   = buildParce(4.6040, -74.0660, null);
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(far, close));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.nearbyPatches()).hasSize(2);
        assertThat(result.nearbyPatches().get(0).distanceMeters())
                .isLessThan(result.nearbyPatches().get(1).distanceMeters());
    }

    @Test
    void shouldReturnFoundMessageWhenParchesExist() {
        ParceDto parce = buildParce(4.6036, -74.0656, null);
        when(locationRepository.findByUserId("user-1")).thenReturn(Optional.of(USER_LOC));
        when(parcheFeignClient.getActiveParches(anyString())).thenReturn(List.of(parce));

        NearbyParchesResponseDto result = useCase.getNearbyParches("user-1", 500);

        assertThat(result.message()).contains("encontraron");
    }

    private ParceDto buildParce(Double lat, Double lng, String place) {
        ParceDto p = new ParceDto();
        p.setId(UUID.randomUUID());
        p.setName("Parce Test");
        p.setDescription("Desc");
        p.setPlace(place);
        p.setCategory("ESTUDIO");
        p.setMaximumQuota(10);
        p.setActualMembers(3);
        p.setLatitude(lat);
        p.setLongitude(lng);
        return p;
    }
}
