package com.andah.stationservice.controller;

import com.andah.stationservice.dto.CreateOwnerRequest;
import com.andah.stationservice.dto.CreateStationRequest;
import com.andah.stationservice.dto.StationDto;
import com.andah.stationservice.dto.UserDto;
import com.andah.stationservice.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<List<StationDto>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationDto> getStationById(@PathVariable String id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<StationDto>> getStationsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(stationService.getStationsByOwnerId(ownerId));
    }

    @PostMapping("/owner/{ownerId}")
    public ResponseEntity<StationDto> createStation(
            @Valid @RequestBody CreateStationRequest request,
            @PathVariable Long ownerId) {
        return new ResponseEntity<>(stationService.createStation(request, ownerId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/owner/{ownerId}")
    public ResponseEntity<StationDto> updateStation(
            @PathVariable String id,
            @Valid @RequestBody CreateStationRequest request,
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(stationService.updateStation(id, request, ownerId));
    }

    @DeleteMapping("/{id}/owner/{ownerId}")
    public ResponseEntity<Void> deleteStation(
            @PathVariable String id,
            @PathVariable Long ownerId) {
        stationService.deleteStation(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    // This endpoint would typically be called by other services
    @PutMapping("/{id}/balance")
    public ResponseEntity<StationDto> updateBalance(
            @PathVariable String id,
            @RequestParam Double amount) {
        return ResponseEntity.ok(stationService.updateStationBalance(id, amount));
    }
    
    // Endpoint to find stations for scooter-service
    @GetMapping("/internal/{id}")
    public ResponseEntity<StationDto> getStationForInternalUse(@PathVariable String id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }

}
