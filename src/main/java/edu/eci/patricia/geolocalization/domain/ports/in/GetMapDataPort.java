package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;

import java.util.List;

public interface GetMapDataPort {
    MapDataResponseDto getMapData(String userId, List<String> capas, double radius);
}
