package com.andah.stationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDto {
    private String id;
    private String name;
    private String location;
    private String description;
    private Double balance;
    private List<String> pictures;
    private Long ownerId;
    private String ownerName; // From user-service
    private String ownerEmail;
    private List<Long> scooterIds; // From scooter-service
    private Double averageRating; // From rating-service
    private Integer totalRatings; // From rating-service
}
