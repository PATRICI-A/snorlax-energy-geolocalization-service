package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.MapItemDto;
import edu.eci.patricia.geolocalization.application.dto.response.UserPositionDto;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetMapDataPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.PlaceGeocoderPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import edu.eci.patricia.geolocalization.infrastructure.external.CampusEventsFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.ParcheFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.CampusEventDto;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.ParceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMapDataUseCase implements GetMapDataPort {

    private static final int ACTIVE_MINUTES = 5;

    private final LocationRepositoryPort locationRepository;
    private final ParcheFeignClient parcheFeignClient;
    private final CampusEventsFeignClient campusEventsFeignClient;
    private final PlaceGeocoderPort placeGeocoder;

    @Override
    public MapDataResponseDto getMapData(String userId, List<String> capas, double radius) {
        Optional<Location> userLocation = locationRepository.findByUserId(userId);

        UserPositionDto userPosition = (capas.contains("mi_posicion") && userLocation.isPresent())
                ? new UserPositionDto(
                        userLocation.get().getLatitude(),
                        userLocation.get().getLongitude(),
                        userLocation.get().getCampusZone())
                : null;

        CompletableFuture<List<MapItemDto>> parchesFuture = capas.contains("parches")
                ? CompletableFuture.supplyAsync(() -> parcheFeignClient.getActiveParches("ACTIVO"))
                        .thenApply(list -> list.stream().map(this::parceToMapItem).toList())
                        .exceptionally(e -> {
                            log.warn("Could not fetch parches: {}", e.getMessage());
                            return List.of();
                        })
                : CompletableFuture.completedFuture(List.of());

        CompletableFuture<List<MapItemDto>> eventosFuture = capas.contains("eventos")
                ? CompletableFuture.supplyAsync(campusEventsFeignClient::getActiveEvents)
                        .thenApply(list -> list.stream().map(this::eventToMapItem).toList())
                        .exceptionally(e -> {
                            log.warn("Could not fetch campus events: {}", e.getMessage());
                            return List.of();
                        })
                : CompletableFuture.completedFuture(List.of());

        CompletableFuture<List<MapItemDto>> usuariosFuture =
                (capas.contains("usuarios") && userLocation.isPresent())
                ? CompletableFuture.supplyAsync(
                        () -> resolveNearbyUsers(userId, userLocation.get(), radius))
                        .exceptionally(e -> {
                            log.warn("Could not fetch nearby users: {}", e.getMessage());
                            return List.of();
                        })
                : CompletableFuture.completedFuture(List.of());

        return new MapDataResponseDto(
                200,
                userPosition,
                List.of(),
                parchesFuture.join(),
                eventosFuture.join(),
                usuariosFuture.join()
        );
    }

    private List<MapItemDto> resolveNearbyUsers(String userId, Location userLoc, double radius) {
        LocalDateTime activeSince = LocalDateTime.now().minusMinutes(ACTIVE_MINUTES);
        return locationRepository
                .findNearbyActiveSharing(
                        userLoc.getLatitude(), userLoc.getLongitude(), radius, activeSince)
                .stream()
                .filter(loc -> !loc.getUserId().equals(userId))
                .map(loc -> new MapItemDto(
                        "USER",
                        loc.getUserId(),
                        loc.getLatitude(),
                        loc.getLongitude(),
                        loc.getCampusZone(),
                        loc.getCampusZone() != null ? loc.getCampusZone() : "Campus ECI",
                        true))
                .toList();
    }

    private MapItemDto parceToMapItem(ParceDto p) {
        String label = p.getName() + (p.getPlace() != null ? " — " + p.getPlace() : "");
        Double lat = p.getLatitude();
        Double lng = p.getLongitude();
        if ((lat == null || lng == null) && p.getPlace() != null) {
            Optional<Coordinates> coords = placeGeocoder.geocode(p.getPlace());
            if (coords.isPresent()) {
                lat = coords.get().getLatitude();
                lng = coords.get().getLongitude();
            }
        }
        return new MapItemDto("PARCE", p.getId() != null ? p.getId().toString() : null,
                lat, lng, null, label, true);
    }

    private MapItemDto eventToMapItem(CampusEventDto e) {
        return new MapItemDto("EVENT", e.getId(),
                e.getLatitude(), e.getLongitude(),
                e.getCampusZone(), e.getName(), true);
    }
}
