package edu.eci.patricia.geolocalization.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileDto {
    private String userId;
    private boolean geolocationEnabled;
}
