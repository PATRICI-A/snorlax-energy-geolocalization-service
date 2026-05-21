package edu.eci.patricia.geolocalization.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParceDto {
    private UUID id;
    private String name;
    private String description;
    private String place;
    private String category;
    private String type;
    private String status;
    private int maximumQuota;
    private int actualMembers;
    private UUID captainId;
    private LocalDate date;
    private LocalTime hour;
    private Double latitude;
    private Double longitude;
}
