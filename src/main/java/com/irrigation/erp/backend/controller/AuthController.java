package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.LoginRequest;
import com.irrigation.erp.backend.dto.LoginResponse;
import com.irrigation.erp.backend.dto.RoleResponseDTO;
import com.irrigation.erp.backend.service.AuthService;
import com.irrigation.erp.backend.util.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

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
        // For JWT, you can implement token blacklisting here if needed
        return ResponseEntity.ok(new LoginResponse("Logout successful", true));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.getEmailFromToken(token);
                boolean isValid = jwtUtil.validateToken(token, email);
                return ResponseEntity.ok(isValid);
            }
            return ResponseEntity.ok(false);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
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

