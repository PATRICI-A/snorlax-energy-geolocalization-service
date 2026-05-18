package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.MapItemDto;
import edu.eci.patricia.geolocalization.application.dto.response.UserPositionDto;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMapDataUseCase implements GetMapDataPort {

    private final LocationRepositoryPort locationRepository;
    private final ParcheFeignClient parcheFeignClient;
    private final CampusEventsFeignClient campusEventsFeignClient;
    private final PlaceGeocoderPort placeGeocoder;

    @Override
    public MapDataResponseDto getMapData(String userId, List<String> capas, double radius) {
        UserPositionDto userPosition = resolveUserPosition(userId, capas);

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

        return new MapDataResponseDto(
                200,
                userPosition,
                List.of(),             // zonas: campus zone polygons — to be implemented via zone-service
                parchesFuture.join(),
                eventosFuture.join()
        );
    }

    private UserPositionDto resolveUserPosition(String userId, List<String> capas) {
        if (!capas.contains("mi_posicion")) {
            return null;
        }
        return locationRepository.findByUserId(userId)
                .map(loc -> new UserPositionDto(loc.getLatitude(), loc.getLongitude(), loc.getCampusZone()))
                .orElse(null);
    }

    private MapItemDto parceToMapItem(ParceDto p) {
        String label = p.getName() + (p.getPlace() != null ? " — " + p.getPlace() : "");
        Double lat = null;
        Double lng = null;
        if (p.getPlace() != null) {
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
