package com.hotel.user.service;

import com.hotel.common.ValidationException;
import com.hotel.user.dto.RegisterRequest;
import com.hotel.user.dto.UserResponse;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import com.hotel.user.Security.PasswordHasher;

import java.util.UUID;

public class AuthenticationService {

    private final UserService userService;

    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    // Register a new customer account
    public UserResponse register(RegisterRequest request) {

        if (request == null) {
            throw new ValidationException(
                    "Registration information is required.");
        }

        // Validate full name
        if (request.getFullName() == null ||
                request.getFullName().isBlank()) {
            throw new ValidationException(
                    "Full name is required.");
        }

        // Validate email
        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {
            throw new ValidationException(
                    "Email address is required.");
        }

        // Validate password
        String password = request.getPassword();

        if (password == null || password.length() < 12) {
            throw new ValidationException(
                    "Password must contain at least 12 characters.");
        }

        if (password.length() > 1024) {
            throw new ValidationException(
                    "Password is too long.");
        }

        // Generate a unique user ID
        String userId = UUID.randomUUID().toString();

        // Convert the plaintext password into a secure hash
        String passwordHash =
                PasswordHasher.hashPassword(password);

        // Create a customer account
        User user = new User(
                userId,
                request.getFullName().trim(),
                request.getEmail().trim(),
                passwordHash,
                UserRole.CUSTOMER,
                request.getPhone() == null
                        ? ""
                        : request.getPhone().trim(),
                true
        );

        // UserService checks duplicate emails
        // and UserRepository stores the user in JSON
        User createdUser = userService.createUser(user);

        // Never expose the password hash
        return new UserResponse(createdUser);
    }
}
