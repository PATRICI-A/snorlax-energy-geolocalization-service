package edu.eci.patricia.geolocalization.application.dto.response;

public record UserPositionDto(
        double lat,
        double lng,
        String zona
) {}
