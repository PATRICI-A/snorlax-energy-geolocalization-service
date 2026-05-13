package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.patricia.geolocalization.application.dto.response.LocationResponseDto;
import edu.eci.patricia.geolocalization.application.dto.response.NearbyUserResponseDto;
import edu.eci.patricia.geolocalization.domain.ports.in.GetLocationPort;
import edu.eci.patricia.geolocalization.domain.ports.in.GetNearbyUsersPort;
import edu.eci.patricia.geolocalization.domain.ports.in.UpdateLocationPort;
import edu.eci.patricia.geolocalization.entrypoints.rest.mapper.GeoRestMapper;
import edu.eci.patricia.geolocalization.infrastructure.external.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeoController.class)
class GeoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @WithMockUser(username = "user-123")
    void shouldReturnLocationOnGetByUserId() throws Exception {
        LocationResponseDto dto = new LocationResponseDto(
                "user-123", 4.6035, -74.0655, "Bloque B", 10.0, LocalDateTime.now());
        when(getLocationPort.getLocation("user-123")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/geo/location/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.campusZone").value("Bloque B"));
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldReturnNearbyUsers() throws Exception {
        NearbyUserResponseDto nearby = new NearbyUserResponseDto(
                "user-B", 4.604, -74.066, "Bloque C", 120.0, LocalDateTime.now());
        when(getNearbyUsersPort.getNearbyUsers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(nearby));

        mockMvc.perform(get("/api/v1/geo/nearby")
                        .param("latitude", "4.6035")
                        .param("longitude", "-74.0655")
                        .param("radiusMeters", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-B"))
                .andExpect(jsonPath("$[0].distanceMeters").value(120.0));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/geo/location/user-123"))
                .andExpect(status().isUnauthorized());
    }
}
