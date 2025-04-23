package com.andah.userservice.service;

import com.andah.userservice.dto.UserDto;
import com.andah.userservice.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    
    List<UserDto> getAllUsers();
    
    UserDto getUserById(String id);
    
    UserDto getUserByEmail(String email);
    
    UserDto createUser(User user);
    
    UserDto updateUser(String id, User userDetails);
    
    void deleteUser(String id);
    
    UserDto verifyUser(String id);
    
    UserDto promoteToStationOwner(String id);
    
    Map<String, Object> getUserProfile(String username);
    
    // Nouvelle méthode pour sauvegarder un utilisateur
    User saveUser(User user);
}
