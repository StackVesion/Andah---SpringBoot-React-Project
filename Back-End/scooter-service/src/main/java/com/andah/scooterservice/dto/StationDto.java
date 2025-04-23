package com.andah.scooterservice.dto;

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
    private Long id;
    private String name;
    private String location;
    private String description;
    private Double balance;
    private List<String> pictures;
    private Long ownerId;
}
