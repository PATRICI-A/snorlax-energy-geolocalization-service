package edu.eci.patricia.geolocalization.entrypoints.advice;

import java.time.LocalDateTime;

public record ErrorResponse(
        String codigo,
        String mensaje,
        LocalDateTime timestamp,
        String detalle
) {
    public static ErrorResponse of(String codigo, String mensaje, String detalle) {
        return new ErrorResponse(codigo, mensaje, LocalDateTime.now(), detalle);
    }
}
