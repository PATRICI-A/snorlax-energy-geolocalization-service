package edu.eci.patricia.geolocalization.application.usecase;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyParcheItemDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyParchesResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.ExternalServiceException;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyParchesPort;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.domain.ports.out.PlaceGeocoderPort;
import edu.eci.patricia.geolocalization.domain.valueobjects.Coordinates;
import edu.eci.patricia.geolocalization.infrastructure.external.ParcheFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.ParceDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetNearbyParchesUseCase implements GetNearbyParchesPort {

    private static final double MAX_RADIUS_METERS = 5000;

    private final LocationRepositoryPort locationRepository;
    private final ParcheFeignClient parcheFeignClient;
    private final PlaceGeocoderPort placeGeocoder;

    @Override
    public NearbyParchesResponseDto getNearbyParches(String userId, double radiusMeters) {
        if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
            throw new InvalidRadiusException("Radius must be between 1 and " + MAX_RADIUS_METERS + " meters");
        }

        Location userLocation = locationRepository.findByUserId(userId)
                .orElseThrow(() -> new LocationNotFoundException(
                        "Activa tu ubicación para ver parches cercanos"));

        Coordinates userCoords = new Coordinates(userLocation.getLatitude(), userLocation.getLongitude());

        List<ParceDto> allParches;
        try {
            allParches = parcheFeignClient.getActiveParches("ACTIVO");
        } catch (FeignException e) {
            log.error("parche-service unavailable: {}", e.getMessage());
            throw new ExternalServiceException("No se pudieron cargar los parches. Intenta más tarde.");
        }

        List<NearbyParcheItemDto> nearby = allParches.stream()
                .map(p -> toItemWithDistance(p, userCoords))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(item -> item.distanceMeters() <= radiusMeters)
                .sorted((a, b) -> Double.compare(a.distanceMeters(), b.distanceMeters()))
                .toList();

        String message = nearby.isEmpty()
                ? "No hay parches activos en este radio. Amplía el rango."
                : "Se encontraron " + nearby.size() + " parche(s) cercano(s).";

        return new NearbyParchesResponseDto(nearby, nearby.size(), message);
    }

    private Optional<NearbyParcheItemDto> toItemWithDistance(ParceDto p, Coordinates userCoords) {
        Double lat = p.getLatitude();
        Double lng = p.getLongitude();

        // Use direct coordinates if available, otherwise geocode by place name
        if ((lat == null || lng == null) && p.getPlace() != null) {
            Optional<Coordinates> geocoded = placeGeocoder.geocode(p.getPlace());
            if (geocoded.isPresent()) {
                lat = geocoded.get().getLatitude();
                lng = geocoded.get().getLongitude();
            }
        }

        if (lat == null || lng == null) {
            log.debug("Parche {} has no resolvable coordinates, skipping", p.getId());
            return Optional.empty();
        }

        Coordinates parcheCoords = new Coordinates(lat, lng);
        double distance = userCoords.distanceMetersTo(parcheCoords);

        return Optional.of(new NearbyParcheItemDto(
                p.getId() != null ? p.getId().toString() : null,
                p.getName(),
                p.getDescription(),
                p.getPlace(),
                p.getCategory(),
                lat,
                lng,
                distance,
                p.getActualMembers(),
                p.getMaximumQuota(),
                p.getActualMembers() < p.getMaximumQuota()
        ));
    }
}
