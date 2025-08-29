package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);
  // Find active users only
  List<User> findAll();

  // Search by first name or last name containing the search term (case insensitive) for active users only
  @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
          "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
          "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
  List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrue(
          @Param("firstName") String firstName, @Param("lastName") String lastName);

  // Custom query to search by full name for active users only
  @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
          "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
  List<User> findByFullNameContainingAndIsActiveTrue(@Param("name") String name);
}
