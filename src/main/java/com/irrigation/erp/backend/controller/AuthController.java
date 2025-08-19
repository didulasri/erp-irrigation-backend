package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.LoginRequest;
import com.irrigation.erp.backend.dto.LoginResponse;
import com.irrigation.erp.backend.dto.RoleResponseDTO;
import com.irrigation.erp.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Validate input
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse("Email is required", false));
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse("Password is required", false));
        }

        // Authenticate user
        LoginResponse response = authService.authenticateUser(loginRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout() {
        // For now, just return success
        // Later you can implement JWT token invalidation here
        return ResponseEntity.ok(new LoginResponse("Logout successful", true));
    }

    @GetMapping("/check-password-change/{email}")
    public ResponseEntity<Boolean> checkPasswordChange(@PathVariable String email) {
        boolean mustChange = authService.mustChangePassword(email);
        return ResponseEntity.ok(mustChange);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        List<RoleResponseDTO> roles = authService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}