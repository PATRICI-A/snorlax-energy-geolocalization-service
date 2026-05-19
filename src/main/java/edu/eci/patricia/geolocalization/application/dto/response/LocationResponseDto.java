package edu.eci.patricia.geolocalization.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Last known campus location for a user")
public record LocationResponseDto(
        @Schema(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        String userId,
        @Schema(description = "Latitude", example = "4.628742")
        double latitude,
        @Schema(description = "Longitude", example = "-74.064583")
        double longitude,
        @Schema(description = "Campus zone resolved by Google Maps or provided by client", example = "Bloque de Ingeniería")
        String campusZone,
        @Schema(description = "GPS accuracy in meters", example = "12.0", nullable = true)
        Double accuracy,
        @Schema(description = "ISO-8601 timestamp of the last update")
        LocalDateTime updatedAt,
        boolean activo,
        boolean lowPrecision
) {}
