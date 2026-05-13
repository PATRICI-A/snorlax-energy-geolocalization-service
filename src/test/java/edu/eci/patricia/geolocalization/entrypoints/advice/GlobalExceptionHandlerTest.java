package edu.eci.patricia.geolocalization.entrypoints.advice;

import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404ForLocationNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleLocationNotFound(
                new LocationNotFoundException("user-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().codigo()).isEqualTo("LOCATION_NOT_FOUND");
    }

    @Test
    void shouldReturn404ForUserNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(
                new UserNotFoundException("user-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().codigo()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void shouldReturn400ForInvalidRadius() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRadius(
                new InvalidRadiusException("Radius out of range"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().codigo()).isEqualTo("INVALID_RADIUS");
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().codigo()).isEqualTo("BAD_REQUEST");
    }
}
