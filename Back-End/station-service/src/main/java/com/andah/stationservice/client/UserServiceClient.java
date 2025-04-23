package com.andah.stationservice.client;

import com.andah.stationservice.dto.CreateOwnerRequest;
import com.andah.stationservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users/owners")
    ResponseEntity<UserDto> createOwner(@RequestBody CreateOwnerRequest request);
}
