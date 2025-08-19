package com.irrigation.erp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique=true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "contact_number")
    private String contactNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name= "is_Active",nullable = false)
    private  Boolean isActive=true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = true;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(nullable = true)  
    private String branch;

    // public User(String username, String password, String email, String firstName, String lastName, String contactNumber, Role role) {
    //     this.username = username;
    //     this.password = password;
    //     this.email = email;
    //     this.firstName = firstName;
    //     this.lastName = lastName;
    //     this.contactNumber = contactNumber;
    //     this.role = role;


    // }



}
