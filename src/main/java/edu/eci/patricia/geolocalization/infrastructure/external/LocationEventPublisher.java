package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.infrastructure.external.dto.LocationUpdatedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationEventPublisher {

    private static final String GEO_EXCHANGE = "geo.exchange";
    private static final String LOCATION_UPDATED_KEY = "geo.location.updated";

    private final RabbitTemplate rabbitTemplate;

    public void publishLocationUpdated(Location location) {
        LocationUpdatedEventDto event = new LocationUpdatedEventDto(
                location.getUserId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCampusZone(),
                location.getUpdatedAt()
        );
        try {
            rabbitTemplate.convertAndSend(GEO_EXCHANGE, LOCATION_UPDATED_KEY, event);
            log.debug("Location event published for user {}", location.getUserId());
        } catch (Exception e) {
            log.warn("Failed to publish location event for user {}: {}", location.getUserId(), e.getMessage());
        }
    }
}
