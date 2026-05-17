package edu.eci.patricia.geolocalization.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CoordinatesTest {

    @Test
    void validCoordinates_created() {
        Coordinates c = new Coordinates(4.628, -74.064);
        assertThat(c.getLatitude()).isEqualTo(4.628);
        assertThat(c.getLongitude()).isEqualTo(-74.064);
    }

    @Test
    void invalidLatitude_belowMinus90_throws() {
        assertThatThrownBy(() -> new Coordinates(-91.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void invalidLatitude_above90_throws() {
        assertThatThrownBy(() -> new Coordinates(91.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void invalidLongitude_belowMinus180_throws() {
        assertThatThrownBy(() -> new Coordinates(0.0, -181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void invalidLongitude_above180_throws() {
        assertThatThrownBy(() -> new Coordinates(0.0, 181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void boundaryValues_valid() {
        new Coordinates(-90, -180);
        new Coordinates(90, 180);
        new Coordinates(0, 0);
    }

    @Test
    void distanceMetersTo_samePoint_isZero() {
        Coordinates a = new Coordinates(4.628742, -74.064583);
        assertThat(a.distanceMetersTo(a)).isCloseTo(0.0, within(0.01));
    }

    @Test
    void distanceMetersTo_knownDistance() {
        // Two points ~111 m apart at equator longitude difference of ~0.001 degrees
        Coordinates a = new Coordinates(4.628, -74.064);
        Coordinates b = new Coordinates(4.629, -74.064);
        // 0.001 degree latitude ≈ 111 meters
        assertThat(a.distanceMetersTo(b)).isCloseTo(111.0, within(5.0));
    }

    @Test
    void distanceMetersTo_symmetric() {
        Coordinates a = new Coordinates(4.628, -74.064);
        Coordinates b = new Coordinates(4.632, -74.060);
        assertThat(a.distanceMetersTo(b)).isCloseTo(b.distanceMetersTo(a), within(0.001));
    }
}
