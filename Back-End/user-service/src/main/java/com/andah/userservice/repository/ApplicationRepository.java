package com.andah.userservice.repository;

import com.andah.userservice.model.Application;
import com.andah.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    Optional<Application> findByUser(User user);
    Optional<Application> findByUserId(String userId);
    List<Application> findByStatus(Application.Status status);
    boolean existsByUser(User user);
    boolean existsByUserId(String userId);
}
