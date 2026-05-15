package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.domain.ports.out.PlaceGeocoderPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.GeocodingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GoogleGeocodingAdapter implements CampusZoneResolverPort, PlaceGeocoderPort {

    private static final List<String> POI_TYPES =
            List.of("establishment", "point_of_interest", "university", "school");

    // Campus ECI bounding box — biases Google results to the campus area
    private static final String CAMPUS_BOUNDS = "4.600,-74.069|4.607,-74.062";
    private static final String CAMPUS_CONTEXT = ", Escuela Colombiana de Ingeniería Julio Garavito, Bogotá";

    private final RestClient restClient;
    private final String apiKey;
    private final Map<String, Optional<Coordinates>> geocodeCache = new ConcurrentHashMap<>();

    public GoogleGeocodingAdapter(
            @Value("${google.maps.api-key}") String apiKey) {
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
    }

    @Override
    public Optional<String> resolveZone(double latitude, double longitude) {
        try {
            GeocodingResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("latlng", latitude + "," + longitude)
                            .queryParam("key", apiKey)
                            .queryParam("language", "es")
                            .build())
                    .retrieve()
                    .body(GeocodingResponseDto.class);

            if (response == null || !"OK".equals(response.status()) || response.results().isEmpty()) {
                log.warn("Google Geocoding returned status: {}",
                        response != null ? response.status() : "null");
                return Optional.empty();
            }

            return response.results().stream()
                    .filter(r -> r.types() != null &&
                            r.types().stream().anyMatch(POI_TYPES::contains))
                    .map(GeocodingResponseDto.Result::formattedAddress)
                    .findFirst()
                    .or(() -> Optional.ofNullable(
                            response.results().get(0).formattedAddress()));

        } catch (Exception e) {
            log.warn("Google Geocoding API error for ({}, {}): {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Coordinates> geocode(String place) {
        if (place == null || place.isBlank()) return Optional.empty();
        return geocodeCache.computeIfAbsent(place, this::forwardGeocode);
    }

    private Optional<Coordinates> forwardGeocode(String place) {
        try {
            String query = place + CAMPUS_CONTEXT;
            GeocodingResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", query)
                            .queryParam("bounds", CAMPUS_BOUNDS)
                            .queryParam("key", apiKey)
                            .queryParam("language", "es")
                            .build())
                    .retrieve()
                    .body(GeocodingResponseDto.class);

            if (response == null || !"OK".equals(response.status()) || response.results().isEmpty()) {
                log.warn("Forward geocoding found no results for '{}'", place);
                return Optional.empty();
            }

            GeocodingResponseDto.Result first = response.results().get(0);
            if (first.geometry() == null || first.geometry().location() == null) {
                return Optional.empty();
            }

            GeocodingResponseDto.Location loc = first.geometry().location();
            return Optional.of(new Coordinates(loc.lat(), loc.lng()));

        } catch (Exception e) {
            log.warn("Forward geocoding failed for '{}': {}", place, e.getMessage());
            return Optional.empty();
        }
    }
}
