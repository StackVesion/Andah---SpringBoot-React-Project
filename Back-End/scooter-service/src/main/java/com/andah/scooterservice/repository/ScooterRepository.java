package com.andah.scooterservice.repository;

import com.andah.scooterservice.model.Scooter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScooterRepository extends MongoRepository<Scooter, String> {

}
