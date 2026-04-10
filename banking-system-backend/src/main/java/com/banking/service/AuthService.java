package com.banking.service;

import com.banking.config.JwtUtil;
import com.banking.dto.AuthResponse;
import com.banking.dto.LoginRequest;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(false, "Email already exists", null, null, null, null, null);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        String token = generateTokenForUser(savedUser);

        return new AuthResponse(
                true,
                "Registration successful",
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = generateTokenForUser(user);
                        return new AuthResponse(
                                true,
                                "Login successful",
                                token,
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole()
                        );
                    } else {
                        return new AuthResponse(false, "Invalid password", null, null, null, null, null);
                    }
                })
                .orElse(new AuthResponse(false, "User not found with email: " + request.getEmail(), null, null, null, null, null));
    }

    private String generateTokenForUser(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        return jwtUtil.generateToken(user.getEmail(), claims);
    }
}
