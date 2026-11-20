package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.dto.AuthResponse;
import com.example.beach_safety_water_quality.dto.LoginRequest;
import com.example.beach_safety_water_quality.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}