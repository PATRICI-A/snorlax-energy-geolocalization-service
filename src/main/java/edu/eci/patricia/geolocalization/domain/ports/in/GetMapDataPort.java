package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.MapDataResponseDto;

public interface GetMapDataPort {
    MapDataResponseDto getMapData();
}
