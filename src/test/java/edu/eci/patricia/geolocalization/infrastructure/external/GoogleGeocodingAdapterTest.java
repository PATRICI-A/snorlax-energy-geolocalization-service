package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.infrastructure.external.dto.GeocodingResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleGeocodingAdapterTest {

    private GoogleGeocodingAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoogleGeocodingAdapter("test-api-key");
    }

    @SuppressWarnings("unchecked")
    private void mockRestClient(GeocodingResponseDto response) {
        RestClient mockClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec<?> uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(mockClient.get()).thenAnswer(inv -> uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(GeocodingResponseDto.class)).thenReturn(response);

        ReflectionTestUtils.setField(adapter, "restClient", mockClient);
    }

    @Test
    void resolveZone_nullResponse_returnsEmpty() {
        mockRestClient(null);
        assertThat(adapter.resolveZone(4.628, -74.064)).isEmpty();
    }

    @Test
    void resolveZone_nonOkStatus_returnsEmpty() {
        mockRestClient(new GeocodingResponseDto(List.of(), "ZERO_RESULTS"));
        assertThat(adapter.resolveZone(4.628, -74.064)).isEmpty();
    }

    @Test
    void resolveZone_emptyResults_returnsEmpty() {
        mockRestClient(new GeocodingResponseDto(List.of(), "OK"));
        assertThat(adapter.resolveZone(4.628, -74.064)).isEmpty();
    }

    @Test
    void resolveZone_poiResult_prefersPOI() {
        GeocodingResponseDto.Result street = new GeocodingResponseDto.Result("Carrera 45", List.of("route"), null, null);
        GeocodingResponseDto.Result poi    = new GeocodingResponseDto.Result("ECI", List.of("university", "establishment"), null, null);
        mockRestClient(new GeocodingResponseDto(List.of(street, poi), "OK"));

        assertThat(adapter.resolveZone(4.628, -74.064)).contains("ECI");
    }

    @Test
    void resolveZone_onlyStreet_returnsFallback() {
        GeocodingResponseDto.Result street = new GeocodingResponseDto.Result("Carrera 45", List.of("route"), null, null);
        mockRestClient(new GeocodingResponseDto(List.of(street), "OK"));

        assertThat(adapter.resolveZone(4.628, -74.064)).contains("Carrera 45");
    }

    @Test
    void resolveZone_resultHasNullTypes_skipsPoiFilter() {
        GeocodingResponseDto.Result r = new GeocodingResponseDto.Result("Somewhere", null, null, null);
        mockRestClient(new GeocodingResponseDto(List.of(r), "OK"));

        assertThat(adapter.resolveZone(4.628, -74.064)).contains("Somewhere");
    }

    @Test
    void resolveZone_exceptionDuringCall_returnsEmpty() {
        RestClient mockClient = mock(RestClient.class);
        when(mockClient.get()).thenThrow(new RuntimeException("Network error"));
        ReflectionTestUtils.setField(adapter, "restClient", mockClient);

        assertThat(adapter.resolveZone(4.628, -74.064)).isEmpty();
    }
}
