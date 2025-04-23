package com.andah.scooterservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScooterDto {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String status;
    private String pictures;
    private Long ownerId;
    private Double averageRating;
    private Integer totalRatings;
}
