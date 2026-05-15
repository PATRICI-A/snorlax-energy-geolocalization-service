package edu.eci.patricia.geolocalization.infrastructure.adapters.adapter;

import edu.eci.patricia.geolocalization.domain.model.Location;
import edu.eci.patricia.geolocalization.domain.ports.out.LocationRepositoryPort;
import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.entity.LocationDocument;
import edu.eci.patricia.geolocalization.infrastructure.adapters.persistence.repository.LocationMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocationRepositoryAdapter implements LocationRepositoryPort {

    private final LocationMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Location save(Location location) {
        Optional<LocationDocument> existing = mongoRepository.findByUserId(location.getUserId());
        LocationDocument doc = existing.orElse(LocationDocument.builder()
                .userId(location.getUserId())
                .build());

        doc.setCoordinates(new GeoJsonPoint(location.getLongitude(), location.getLatitude()));
        doc.setCampusZone(location.getCampusZone());
        doc.setAccuracy(location.getAccuracy());
        doc.setLowPrecision(location.isLowPrecision());
        doc.setUpdatedAt(location.getUpdatedAt() != null ? location.getUpdatedAt() : LocalDateTime.now());

        LocationDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<Location> findByUserId(String userId) {
        return mongoRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<Location> findNearby(double latitude, double longitude, double radiusMeters) {
        NearQuery nearQuery = NearQuery
                .near(new Point(longitude, latitude))
                .maxDistance(new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS))
                .spherical(true);

        GeoResults<LocationDocument> results = mongoTemplate.geoNear(nearQuery, LocationDocument.class);

        return results.getContent().stream()
                .map(gr -> toDomain(gr.getContent()))
                .toList();
    }

    @Override
    public List<Location> findNearbyActive(double latitude, double longitude, double radiusMeters, LocalDateTime activeSince) {
        NearQuery nearQuery = NearQuery
                .near(new Point(longitude, latitude))
                .maxDistance(new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS))
                .spherical(true)
                .query(Query.query(Criteria.where("updatedAt").gte(activeSince)));

        GeoResults<LocationDocument> results = mongoTemplate.geoNear(nearQuery, LocationDocument.class);

        return results.getContent().stream()
                .map(gr -> toDomain(gr.getContent()))
                .toList();
    }

    @Override
    public List<Location> findAllActive(LocalDateTime activeSince) {
        Query query = Query.query(Criteria.where("updatedAt").gte(activeSince));
        return mongoTemplate.find(query, LocationDocument.class).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByUserId(String userId) {
        mongoRepository.deleteByUserId(userId);
    }

    private Location toDomain(LocationDocument doc) {
        double lat = doc.getCoordinates() != null ? doc.getCoordinates().getY() : 0;
        double lon = doc.getCoordinates() != null ? doc.getCoordinates().getX() : 0;
        Location location = new Location(
                doc.getId(), doc.getUserId(), lat, lon,
                doc.getCampusZone(), doc.getAccuracy(), doc.getUpdatedAt());
        location.setLowPrecision(doc.isLowPrecision());
        return location;
    }
}
