package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;

import java.util.List;

public interface GetNearbyActiveUsersPort {
    List<NearbyUserResponseDto> getNearbyUsers(String userId, double radiusMeters, boolean soloActivos);
}
