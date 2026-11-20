package com.example.beach_safety_water_quality.dto;

import com.example.beach_safety_water_quality.model.Role;
import com.example.beach_safety_water_quality.model.User;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, Role role, Boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public static UserResponse fromUser(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEnabled(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}