package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUsersResponseDto;
import edu.eci.patricia.geolocalization.domain.ports.in.GetInternalNearbyUsersPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal Geolocation", description = "Internal-only endpoints for inter-service proximity queries. Requires X-Internal-Service-Token header.")
@Validated
@RestController
@RequestMapping("/internal/geolocation")
@RequiredArgsConstructor
public class InternalGeoController {

    private final GetInternalNearbyUsersPort getInternalNearbyUsersPort;

    @Operation(
            summary = "Get nearby users (internal)",
            description = "Returns users near the given userId's location. " +
                    "Only includes active users (last 5 min) with sharing enabled. " +
                    "Radius: 50–500 m, default 200 m. " +
                    "Returns only userId, distanciaMetros, zona — no personal data.")
    @ApiResponse(responseCode = "200", description = "List of nearby users")
    @ApiResponse(responseCode = "400", description = "Radius out of allowed range or userId missing location")
    @ApiResponse(responseCode = "403", description = "Missing or invalid X-Internal-Service-Token")
    @ApiResponse(responseCode = "404", description = "Reference user has no registered location")
    @GetMapping("/nearby")
    public ResponseEntity<InternalNearbyUsersResponseDto> getNearbyUsers(
            @RequestParam String userId,
            @RequestParam(defaultValue = "200") double radius,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(getInternalNearbyUsersPort.getNearbyUsers(userId, radius, soloActivos));
    }
}
