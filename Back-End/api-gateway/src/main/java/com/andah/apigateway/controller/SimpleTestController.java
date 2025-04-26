package com.andah.apigateway.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class SimpleTestController {

    @GetMapping("/simple-test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simple test controller is working!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/simple-users")
    public Map<String, Object> users() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "1");
        user1.put("email", "user1@example.com");
        user1.put("name", "Test User 1");
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "2");
        user2.put("email", "user2@example.com");
        user2.put("name", "Test User 2");
        
        users.add(user1);
        users.add(user2);
        
        response.put("users", users);
        response.put("count", users.size());
        response.put("timestamp", new Date().toString());
        
        return response;
    }
    
    @GetMapping("/stations-test")
    public Map<String, Object> stations() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> stations = new ArrayList<>();
        
        Map<String, Object> station1 = new HashMap<>();
        station1.put("id", "1");
        station1.put("name", "Central Station");
        station1.put("capacity", 20);
        
        Map<String, Object> station2 = new HashMap<>();
        station2.put("id", "2");
        station2.put("name", "North Station");
        station2.put("capacity", 15);
        
        stations.add(station1);
        stations.add(station2);
        
        response.put("stations", stations);
        response.put("count", stations.size());
        
        return response;
    }
}
