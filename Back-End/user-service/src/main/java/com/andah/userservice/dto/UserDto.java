package com.andah.userservice.dto;

import com.andah.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean isVerified;
    private User.Role role;
    private List<String> reservationIds;
    private List<String> ratingIds;
}
