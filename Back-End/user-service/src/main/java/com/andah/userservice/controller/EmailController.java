package com.andah.userservice.controller;

import com.andah.userservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) {
        logger.info("Received request to send email to: {}", request.getTo());
        Map<String, String> response = new HashMap<>();
        
        try {
            emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getText());
            response.put("message", "Email sent successfully to " + request.getTo());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            response.put("error", "Failed to send email");
            response.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    public static class EmailRequest {
        private String to;
        private String subject;
        private String text;
        
        // Getters and setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
