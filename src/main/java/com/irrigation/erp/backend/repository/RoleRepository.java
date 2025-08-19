package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> { // Changed from Integer to Long
    Role findByName(String name);
}