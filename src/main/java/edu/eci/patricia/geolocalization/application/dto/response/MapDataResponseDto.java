package edu.eci.patricia.geolocalization.application.dto.response;

import java.util.List;

public record MapDataResponseDto(
        List<MapItemDto> activeUsers,
        List<MapItemDto> parches,
        List<MapItemDto> campusEvents
) {}
