package edu.eci.patricia.geolocalization.application.dto.response;

public record MapItemDto(
        String type,
        String referenceId,
        Double latitude,
        Double longitude,
        String campusZone,
        String label,
        boolean activo
) {}
