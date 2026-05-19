package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.infrastructure.external.dto.CampusEventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "campus-events-service", url = "${campus.events.service.url}")
public interface CampusEventsFeignClient {

    @GetMapping("/api/v1/events/campus/active")
    List<CampusEventDto> getActiveEvents();
}
