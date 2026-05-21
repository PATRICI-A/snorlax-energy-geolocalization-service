package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.infrastructure.external.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-profile-client", url = "${user.service.url}")
public interface UserProfileFeignClient {

    @GetMapping("/api/v1/internal/users/{userId}")
    UserProfileDto getUserProfile(@PathVariable("userId") String userId);
}
