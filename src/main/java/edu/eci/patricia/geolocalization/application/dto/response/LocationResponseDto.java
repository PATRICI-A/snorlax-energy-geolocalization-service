package edu.eci.patricia.geolocalization.application.dto.response;

import java.time.LocalDateTime;

public record LocationResponseDto(
        String userId,
        double latitude,
        double longitude,
        String campusZone,
        Double accuracy,
        LocalDateTime updatedAt,
        boolean activo,
        boolean lowPrecision
) {}
