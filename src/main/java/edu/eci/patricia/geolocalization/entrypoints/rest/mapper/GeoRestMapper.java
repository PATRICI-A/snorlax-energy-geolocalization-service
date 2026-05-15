package edu.eci.patricia.geolocalization.entrypoints.rest.mapper;

import edu.eci.patricia.geolocalization.application.dto.request.UpdateLocationRequestDto;
import edu.eci.patricia.geolocalization.entrypoints.rest.request.UpdateLocationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeoRestMapper {

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "accuracy", target = "accuracy")
    @Mapping(source = "campusZone", target = "campusZone")
    @Mapping(source = "timestamp", target = "timestamp")
    UpdateLocationRequestDto toDto(UpdateLocationRequest request);
}
