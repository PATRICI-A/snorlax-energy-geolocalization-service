package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;

import java.util.List;

public interface GetNearbyUsersPort {
    List<NearbyUserResponseDto> getNearbyUsers(double latitude, double longitude, double radiusMeters);
}
