package edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.repository;

import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.entity.LocationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LocationMongoRepository extends MongoRepository<LocationDocument, String> {
    Optional<LocationDocument> findByUserId(String userId);
    void deleteByUserId(String userId);
}
