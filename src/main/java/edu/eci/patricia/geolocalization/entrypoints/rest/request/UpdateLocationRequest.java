package edu.eci.patricia.geolocalization.entrypoints.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateLocationRequest {

    @Schema(description = "Latitude of the device", example = "4.628742")
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @Schema(description = "Longitude of the device", example = "-74.064583")
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Schema(description = "GPS accuracy in meters. Values >100m are stored with a low-precision flag.", example = "12.0")
    private Double accuracy;

    @Schema(description = "Campus zone hint provided by the client (overridden by Google Maps when API key is set)", example = "Bloque de Ingeniería")
    private String campusZone;

    @Schema(description = "ISO-8601 timestamp of the GPS reading. Must not be older than 30 seconds.", example = "2025-05-13T10:30:00Z")
    @PastOrPresent(message = "Timestamp must not be in the future")
    private Instant timestamp;
}
