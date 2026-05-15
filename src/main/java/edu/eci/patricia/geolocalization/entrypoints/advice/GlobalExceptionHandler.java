package edu.eci.patricia.geolocalization.entrypoints.advice;

import edu.eci.patricia.geolocalization.domain.exceptions.GeoLocationDisabledException;
import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.exceptions.OutOfCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeoLocationDisabledException.class)
    public ResponseEntity<ErrorResponse> handleGeoLocationDisabled(GeoLocationDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("GEO_LOCATION_DISABLED", ex.getMessage(), null));
    }

    @ExceptionHandler(OutOfCampusException.class)
    public ResponseEntity<ErrorResponse> handleOutOfCampus(OutOfCampusException ex) {
        return ResponseEntity.status(422)
                .body(ErrorResponse.of("OUT_OF_CAMPUS", ex.getMessage(), null));
    }

    @ExceptionHandler(StaleTimestampException.class)
    public ResponseEntity<ErrorResponse> handleStaleTimestamp(StaleTimestampException ex) {
        return ResponseEntity.status(422)
                .body(ErrorResponse.of("STALE_TIMESTAMP", ex.getMessage(), null));
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("LOCATION_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("USER_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidRadiusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRadius(InvalidRadiusException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_RADIUS", ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", mensaje, detalle));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("BAD_REQUEST", ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", ex.getMessage()));
    }
}
