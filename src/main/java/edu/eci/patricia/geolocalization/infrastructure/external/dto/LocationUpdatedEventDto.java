package edu.eci.patricia.geolocalization.infrastructure.external.dto;

import java.time.LocalDateTime;

public record LocationUpdatedEventDto(
        String userId,
        double latitude,
        double longitude,
        String campusZone,
        LocalDateTime updatedAt
) {}
