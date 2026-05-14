package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.entrypoints.rest.mapper.GeoRestMapper;
import edu.eci.patricia.geolocalization.entrypoints.rest.request.UpdateLocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Geolocation", description = "Campus location detection and proximity queries for matching and parches")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/v1/geo")
@RequiredArgsConstructor
public class GeoController {

    private final UpdateLocationPort updateLocationPort;
    private final GetLocationPort getLocationPort;
    private final GetNearbyUsersPort getNearbyUsersPort;
    private final GeoRestMapper mapper;

    @Operation(
            summary = "Update my location",
            description = "Updates the authenticated student's current campus location. " +
                    "Accepts latitude, longitude, optional accuracy and campus zone. " +
                    "Publishes a geo.location.updated event for the matching and parches modules.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates"),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid")
    })
    @PutMapping("/location")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(updateLocationPort.updateLocation(userId, mapper.toDto(request)));
    }

    @Operation(
            summary = "Get location of a user",
            description = "Returns the last known campus location of the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "No location registered for this user"),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid")
    })
    @GetMapping("/location/{userId}")
    public ResponseEntity<LocationResponseDto> getLocation(@PathVariable String userId) {
        return ResponseEntity.ok(getLocationPort.getLocation(userId));
    }

    @Operation(
            summary = "Get my own location",
            description = "Returns the last known campus location of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "No location registered yet"),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid")
    })
    @GetMapping("/location/me")
    public ResponseEntity<LocationResponseDto> getMyLocation(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(getLocationPort.getLocation(userId));
    }

    @Operation(
            summary = "Get nearby users",
            description = "Returns all users within the specified radius (in meters) from the given coordinates. " +
                    "Used by the matching and parches modules to suggest nearby students. " +
                    "Maximum radius: 5000 meters. Results are sorted by distance (closest first).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of nearby users sorted by distance"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius out of range"),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid")
    })
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyUserResponseDto>> getNearbyUsers(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam(defaultValue = "500") @Positive double radiusMeters) {
        return ResponseEntity.ok(getNearbyUsersPort.getNearbyUsers(latitude, longitude, radiusMeters));
    }
}
