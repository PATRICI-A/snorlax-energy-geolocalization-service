package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyParchesResponseDto;

public interface GetNearbyParchesPort {
    NearbyParchesResponseDto getNearbyParches(String userId, double radiusMeters);
}
