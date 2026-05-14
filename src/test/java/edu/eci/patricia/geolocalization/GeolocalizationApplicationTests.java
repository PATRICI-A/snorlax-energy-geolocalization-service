package edu.eci.patricia.geolocalization;

import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.repository.LocationMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class GeolocalizationApplicationTests {

    @MockitoBean
    LocationMongoRepository locationMongoRepository;

    @MockitoBean
    MongoTemplate mongoTemplate;

    @MockitoBean
    ConnectionFactory connectionFactory;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
    }
}
