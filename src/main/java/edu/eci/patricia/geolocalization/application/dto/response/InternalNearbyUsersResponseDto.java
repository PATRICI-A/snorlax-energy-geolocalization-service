package edu.eci.patricia.geolocalization.application.dto.response;

import java.util.List;

public record InternalNearbyUsersResponseDto(
        int status,
        int total,
        List<InternalNearbyUserDto> usuarios
) {}
