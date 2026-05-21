package edu.eci.patricia.geolocalization.entrypoints.rest.controller;

import edu.eci.patricia.geolocalization.application.dto.response.InternalNearbyUsersResponseDto;
import edu.eci.patricia.geolocalization.domain.ports.in.GetInternalNearbyUsersPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalGeoControllerTest {

    @Mock private GetInternalNearbyUsersPort getInternalNearbyUsersPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InternalGeoController(getInternalNearbyUsersPort))
                .build();
    }

    @Test
    void getNearbyUsers_returns200() throws Exception {
        InternalNearbyUsersResponseDto response =
                new InternalNearbyUsersResponseDto(200, 0, List.of());
        when(getInternalNearbyUsersPort.getNearbyUsers(anyString(), anyDouble(), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(get("/internal/geolocation/nearby")
                        .param("userId", "user-1")
                        .param("radius", "200")
                        .param("soloActivos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
