package edu.eci.patricia.geolocalization.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampusEventDto {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String campusZone;
}
