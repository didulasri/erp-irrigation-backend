package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // You can add custom queries if needed
    Role findByName(String name);
}
