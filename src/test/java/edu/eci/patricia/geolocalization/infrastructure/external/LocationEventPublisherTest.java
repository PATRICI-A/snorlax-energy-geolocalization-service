package edu.eci.patricia.geolocalization.infrastructure.external;

import edu.eci.patricia.geolocalization.domain.model.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LocationEventPublisher publisher;

    @Test
    void publishLocationUpdated_success_sendsToRabbit() {
        Location loc = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());

        publisher.publishLocationUpdated(loc);

        verify(rabbitTemplate).convertAndSend(eq("geo.exchange"), eq("geo.location.updated"), any(Object.class));
    }

    @Test
    void publishLocationUpdated_rabbitThrows_doesNotPropagateException() {
        Location loc = new Location("id1", "user-1", 4.628, -74.064, "Bloque A", 10.0, LocalDateTime.now());
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        // Should not throw
        publisher.publishLocationUpdated(loc);
    }
}
