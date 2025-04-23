package com.andah.scooterservice.service;

import com.andah.scooterservice.dto.ScooterDto;
import com.andah.scooterservice.model.Scooter;
import com.andah.scooterservice.repository.ScooterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScooterService {

    @Autowired
    private ScooterRepository scooterRepository;

    public List<Scooter> getAllScooters() {
        return scooterRepository.findAll();
    }

    public Optional<Scooter> getScooterById(String id) {
        return scooterRepository.findById(id);
    }

    @Autowired
    private EmailService emailService;  // Injection du service Email
    public Scooter createScooter(Scooter scooter) {
        Scooter savedScooter = scooterRepository.save(scooter);

        // Envoyer l'email après la création
        String ownerEmail = "Haythem.raggad@esprit.tn";
        emailService.sendScooterCreationEmail(
                ownerEmail,
                savedScooter.getName(),
                savedScooter.getDescription(),
                savedScooter.getPrice(),
                savedScooter.getStatus().toString() // Convertir le statut en String
        );

        return savedScooter;
    }


    public Scooter updateScooter(String id, Scooter scooterDetails) {
        return scooterRepository.findById(id)
                .map(scooter -> {
                    scooter.setName(scooterDetails.getName());
                    scooter.setDescription(scooterDetails.getDescription());
                    scooter.setPrice(scooterDetails.getPrice());
                    scooter.setStatus(scooterDetails.getStatus());
                    scooter.setPictures(scooterDetails.getPictures());
                    scooter.setOwnerId(scooterDetails.getOwnerId());
                    return scooterRepository.save(scooter);
                })
                .orElseGet(() -> {
                    scooterDetails.setId(id);
                    return scooterRepository.save(scooterDetails);
                });
    }

    public void deleteScooter(String id) {
        scooterRepository.deleteById(id);
    }

}