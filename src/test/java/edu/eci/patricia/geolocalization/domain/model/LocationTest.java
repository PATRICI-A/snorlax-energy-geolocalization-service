package edu.eci.patricia.geolocalization.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void shouldUpdateCoordinates() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", 5.0, LocalDateTime.now());

        location.updateCoordinates(4.6035, -74.0655, 10.0);

        assertThat(location.getLatitude()).isEqualTo(4.6035);
        assertThat(location.getLongitude()).isEqualTo(-74.0655);
        assertThat(location.getAccuracy()).isEqualTo(10.0);
        assertThat(location.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSetDefaultTimestampWhenNullProvided() {
        Location location = new Location("id", "user-1", 4.60, -74.06, null, null, null);
        assertThat(location.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldBeActiveWhenUpdatedAtWithinFiveMinutes() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", 5.0,
                LocalDateTime.now().minusMinutes(2));

        assertThat(location.isActive()).isTrue();
    }

    @Test
    void shouldBeInactiveWhenUpdatedAtExceedsFiveMinutes() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", 5.0,
                LocalDateTime.now().minusMinutes(10));

        assertThat(location.isActive()).isFalse();
    }

    @Test
    void shouldMarkLowPrecisionWhenAccuracyAboveThreshold() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", 150.0, LocalDateTime.now());
        assertThat(location.isLowPrecision()).isTrue();
    }

    @Test
    void shouldNotMarkLowPrecisionWhenAccuracyBelowThreshold() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", 10.0, LocalDateTime.now());
        assertThat(location.isLowPrecision()).isFalse();
    }

    @Test
    void shouldMarkLowPrecisionWhenAccuracyIsNull() {
        Location location = new Location("id", "user-1", 4.60, -74.06, "Bloque A", null, LocalDateTime.now());
        assertThat(location.isLowPrecision()).isTrue();
    }
}
