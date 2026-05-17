package edu.eci.patricia.geolocalization.entrypoints.advice;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response returned by all error endpoints")
public record ErrorResponse(
        @Schema(description = "Internal error code", example = "LOCATION_NOT_FOUND")
        String codigo,
        @Schema(description = "Human-readable error message", example = "No location found for user: abc-123")
        String mensaje,
        @Schema(description = "ISO-8601 timestamp of the error")
        LocalDateTime timestamp,
        @Schema(description = "Additional technical detail, may be null", nullable = true)
        String detalle
) {
    public static ErrorResponse of(String codigo, String mensaje, String detalle) {
        return new ErrorResponse(codigo, mensaje, LocalDateTime.now(), detalle);
    }
}
