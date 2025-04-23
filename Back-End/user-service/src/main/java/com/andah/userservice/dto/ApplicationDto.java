package com.andah.userservice.dto;

import com.andah.userservice.model.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String applicationLetter;
    private Application.Status status;
    private String remarks;
}
