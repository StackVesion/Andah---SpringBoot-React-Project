package com.andah.stationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStationRequest {
    @NotBlank(message = "Station name is required")
    private String name;
    
    @NotBlank(message = "Station location is required")
    private String location;
    
    private String description;
    
    private List<String> pictures;
    private String ownerEmail;
}
