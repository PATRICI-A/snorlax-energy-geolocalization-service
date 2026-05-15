package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.domain.ports.out.CampusZoneResolverPort;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.GeocodingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GoogleGeocodingAdapter implements CampusZoneResolverPort {

    private static final String GEOCODING_URL =
            "https://maps.googleapis.com/maps/api/geocode/json";

    private static final List<String> POI_TYPES =
            List.of("establishment", "point_of_interest", "university", "school");

    private final RestClient restClient;
    private final String apiKey;

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

            // Prefer establishments/POIs (campus buildings) over street addresses
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
}
