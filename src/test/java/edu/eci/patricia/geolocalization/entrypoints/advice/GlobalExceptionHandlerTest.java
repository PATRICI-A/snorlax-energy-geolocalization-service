package edu.eci.patricia.geolocalization.entrypoints.advice;

import edu.eci.patricia.geolocalization.domain.exceptions.InvalidRadiusException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationOutsideCampusException;
import edu.eci.patricia.geolocalization.domain.exceptions.StaleTimestampException;
import edu.eci.patricia.geolocalization.domain.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleLocationNotFound_returns404() {
        ResponseEntity<ErrorResponse> resp = handler.handleLocationNotFound(new LocationNotFoundException("u-1"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().codigo()).isEqualTo("LOCATION_NOT_FOUND");
    }

    @Test
    void handleUserNotFound_returns404() {
        ResponseEntity<ErrorResponse> resp = handler.handleUserNotFound(new UserNotFoundException("User not found"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().codigo()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void handleInvalidRadius_returns400() {
        ResponseEntity<ErrorResponse> resp = handler.handleInvalidRadius(new InvalidRadiusException("bad radius"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().codigo()).isEqualTo("INVALID_RADIUS");
    }

    @Test
    void handleOutsideCampus_returns422() {
        ResponseEntity<ErrorResponse> resp = handler.handleOutsideCampus(
                new LocationOutsideCampusException(40.712, -74.006));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().codigo()).isEqualTo("OUTSIDE_CAMPUS");
    }

    @Test
    void handleStaleTimestamp_returns422() {
        ResponseEntity<ErrorResponse> resp = handler.handleStaleTimestamp(new StaleTimestampException());
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().codigo()).isEqualTo("STALE_TIMESTAMP");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().codigo()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ErrorResponse> resp = handler.handleGeneric(new RuntimeException("boom"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().codigo()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void errorResponse_hasTimestamp() {
        ResponseEntity<ErrorResponse> resp = handler.handleGeneric(new Exception("test"));
        assertThat(resp.getBody().timestamp()).isNotNull();
    }
}
