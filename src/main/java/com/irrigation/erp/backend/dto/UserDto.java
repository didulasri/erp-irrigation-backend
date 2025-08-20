package com.irrigation.erp.backend.dto;

import com.irrigation.erp.backend.model.User; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String roleName;
    private Boolean isActive;
    private String branch;

    // Constructor for simplified display (name, email, role only)
    public UserDto(Long id, String firstName, String lastName, String email, String roleName, String branch) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleName = roleName;
        this.branch = branch;
    }

    // Method to get full name
    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (lastName != null ? " " + lastName : "");
    }
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getContactNumber(),
                user.getRole() != null ? user.getRole().getName() : "N/A",
                user.getIsActive(),
                user.getBranch()
        );
    }
}