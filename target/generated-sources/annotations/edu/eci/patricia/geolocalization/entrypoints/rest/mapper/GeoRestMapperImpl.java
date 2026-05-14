package edu.eci.patricia.geolocalization.entrypoints.rest.mapper;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.entrypoints.rest.request.UpdateLocationRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-14T16:34:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class GeoRestMapperImpl implements GeoRestMapper {

    @Override
    public UpdateLocationRequestDto toDto(UpdateLocationRequest request) {
        if ( request == null ) {
            return null;
        }

        double latitude = 0.0d;
        double longitude = 0.0d;
        Double accuracy = null;
        String campusZone = null;

        if ( request.getLatitude() != null ) {
            latitude = request.getLatitude();
        }
        if ( request.getLongitude() != null ) {
            longitude = request.getLongitude();
        }
        accuracy = request.getAccuracy();
        campusZone = request.getCampusZone();

        UpdateLocationRequestDto updateLocationRequestDto = new UpdateLocationRequestDto( latitude, longitude, accuracy, campusZone );

        return updateLocationRequestDto;
    }
}
