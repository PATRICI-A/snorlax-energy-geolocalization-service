package edu.eci.patricia.geolocalization.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "gamification-service", url = "${gamification.service.url:http://localhost:8080}")
public interface GamificationClient {

    @PostMapping("/api/v1/gamificacion/events/zone-visited")
    Map<String, Object> reportZoneVisited(@RequestBody Map<String, String> body);
}
