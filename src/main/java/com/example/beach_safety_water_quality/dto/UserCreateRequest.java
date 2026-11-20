package com.example.beach_safety_water_quality.dto;

import com.example.beach_safety_water_quality.model.Role;

public class UserCreateRequest {
    private String username;
    private String password;
    private Role role;
    private Boolean enabled = true;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UserCreateRequest(String username, String password, Role role, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled != null ? enabled : true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}