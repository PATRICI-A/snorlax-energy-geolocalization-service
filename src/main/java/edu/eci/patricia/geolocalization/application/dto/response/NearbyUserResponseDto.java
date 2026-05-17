package edu.eci.patricia.geolocalization.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "A nearby user with their distance from the query coordinates")
public record NearbyUserResponseDto(
        @Schema(description = "UUID of the nearby user", example = "550e8400-e29b-41d4-a716-446655440000")
        String userId,
        @Schema(description = "Latitude of the nearby user", example = "4.629100")
        double latitude,
        @Schema(description = "Longitude of the nearby user", example = "-74.064100")
        double longitude,
        @Schema(description = "Campus zone of the nearby user", example = "Cafetería Central")
        String campusZone,
        @Schema(description = "Haversine distance in meters from the query coordinates", example = "45.3")
        double distanceMeters,
        @Schema(description = "ISO-8601 timestamp of the user's last location update")
        LocalDateTime updatedAt
) {}
