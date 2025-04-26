package com.andah.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from API Gateway");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/auth")
    public ResponseEntity<?> auth() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "You are authenticated!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
