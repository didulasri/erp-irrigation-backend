package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.UserDto;
import com.irrigation.erp.backend.dto.UserStatusDTO;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.model.Role;
import com.irrigation.erp.backend.service.UserService;
import com.irrigation.erp.backend.repository.UserRepository;
import com.irrigation.erp.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Support both React dev ports
public class UserController {

    @Autowired
    private UserService userService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all users (using service layer)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID (using service layer)
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Search users by name (using service layer)
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String name) {
        List<UserDto> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    // Add new user (direct repository access for user creation)
    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        // Set role
        Role role = roleRepository.findByName(user.getRole().getName());
        user.setRole(role);

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Delete user (direct repository access)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // toggle status
    @PutMapping("/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusDTO  statusDTO) {

        return userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(statusDTO.getIsActive());
                    userRepository.save(user);
                    return ResponseEntity.ok(UserDto.fromEntity(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    user.setBranch(updatedUser.getBranch());

                    // Update role properly
                    if (updatedUser.getRole() != null && updatedUser.getRole().getName() != null) {
                        Role role = roleRepository.findByName(updatedUser.getRole().getName());
                        user.setRole(role);
                    }

                    userRepository.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}