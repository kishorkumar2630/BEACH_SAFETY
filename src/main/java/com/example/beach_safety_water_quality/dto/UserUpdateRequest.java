package com.example.beach_safety_water_quality.dto;

import com.example.beach_safety_water_quality.model.Role;

public class UserUpdateRequest {
    private Role role;
    private Boolean enabled;
    private String password;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(Role role, Boolean enabled) {
        this.role = role;
        this.enabled = enabled;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}