package com.andah.scooterservice.client;

import com.andah.scooterservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceClient {
    @GetMapping("/api/users/{userId}")
    ResponseEntity<?> getUserById(@PathVariable("userId") String userId);
    
    @GetMapping("/api/users/me")
    ResponseEntity<?> getCurrentUser();
}
