package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;

public interface UpdateLocationPort {
    LocationResponseDto updateLocation(String userId, UpdateLocationRequestDto dto);
}
