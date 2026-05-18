package edu.eci.patricia.geolocalization.application.dto.response;

import java.util.List;

public record MapDataResponseDto(
        int status,
        UserPositionDto usuario,
        List<MapItemDto> zonas,
        List<MapItemDto> parches,
        List<MapItemDto> eventos
) {}
