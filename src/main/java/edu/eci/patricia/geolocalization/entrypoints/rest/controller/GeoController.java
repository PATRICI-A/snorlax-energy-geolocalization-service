package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.entrypoints.advice.ErrorResponse;
import edu.eci.patricia.geolocalization.entrypoints.rest.mapper.GeoRestMapper;
import edu.eci.patricia.geolocalization.entrypoints.rest.request.UpdateLocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Tag(name = "Geolocation", description = "RF06 — Campus location detection and proximity queries for matching and parches. " +
        "All endpoints require a valid JWT token in the Authorization header.")
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
            summary = "Update my location (RF06.1)",
            description = "Updates the authenticated student's current campus location. " +
                    "Validates that coordinates are within the ECI campus perimeter and that the " +
                    "timestamp is not older than 30 seconds. " +
                    "Publishes a `geo.location.updated` event to RabbitMQ for the matching and parches modules. " +
                    "The campus zone is resolved automatically via Google Maps Geocoding API (falls back to client-provided value).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated successfully",
                    content = @Content(schema = @Schema(implementation = LocationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Coordinates outside campus perimeter (OUTSIDE_CAMPUS) or timestamp older than 30 seconds (STALE_TIMESTAMP)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/location")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(updateLocationPort.updateLocation(userId, mapper.toDto(request)));
    }

    @Operation(
            summary = "Get location of a user (RF06.2)",
            description = "Returns the last known campus location of the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found",
                    content = @Content(schema = @Schema(implementation = LocationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No location registered for this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/location/{userId}")
    public ResponseEntity<LocationResponseDto> getLocation(
            @Parameter(description = "UUID of the target user", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable String userId) {
        return ResponseEntity.ok(getLocationPort.getLocation(userId));
    }

    @Operation(
            summary = "Get my own location",
            description = "Returns the last known campus location of the authenticated user (userId extracted from JWT).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found",
                    content = @Content(schema = @Schema(implementation = LocationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No location registered yet for this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/location/me")
    public ResponseEntity<LocationResponseDto> getMyLocation(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(getLocationPort.getLocation(userId));
    }

    @Operation(
            summary = "Get nearby users (RF06.2)",
            description = "Returns all users within the specified radius (in meters) from the given coordinates. " +
                    "Used by the matching and parches modules to suggest nearby students. " +
                    "Maximum radius: 5000 meters. Results are sorted by distance ascending (closest first).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of nearby users sorted by ascending distance",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NearbyUserResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius out of range (must be 1–5000 m)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT token missing or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyUserResponseDto>> getNearbyUsers(
            @Parameter(description = "Latitude of the reference point", example = "4.628742", required = true)
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @Parameter(description = "Longitude of the reference point", example = "-74.064583", required = true)
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @Parameter(description = "Search radius in meters (1–5000). Default: 500.", example = "500")
            @RequestParam(defaultValue = "500") @Positive double radiusMeters) {
        return ResponseEntity.ok(getNearbyUsersPort.getNearbyUsers(latitude, longitude, radiusMeters));
    }
}
