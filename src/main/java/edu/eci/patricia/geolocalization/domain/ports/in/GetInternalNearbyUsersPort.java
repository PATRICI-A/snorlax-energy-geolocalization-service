package edu.eci.patricia.geolocalization.domain.ports.in;

import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUsersResponseDto;

public interface GetInternalNearbyUsersPort {
    InternalNearbyUsersResponseDto getNearbyUsers(String userId, double radius, boolean soloActivos);
}
