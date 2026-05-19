package edu.eci.patricia.geolocalization.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void constructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Location loc = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, now);

        assertThat(loc.getId()).isEqualTo("id1");
        assertThat(loc.getUserId()).isEqualTo("user-1");
        assertThat(loc.getLatitude()).isEqualTo(4.628);
        assertThat(loc.getLongitude()).isEqualTo(-74.064);
        assertThat(loc.getCampusZone()).isEqualTo("Bloque A");
        assertThat(loc.getAccuracy()).isEqualTo(10.0);
        assertThat(loc.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void constructor_nullTimestamp_usesNow() {
        Location loc = new Location(null, "user-2", 4.628, -74.064, null, null, null);
        assertThat(loc.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateCoordinates_updatesFieldsAndTimestamp() throws InterruptedException {
        Location loc = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());
        Thread.sleep(5);
        LocalDateTime before = loc.getUpdatedAt();

        loc.updateCoordinates(4.630, -74.060, 15.0);

        assertThat(loc.getLatitude()).isEqualTo(4.630);
        assertThat(loc.getLongitude()).isEqualTo(-74.060);
        assertThat(loc.getAccuracy()).isEqualTo(15.0);
        assertThat(loc.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void setCampusZone_updatesZone() {
        Location loc = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());
        loc.setCampusZone("Cafetería");
        assertThat(loc.getCampusZone()).isEqualTo("Cafetería");
    }

    @Test
    void setId_updatesId() {
        Location loc = new Location(null, "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());
        loc.setId("new-id");
        assertThat(loc.getId()).isEqualTo("new-id");
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
