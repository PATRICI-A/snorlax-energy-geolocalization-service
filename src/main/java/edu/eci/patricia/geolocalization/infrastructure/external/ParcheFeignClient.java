package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.infrastructure.external.dto.ParceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "parche-service", url = "${parche.service.url}")
public interface ParcheFeignClient {

    @GetMapping("/api/v1/parches")
    List<ParceDto> getActiveParches(@RequestParam("status") String status);
}
