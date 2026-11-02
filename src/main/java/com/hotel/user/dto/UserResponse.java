package com.hotel.user.dto;

import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;

public class UserResponse {

    private final String id;
    private final String fullName;
    private final String email;
    private final UserRole role;
    private final String phone;
    private final boolean active;

    public UserResponse(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.phone = user.getPhone();
        this.active = user.isActive();
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }
}
