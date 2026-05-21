package edu.eci.patricia.geolocalization.application.dto.response;

public record InternalNearbyUserDto(
        String userId,
        double distanciaMetros,
        String zona
) {}
