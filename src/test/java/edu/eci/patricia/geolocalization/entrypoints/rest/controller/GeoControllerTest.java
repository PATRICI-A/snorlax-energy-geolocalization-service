package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.domain.exceptions.LocationNotFoundException;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.entrypoints.rest.mapper.GeoRestMapper;
import edu.eci.patricia.geolocalization.infrastructure.external.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeoController.class)
class GeoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UpdateLocationPort updateLocationPort;
    @MockitoBean
    private GetLocationPort getLocationPort;
    @MockitoBean
    private GetNearbyUsersPort getNearbyUsersPort;
    @MockitoBean
    private GeoRestMapper mapper;
    @MockitoBean
    private JwtService jwtService;

    private static final String USER_ID = "test-user-uuid";
    private static final String TOKEN = "test-token";

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void updateLocation_validRequest_returns200() throws Exception {
        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(TOKEN)).thenReturn(USER_ID);

        LocationResponseDto response = new LocationResponseDto(
                USER_ID, 4.630, -74.063, "Bloque A", 10.0, LocalDateTime.now());

        when(mapper.toDto(any())).thenReturn(new UpdateLocationRequestDto(4.630, -74.063, 10.0, "Bloque A", null));
        when(updateLocationPort.updateLocation(eq(USER_ID), any())).thenReturn(response);

        String body = """
                {"latitude": 4.630, "longitude": -74.063, "accuracy": 10.0}
                """;

        mockMvc.perform(put("/api/v1/geo/location")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    void updateLocation_noAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/geo/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": 4.630, \"longitude\": -74.063}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLocation_found_returns200() throws Exception {
        LocationResponseDto response = new LocationResponseDto(
                "other-user", 4.629, -74.064, "Cafetería", null, LocalDateTime.now());
        when(getLocationPort.getLocation("other-user")).thenReturn(response);

        mockMvc.perform(get("/api/v1/geo/location/other-user")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("other-user"));
    }

    @Test
    void getLocation_notFound_returns404() throws Exception {
        when(getLocationPort.getLocation("ghost")).thenThrow(new LocationNotFoundException("ghost"));

        mockMvc.perform(get("/api/v1/geo/location/ghost")
                        .with(authentication(auth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("LOCATION_NOT_FOUND"));
    }

    @Test
    void getMyLocation_authenticated_returns200() throws Exception {
        LocationResponseDto response = new LocationResponseDto(
                USER_ID, 4.630, -74.063, "Bloque A", 10.0, LocalDateTime.now());
        when(getLocationPort.getLocation(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geo/location/me")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    void getNearbyUsers_validParams_returns200() throws Exception {
        NearbyUserResponseDto nearby = new NearbyUserResponseDto(
                "neighbor", 4.629, -74.064, "Cafetería", 45.3, LocalDateTime.now());
        when(getNearbyUsersPort.getNearbyUsers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(nearby));

        mockMvc.perform(get("/api/v1/geo/nearby")
                        .with(authentication(auth()))
                        .param("latitude", "4.630")
                        .param("longitude", "-74.063")
                        .param("radiusMeters", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("neighbor"));
    }

    @Test
    void getNearbyUsers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/geo/nearby")
                        .param("latitude", "4.630")
                        .param("longitude", "-74.063"))
                .andExpect(status().isUnauthorized());
    }
}
