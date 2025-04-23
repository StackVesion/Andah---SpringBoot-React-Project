package com.andah.stationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    private String id; // MongoDB usually uses String/ObjectId for _id

    private String name;
    private String location;
    private String description;
    private Double balance;

    private List<String> pictures = new ArrayList<>();

    private Long ownerId;
    private String ownerEmail;
    @Transient
    private List<Long> scooterIds = new ArrayList<>();

    @Transient
    private Double averageRating;

    @Transient
    private Integer totalRatings;


}
