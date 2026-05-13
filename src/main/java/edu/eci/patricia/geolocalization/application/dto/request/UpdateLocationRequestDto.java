package edu.eci.patricia.geolocalization.application.dto.request;

public record UpdateLocationRequestDto(
        double latitude,
        double longitude,
        Double accuracy,
        String campusZone
) {}
