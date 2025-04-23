package com.andah.stationservice.service;

import com.andah.stationservice.client.UserServiceClient;
import com.andah.stationservice.dto.CreateOwnerRequest;
import com.andah.stationservice.dto.CreateStationRequest;
import com.andah.stationservice.dto.StationDto;
import com.andah.stationservice.dto.UserDto;
import com.andah.stationservice.model.Station;
import com.andah.stationservice.repository.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationRepository stationRepository;
    private final UserServiceClient userServiceClient;

    public List<StationDto> getAllStations() {
        return stationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public StationDto getStationById(String id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        return mapToDto(station);
    }

    public List<StationDto> getStationsByOwnerId(Long ownerId) {
        return stationRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Autowired
    private EmailService emailService;

    public StationDto createStation(CreateStationRequest request, Long ownerId) {

        // TEMPORARY: Skip user verification for testing
/*
    try {
        UserDto user = userServiceClient.getUserById(ownerId).getBody();
        if (user == null || !user.getRole().equals("STATION_OWNER")) {
            throw new IllegalStateException("User is not authorized to create a station");
        }
    } catch (Exception e) {
        log.error("Error communicating with user-service: {}", e.getMessage());
        throw new IllegalStateException("Cannot verify user authorization");
    }
*/
        //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
        // Get user email from the user service
        try {
        // Assuming the UserDto has an email field
        } catch (Exception e) {
            log.error("Error fetching user email: {}", e.getMessage());
            // Continue with station creation even if we can't get the email
        }
        //aaaaaaaaaaaaaaaaaaaaaa

        // Check if station with the same name already exists for this owner
        if (stationRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new IllegalStateException("Station with this name already exists for this owner");
        }

        Station station = new Station();
        station.setName(request.getName());
        station.setLocation(request.getLocation());
        station.setDescription(request.getDescription());
        station.setPictures(request.getPictures());
        station.setOwnerId(ownerId);
        station.setOwnerEmail(request.getOwnerEmail());
        station.setBalance(0.0);

        Station savedStation = stationRepository.save(station);

// aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

        String ownerEmaill = request.getOwnerEmail();
        // Send email notification if we have the owner's email
        try {
            emailService.sendStationCreationEmail(
                    ownerEmaill,
                    savedStation.getName(),
                    savedStation.getLocation()
            );
        } catch (Exception e) {
            // Log but don't fail the operation
            log.error("Failed to send email notification: {}", e.getMessage());
        }


        return mapToDto(savedStation);
    }

    public StationDto updateStation(String id, CreateStationRequest request, Long ownerId) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));

        // Verify ownership
        if (!station.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("User is not authorized to update this station");
        }

        station.setName(request.getName());
        station.setLocation(request.getLocation());
        station.setDescription(request.getDescription());
        
        // Only update pictures if they are provided
        if (request.getPictures() != null && !request.getPictures().isEmpty()) {
            station.setPictures(request.getPictures());
        }

        Station updatedStation = stationRepository.save(station);
        return mapToDto(updatedStation);
    }

    public void deleteStation(String id, Long ownerId) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));

        // Verify ownership
        if (!station.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("User is not authorized to delete this station");
        }

        stationRepository.delete(station);
    }

    public StationDto updateStationBalance(String id, Double amount) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        
        station.setBalance(station.getBalance() + amount);
        Station updatedStation = stationRepository.save(station);
        
        return mapToDto(updatedStation);
    }

    private StationDto mapToDto(Station station) {
        StationDto.StationDtoBuilder builder = StationDto.builder()
                .id(station.getId())
                .name(station.getName())
                .location(station.getLocation())
                .description(station.getDescription())
                .balance(station.getBalance())
                .pictures(station.getPictures())
                .ownerId(station.getOwnerId())
                .scooterIds(station.getScooterIds());

        // Try to get owner name from user-service
        try {
            UserDto owner = userServiceClient.getUserById(station.getOwnerId()).getBody();
            if (owner != null) {
                builder.ownerName(owner.getName());
            }
        } catch (Exception e) {
            log.warn("Could not fetch owner details for station {}: {}", station.getId(), e.getMessage());
        }

        // Add average rating and total ratings - in a real implementation, 
        // these would come from the rating-service
        builder.averageRating(station.getAverageRating());
        builder.totalRatings(station.getTotalRatings());

        return builder.build();
    }
}
