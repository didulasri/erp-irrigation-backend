package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.UserDto;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll(); // Only get active users
        return users.stream()
                .map(this::convertToDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    public List<UserDto> searchUsersByName(String name) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrue(name, name);
        return users.stream()
                .map(this::convertToDto)
                .toList();
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setContactNumber(user.getContactNumber());
        dto.setIsActive(user.getIsActive());

        // Set role name
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        dto.setBranch(user.getBranch());

        return dto;
    }
}