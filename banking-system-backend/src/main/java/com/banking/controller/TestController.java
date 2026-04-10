package com.banking.controller;

import com.banking.model.User;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            long userCount = userRepository.count();
            response.put("status", "SUCCESS");
            response.put("message", "Database connection is working!");
            response.put("totalUsers", userCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "FAILURE");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/save-user")
    public ResponseEntity<Map<String, Object>> saveTestUser() {
        Map<String, Object> response = new HashMap<>();
        try {
            User testUser = new User();
            testUser.setName("Test User");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setRole("USER");

            User saved = userRepository.save(testUser);

            response.put("status", "SUCCESS");
            response.put("message", "Test user saved successfully!");
            response.put("userId", saved.getId());
            response.put("userName", saved.getName());
            response.put("userEmail", saved.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "FAILURE");
            response.put("message", "Failed to save test user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
