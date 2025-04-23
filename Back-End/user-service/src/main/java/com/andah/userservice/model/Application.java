package com.andah.userservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.*;

@Document(collection = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    private String userId;  // Store the user ID separately for easy querying
    
    private String applicationLetter;
    
    private Status status;
    
    private String remarks;
    
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
