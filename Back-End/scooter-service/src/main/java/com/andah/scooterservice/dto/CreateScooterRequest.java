package com.andah.scooterservice.dto;

import lombok.Data;

@Data
public class CreateScooterRequest {
    private String name;
    private String description;
    private Double price;
    private String status;
    private String pictures;
}
