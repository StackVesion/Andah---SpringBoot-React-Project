package com.andah.userservice.service.impl;

import com.andah.userservice.dto.UserDto;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.UserRepository;
import com.andah.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    // Private fields
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor with @Autowired explicitly added
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        System.out.println("Initializing UserServiceImpl...");
        System.out.println("PasswordEncoder bean: " + (passwordEncoder != null ? "available" : "null"));
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        return mapToDto(user);
    }

    @Override
    public UserDto createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user); // Ensure this line is being executed
        return mapToDto(savedUser);
    }

    @Override
    public UserDto updateUser(String id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        
        user.setName(userDetails.getName());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        
        // Only update password if it's provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        userRepository.delete(user);
    }
    
    @Override
    public UserDto verifyUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        user.setVerified(true);
        User verifiedUser = userRepository.save(user);
        return mapToDto(verifiedUser);
    }
    
    @Override
    public UserDto promoteToStationOwner(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        user.setRole(User.Role.STATION_OWNER);
        User promotedUser = userRepository.save(user);
        return mapToDto(promotedUser);
    }
    
    @Override
    public Map<String, Object> getUserProfile(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + username));
        
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("id", user.getId());
        userProfile.put("name", user.getName());
        userProfile.put("firstName", user.getFirstName());
        userProfile.put("lastName", user.getLastName());
        userProfile.put("email", user.getEmail());
        userProfile.put("phoneNumber", user.getPhoneNumber());
        userProfile.put("isVerified", user.isVerified());
        userProfile.put("role", user.getRole());
        userProfile.put("keycloakId", user.getKeycloakId());
        
        return userProfile;
    }
    
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isVerified(user.isVerified())
                .role(user.getRole())
                .reservationIds(user.getReservationIds())
                .ratingIds(user.getRatingIds())
                .build();
    }
}
