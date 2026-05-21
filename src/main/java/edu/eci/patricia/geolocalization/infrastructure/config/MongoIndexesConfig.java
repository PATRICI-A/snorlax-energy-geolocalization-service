package edu.eci.patricia.geolocalization.infrastructure.config;

import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.entity.LocationDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MongoIndexesConfig {

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        mongoTemplate.indexOps(LocationDocument.class)
                .createIndex(new Index()
                        .on("updatedAt", Sort.Direction.ASC)
                        .expire(Duration.ofDays(1)));
    }
}
