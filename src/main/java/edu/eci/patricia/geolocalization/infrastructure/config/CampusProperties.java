package edu.eci.patricia.geolocalization.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "geo.campus")
@Data
public class CampusProperties {
    private double centerLat = 4.6035;
    private double centerLng = -74.0653;
    private double perimeterRadiusMeters = 1000.0;
    private double defaultRadiusMeters = 500.0;
}
