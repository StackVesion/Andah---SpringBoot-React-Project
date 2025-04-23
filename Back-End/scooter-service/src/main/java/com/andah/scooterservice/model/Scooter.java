package com.andah.scooterservice.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "scooters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scooter {

    @Id
    private String id;

    private String name;
    private String description;
    private Double price;

    @Enumerated(EnumType.STRING)
    private Status status;

    private List<String> pictures = new ArrayList<>();
    private Long ownerId;

    @Transient
    private Double averageRating;

    @Transient
    private Integer totalRatings;

    // Liste des IDs des scooters associés
    @Transient
    private List<String> scooterIds = new ArrayList<>();

    // Ajout de la méthode getter pour scooterIds
    public List<String> getScooterIds() {
        return scooterIds;
    }

    public void setScooterIds(List<String> scooterIds) {
        this.scooterIds = scooterIds;
    }
}
