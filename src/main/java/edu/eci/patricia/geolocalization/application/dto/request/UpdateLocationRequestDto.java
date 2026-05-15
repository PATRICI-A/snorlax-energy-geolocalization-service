package edu.eci.patricia.geolocalization.application.dto.request;

import java.time.Instant;

public record UpdateLocationRequestDto(
        double latitude,
        double longitude,
        Double accuracy,
        String campusZone,
        Instant timestamp
) {}
