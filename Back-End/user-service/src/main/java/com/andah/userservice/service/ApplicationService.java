package com.andah.userservice.service;

import com.andah.userservice.dto.ApplicationDto;
import com.andah.userservice.model.Application;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.ApplicationRepository;
import com.andah.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<ApplicationDto> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ApplicationDto> getPendingApplications() {
        return applicationRepository.findByStatus(Application.Status.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ApplicationDto getApplicationById(String id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application not found with id: " + id));
        return mapToDto(application);
    }

    public ApplicationDto getApplicationByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
        
        Application application = applicationRepository.findByUser(user)
                .orElseGet(() -> applicationRepository.findByUserId(userId)
                    .orElseThrow(() -> new NoSuchElementException("Application not found for user with id: " + userId)));
        
        return mapToDto(application);
    }

    public ApplicationDto createApplication(String userId, String applicationLetter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
        
        if (applicationRepository.existsByUser(user) || applicationRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User already has an application");
        }
        
        Application application = Application.builder()
            .user(user)
            .userId(userId)
            .applicationLetter(applicationLetter)
            .status(Application.Status.PENDING)
            .build();
        
        Application savedApplication = applicationRepository.save(application);
        return mapToDto(savedApplication);
    }

    public ApplicationDto approveApplication(String id, String remarks) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application not found with id: " + id));
        
        application.setStatus(Application.Status.APPROVED);
        application.setRemarks(remarks);
        Application approvedApplication = applicationRepository.save(application);
        
        // Promote user to STATION_OWNER
        String userId = application.getUser() != null ? application.getUser().getId() : application.getUserId();
        userService.promoteToStationOwner(userId);
        
        return mapToDto(approvedApplication);
    }

    public ApplicationDto rejectApplication(String id, String remarks) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application not found with id: " + id));
        
        application.setStatus(Application.Status.REJECTED);
        application.setRemarks(remarks);
        Application rejectedApplication = applicationRepository.save(application);
        
        return mapToDto(rejectedApplication);
    }
    
    private ApplicationDto mapToDto(Application application) {
        User user = application.getUser();
        String userId = user != null ? user.getId() : application.getUserId();
        String userName = user != null ? user.getName() : "Unknown";
        String userEmail = user != null ? user.getEmail() : "Unknown";
        
        return ApplicationDto.builder()
                .id(application.getId())
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .applicationLetter(application.getApplicationLetter())
                .status(application.getStatus())
                .remarks(application.getRemarks())
                .build();
    }
}
