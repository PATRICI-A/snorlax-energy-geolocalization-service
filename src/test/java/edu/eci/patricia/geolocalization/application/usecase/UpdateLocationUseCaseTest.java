package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.GeoLocationDisabledException;
import edu.eci.patricia.geolocalization.domain.exceptions.OutOfCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.UserProfilePort;
import edu.eci.patricia.geolocalization.infrastructure.config.CampusProperties;
import edu.eci.patricia.geolocalization.infrastructure.external.LocationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @Mock
    private CampusZoneResolverPort campusZoneResolver;

    @Mock
    private LocationEventPublisher eventPublisher;

    @Mock
    private UserProfilePort userProfilePort;

    @Mock
    private CampusProperties campusProperties;

    @InjectMocks
    private UpdateLocationUseCase useCase;

    private UpdateLocationRequestDto dto;
    private Location savedLocation;

    @BeforeEach
    void setUp() {
        when(userProfilePort.isGeoLocationEnabled(anyString())).thenReturn(true);
        when(campusProperties.getCenterLat()).thenReturn(4.6035);
        when(campusProperties.getCenterLng()).thenReturn(-74.0653);
        when(campusProperties.getPerimeterRadiusMeters()).thenReturn(1000.0);

        dto = new UpdateLocationRequestDto(4.6035, -74.0655, 10.0, null, null);
        savedLocation = new Location("loc-1", "user-123", 4.6035, -74.0655,
                "Escuela Colombiana de Ingeniería Julio Garavito", 10.0, LocalDateTime.now());
    }

    @Test
    void shouldAutoDetectCampusZoneFromGoogleMaps() {
        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.empty());
        when(campusZoneResolver.resolveZone(anyDouble(), anyDouble()))
                .thenReturn(Optional.of("Escuela Colombiana de Ingeniería Julio Garavito"));
        when(locationRepository.save(any())).thenReturn(savedLocation);
        doNothing().when(eventPublisher).publishLocationUpdated(any());

        LocationResponseDto result = useCase.updateLocation("user-123", dto);

        assertThat(result.campusZone()).isEqualTo("Escuela Colombiana de Ingeniería Julio Garavito");
        verify(campusZoneResolver).resolveZone(4.6035, -74.0655);
        verify(locationRepository).save(any());
        verify(eventPublisher).publishLocationUpdated(any());
    }

    @Test
    void shouldFallBackToClientZoneWhenGoogleMapsFails() {
        UpdateLocationRequestDto dtoWithZone =
                new UpdateLocationRequestDto(4.6035, -74.0655, 10.0, "Bloque B", null);
        Location savedWithFallback = new Location("loc-1", "user-123", 4.6035, -74.0655,
                "Bloque B", 10.0, LocalDateTime.now());

        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.empty());
        when(campusZoneResolver.resolveZone(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(savedWithFallback);
        doNothing().when(eventPublisher).publishLocationUpdated(any());

        LocationResponseDto result = useCase.updateLocation("user-123", dtoWithZone);

        assertThat(result.campusZone()).isEqualTo("Bloque B");
    }

    @Test
    void shouldUpdateExistingLocation() {
        Location existing = new Location("loc-1", "user-123", 4.60, -74.06, "Bloque A", 5.0, LocalDateTime.now());
        when(locationRepository.findByUserId("user-123")).thenReturn(Optional.of(existing));
        when(campusZoneResolver.resolveZone(anyDouble(), anyDouble()))
                .thenReturn(Optional.of("Escuela Colombiana de Ingeniería Julio Garavito"));
        when(locationRepository.save(any())).thenReturn(savedLocation);
        doNothing().when(eventPublisher).publishLocationUpdated(any());

        LocationResponseDto result = useCase.updateLocation("user-123", dto);

        assertThat(result.latitude()).isEqualTo(4.6035);
        verify(locationRepository).save(any());
    }

    @Test
    void shouldThrowWhenGeoLocationDisabled() {
        when(userProfilePort.isGeoLocationEnabled("user-123")).thenReturn(false);

        assertThatThrownBy(() -> useCase.updateLocation("user-123", dto))
                .isInstanceOf(GeoLocationDisabledException.class);
    }

    @Test
    void shouldThrowStaleTimestampWhenTimestampTooOld() {
        UpdateLocationRequestDto dtoWithStaleTimestamp = new UpdateLocationRequestDto(
                4.6035, -74.0655, 10.0, null,
                Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> useCase.updateLocation("user-123", dtoWithStaleTimestamp))
                .isInstanceOf(StaleTimestampException.class);
    }

    @Test
    void shouldThrowOutOfCampusWhenCoordinatesOutsidePerimeter() {
        UpdateLocationRequestDto outsideDto = new UpdateLocationRequestDto(
                0.0, 0.0, 10.0, null, null);

        assertThatThrownBy(() -> useCase.updateLocation("user-123", outsideDto))
                .isInstanceOf(OutOfCampusException.class);
    }
}
