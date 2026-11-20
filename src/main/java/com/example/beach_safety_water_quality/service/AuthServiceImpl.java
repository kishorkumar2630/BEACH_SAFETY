package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.dto.AuthResponse;
import com.example.beach_safety_water_quality.dto.LoginRequest;
import com.example.beach_safety_water_quality.dto.RegisterRequest;
import com.example.beach_safety_water_quality.model.Role;
import com.example.beach_safety_water_quality.model.User;
import com.example.beach_safety_water_quality.repo.UserRepo;
import com.example.beach_safety_water_quality.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        String username = request.getUsername().trim();
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.VIEWER);
        user.setEnabled(true);

        User savedUser = userRepo.save(user);

        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepo.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}