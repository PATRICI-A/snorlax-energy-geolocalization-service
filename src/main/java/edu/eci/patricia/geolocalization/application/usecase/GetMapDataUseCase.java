package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.MapItemDto;
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
    public MapDataResponseDto getMapData() {
        LocalDateTime activeSince = LocalDateTime.now().minusMinutes(ACTIVE_MINUTES);

        List<MapItemDto> activeUsers = locationRepository.findAllActive(activeSince).stream()
                .map(loc -> new MapItemDto(
                        "USER", loc.getUserId(),
                        loc.getLatitude(), loc.getLongitude(),
                        loc.getCampusZone(), null, true))
                .toList();

        CompletableFuture<List<MapItemDto>> parchesFuture = CompletableFuture
                .supplyAsync(() -> parcheFeignClient.getActiveParches("ACTIVE"))
                .thenApply(list -> list.stream().map(this::parceToMapItem).toList())
                .exceptionally(e -> {
                    log.warn("Could not fetch parches: {}", e.getMessage());
                    return List.of();
                });

        CompletableFuture<List<MapItemDto>> eventsFuture = CompletableFuture
                .supplyAsync(campusEventsFeignClient::getActiveEvents)
                .thenApply(list -> list.stream().map(this::eventToMapItem).toList())
                .exceptionally(e -> {
                    log.warn("Could not fetch campus events: {}", e.getMessage());
                    return List.of();
                });

        List<MapItemDto> parches = parchesFuture.join();
        List<MapItemDto> events = eventsFuture.join();

        return new MapDataResponseDto(activeUsers, parches, events);
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

        return new MapItemDto(
                "PARCE", p.getId() != null ? p.getId().toString() : null,
                lat, lng, null, label, true);
    }

    private MapItemDto eventToMapItem(CampusEventDto e) {
        return new MapItemDto(
                "EVENT", e.getId(),
                e.getLatitude(), e.getLongitude(),
                e.getCampusZone(), e.getName(), true);
    }
}
