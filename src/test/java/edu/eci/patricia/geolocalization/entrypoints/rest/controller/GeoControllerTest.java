package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetMapDataPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyActiveUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.entrypoints.advice.GlobalExceptionHandler;
import edu.eci.patricia.geolocalization.entrypoints.rest.mapper.GeoRestMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GeoControllerTest {

    @Mock private UpdateLocationPort updateLocationPort;
    @Mock private GetLocationPort getLocationPort;
    @Mock private GetNearbyUsersPort getNearbyUsersPort;
    @Mock private GetNearbyActiveUsersPort getNearbyActiveUsersPort;
    @Mock private GetMapDataPort getMapDataPort;
    @Mock private GeoRestMapper mapper;

    private MockMvc mockMvc;
    private static final String USER_ID = "test-user-uuid";

    @BeforeEach
    void setUp() {
        GeoController controller = new GeoController(
                updateLocationPort, getLocationPort, getNearbyUsersPort,
                getNearbyActiveUsersPort, getMapDataPort, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateLocation_validRequest_returns200() throws Exception {
        LocationResponseDto response = new LocationResponseDto(
                USER_ID, 4.630, -74.063, "Bloque A", 10.0, LocalDateTime.now(), true, false);
        when(mapper.toDto(any())).thenReturn(
                new UpdateLocationRequestDto(4.630, -74.063, 10.0, "Bloque A", null));
        when(updateLocationPort.updateLocation(eq(USER_ID), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/geo/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":4.630,\"longitude\":-74.063,\"accuracy\":10.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    void getLocation_found_returns200() throws Exception {
        LocationResponseDto response = new LocationResponseDto(
                "other", 4.629, -74.064, "Cafetería", null, LocalDateTime.now(), true, false);
        when(getLocationPort.getLocation("other")).thenReturn(response);

        mockMvc.perform(get("/api/v1/geo/location/other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("other"));
    }

    @Test
    void getLocation_notFound_returns404() throws Exception {
        when(getLocationPort.getLocation("ghost"))
                .thenThrow(new LocationNotFoundException("ghost"));

        mockMvc.perform(get("/api/v1/geo/location/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("LOCATION_NOT_FOUND"));
    }

    @Test
    void getMyLocation_returns200() throws Exception {
        LocationResponseDto response = new LocationResponseDto(
                USER_ID, 4.630, -74.063, "Bloque A", 10.0, LocalDateTime.now(), true, false);
        when(getLocationPort.getLocation(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geo/location/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    void getNearbyUsers_validParams_returns200() throws Exception {
        NearbyUserResponseDto nearby = new NearbyUserResponseDto(
                "neighbor", 4.629, -74.064, "Cafetería", 45.3, LocalDateTime.now(), true, false);
        when(getNearbyUsersPort.getNearbyUsers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(nearby));

        mockMvc.perform(get("/api/v1/geo/nearby")
                        .param("latitude", "4.630")
                        .param("longitude", "-74.063")
                        .param("radiusMeters", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("neighbor"));
    }
}
