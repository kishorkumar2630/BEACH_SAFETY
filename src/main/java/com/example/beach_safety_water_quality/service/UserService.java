package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.dto.UserCreateRequest;
import com.example.beach_safety_water_quality.dto.UserResponse;
import com.example.beach_safety_water_quality.dto.UserUpdateRequest;
import com.example.beach_safety_water_quality.model.Role;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse setEnabled(Long id, boolean enabled);
    UserResponse changeRole(Long id, Role role);
    void deleteUser(Long id, String currentUsername);
}