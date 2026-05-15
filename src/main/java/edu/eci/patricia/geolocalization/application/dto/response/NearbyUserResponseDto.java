package edu.eci.patricia.geolocalization.application.dto.response;

import java.time.LocalDateTime;

public record NearbyUserResponseDto(
        String userId,
        double latitude,
        double longitude,
        String campusZone,
        double distanceMeters,
        LocalDateTime updatedAt,
        boolean activo,
        boolean lowPrecision
) {}
