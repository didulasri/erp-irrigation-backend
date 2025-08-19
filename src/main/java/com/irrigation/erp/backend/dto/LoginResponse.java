package com.irrigation.erp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private boolean success;
    private UserDto user;
    private String token; // Optional: if you plan to use JWT tokens later

    // Constructor for successful login
    public LoginResponse(String message, boolean success, UserDto user) {
        this.message = message;
        this.success = success;
        this.user = user;
    }

    // Constructor for failed login
    public LoginResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}