package edu.eci.patricia.geolocalization;

import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.repository.LocationMongoRepository;
import edu.eci.patricia.geolocalization.infrastructure.external.CampusEventsFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.ParcheFeignClient;
import edu.eci.patricia.geolocalization.infrastructure.external.UserProfileFeignClient;
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

    @MockitoBean
    UserProfileFeignClient userProfileFeignClient;

    @MockitoBean
    ParcheFeignClient parcheFeignClient;

    @MockitoBean
    CampusEventsFeignClient campusEventsFeignClient;

    @Test
    void contextLoads() {
        // verifies the Spring context starts without errors
    }
}
