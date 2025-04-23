package com.andah.scooterservice.controller;

import com.andah.scooterservice.dto.ScooterDto;
import com.andah.scooterservice.model.Scooter;
import com.andah.scooterservice.service.ScooterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.andah.scooterservice.dto.CreateScooterRequest;
import com.andah.scooterservice.dto.ScooterDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scooters")
public class ScooterController {

    @Autowired
    private ScooterService scooterService;

    @GetMapping
    public List<Scooter> getAllScooters() {
        return scooterService.getAllScooters();
    }

    @GetMapping("/{id}")
    public Optional<Scooter> getScooterById(@PathVariable String id) {
        return scooterService.getScooterById(id);
    }

    @PostMapping
    public Scooter createScooter(@RequestBody Scooter scooter) {
        return scooterService.createScooter(scooter);
    }

    @PutMapping("/{id}")
    public Scooter updateScooter(@PathVariable String id, @RequestBody Scooter scooter) {
        return scooterService.updateScooter(id, scooter);
    }

    @DeleteMapping("/{id}")
    public void deleteScooter(@PathVariable String id) {
        scooterService.deleteScooter(id);
    }}
