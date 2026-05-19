package edu.eci.patricia.geolocalization.application.dto.response;

import java.util.List;

public record NearbyParchesResponseDto(
        List<NearbyParcheItemDto> nearbyPatches,
        int count,
        String message
) {}
