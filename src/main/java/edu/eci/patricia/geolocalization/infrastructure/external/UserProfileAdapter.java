package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.domain.exceptions.UserNotFoundException;
import edu.eci.patricia.geolocalization.domain.ports.out.UserProfilePort;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.UserProfileDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileAdapter implements UserProfilePort {

    private final UserProfileFeignClient userProfileFeignClient;

    @Override
    public boolean isGeoLocationEnabled(String userId) {
        try {
            UserProfileDto profile = userProfileFeignClient.getUserProfile(userId);
            return profile.isGeoLocationEnabled();
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found: " + userId);
        } catch (FeignException e) {
            log.warn("Profile service unavailable for user {}, assuming geo enabled: {}", userId, e.getMessage());
            return true;
        }
    }
}
