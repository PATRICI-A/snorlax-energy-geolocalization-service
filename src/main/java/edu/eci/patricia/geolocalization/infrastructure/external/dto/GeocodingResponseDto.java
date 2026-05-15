package edu.eci.patricia.geolocalization.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeocodingResponseDto(
        List<Result> results,
        String status
) {
    public record Result(
            @JsonProperty("formatted_address") String formattedAddress,
            List<String> types,
            @JsonProperty("address_components") List<AddressComponent> addressComponents
    ) {}

    public record AddressComponent(
            @JsonProperty("long_name") String longName,
            @JsonProperty("short_name") String shortName,
            List<String> types
    ) {}
}
