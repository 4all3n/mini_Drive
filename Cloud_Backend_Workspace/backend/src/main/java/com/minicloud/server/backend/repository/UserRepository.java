package com.minicloud.server.backend.repository;

import com.minicloud.server.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Boot automatically writes the SQL to find a user by their username!
    Optional<User> findByUsername(String username);
}