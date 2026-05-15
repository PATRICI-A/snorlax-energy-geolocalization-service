package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;

public interface GetLocationPort {
    LocationResponseDto getLocation(String userId);
}
