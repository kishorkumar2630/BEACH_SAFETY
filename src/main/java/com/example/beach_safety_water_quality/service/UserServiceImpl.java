package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.dto.UserCreateRequest;
import com.example.beach_safety_water_quality.dto.UserResponse;
import com.example.beach_safety_water_quality.dto.UserUpdateRequest;
import com.example.beach_safety_water_quality.model.Role;
import com.example.beach_safety_water_quality.model.User;
import com.example.beach_safety_water_quality.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
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
        user.setRole(request.getRole() != null ? request.getRole() : Role.VIEWER);
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        User savedUser = userRepo.save(user);
        return UserResponse.fromUser(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        User updatedUser = userRepo.save(user);
        return UserResponse.fromUser(updatedUser);
    }

    @Override
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        user.setEnabled(enabled);
        User updatedUser = userRepo.save(user);
        return UserResponse.fromUser(updatedUser);
    }

    @Override
    public UserResponse changeRole(Long id, Role role) {
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role cannot be null");
        }
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        user.setRole(role);
        User updatedUser = userRepo.save(user);
        return UserResponse.fromUser(updatedUser);
    }

    @Override
    public void deleteUser(Long id, String currentUsername) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        if (currentUsername != null && user.getUsername().equalsIgnoreCase(currentUsername.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete currently authenticated user");
        }

        userRepo.delete(user);
    }
}