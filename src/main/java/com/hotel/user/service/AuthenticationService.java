package com.hotel.user.service;

import com.hotel.common.ValidationException;
import com.hotel.user.dto.RegisterRequest;
import com.hotel.user.dto.UserResponse;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import com.hotel.user.security.PasswordHasher;
import com.hotel.user.dto.LoginRequest;
import com.hotel.user.repository.UserRepository;
import java.util.Locale;
import com.hotel.user.exception.InvalidCredentialsException;

import java.util.UUID;

public class AuthenticationService {


    private final UserService userService;
    private final UserRepository userRepository;

    public AuthenticationService(
            UserService userService,
            UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
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

    public User login(LoginRequest request) {

        if (request == null ||
                request.getEmail() == null ||
                request.getEmail().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().isEmpty()) {

            throw new ValidationException(
                    "Email and password are required.");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find the customer by email
        User user = userRepository.findAll().stream()
                .filter(existing ->
                        existing.getEmail() != null &&
                                existing.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        // Return the same error for invalid email or password
        if (user == null ||
                !PasswordHasher.verifyPassword(
                        request.getPassword(),
                        user.getPasswordHash())) {

            throw new InvalidCredentialsException();
        }

        // A deactivated account cannot log in
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

}
