package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.repository.UserRepository;
import com.minicloud.server.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User newUser) {
        // Check if username already exists
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }
        
        // Hash password with BCrypt before saving
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // Save to database (password is now a $2a$10$... hash)
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        Optional<User> dbUser = userRepository.findByUsername(loginRequest.getUsername());
        
        // Verify password using BCrypt matching (compares raw input against stored hash)
        if (dbUser.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), dbUser.get().getPassword())) {
            // Generate JWT token for the authenticated user
            String token = jwtTokenProvider.generateToken(loginRequest.getUsername());

            // Return token in JSON response
            return ResponseEntity.ok(Map.of(
                "token", token,
                "username", loginRequest.getUsername(),
                "message", "Login successful"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}