package edu.eci.patricia.geolocalization.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A parche near the user with its distance")
public record NearbyParcheItemDto(
        @Schema(description = "UUID of the parche") String id,
        @Schema(description = "Name of the parche") String name,
        @Schema(description = "Description") String description,
        @Schema(description = "Place or location name") String place,
        @Schema(description = "Category") String category,
        @Schema(description = "Latitude of the parche") Double latitude,
        @Schema(description = "Longitude of the parche") Double longitude,
        @Schema(description = "Haversine distance in meters from the user", example = "120.5") double distanceMeters,
        @Schema(description = "Current number of members") int actualMembers,
        @Schema(description = "Maximum capacity") int maximumQuota,
        @Schema(description = "Whether there is available space") boolean hasAvailableSpot
) {}
