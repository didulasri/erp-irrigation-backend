package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.LoginRequest;
import com.irrigation.erp.backend.dto.LoginResponse;
import com.irrigation.erp.backend.dto.RoleResponseDTO;
import com.irrigation.erp.backend.dto.UserDto;
import com.irrigation.erp.backend.model.Role;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.RoleRepository;
import com.irrigation.erp.backend.repository.UserRepository;
import com.irrigation.erp.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                return new LoginResponse("Invalid email or password", false);
            }

            User user = userOptional.get();

            // Check if user is active
            if (!user.getIsActive()) {
                return new LoginResponse("Account is deactivated", false);
            }

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return new LoginResponse("Invalid email or password", false);
            }

            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT token
            String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getUsername(),
                    roleName,
                    user.getId()
            );

            // Convert to DTO (excluding sensitive information)
            UserDto userDto = convertToDto(user);

            // Create response with token
            LoginResponse response = new LoginResponse("Login successful", true, userDto);
            response.setToken(token);

            return response;

        } catch (Exception e) {
            return new LoginResponse("Login failed: " + e.getMessage(), false);
        }
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

        // Set role name if role exists
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }

    // Method to check if user must change password
    public boolean mustChangePassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(User::getMustChangePassword).orElse(false);
    }

    public List<RoleResponseDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToRoleDto)
                .toList();
    }

    private RoleResponseDTO convertToRoleDto(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }
}
