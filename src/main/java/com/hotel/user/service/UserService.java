
package com.hotel.user.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.user.model.User;
import com.hotel.user.repository.UserRepository;
import com.hotel.user.dto.UpdateProfileRequest;
import com.hotel.user.dto.UserResponse;

import java.util.List;
import java.util.Locale;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        validateUser(user);

        boolean emailExists = userRepository.findAll().stream()
                .anyMatch(existing ->
                        existing.getEmail().equalsIgnoreCase(
                                user.getEmail().trim()));

        if (emailExists) {
            throw new ValidationException(
                    "An account with this email already exists.");
        }

        user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        return userRepository.create(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id));
    }

    public User updateUser(String id, User updatedUser) {
        getUserById(id);
        validateUser(updatedUser);

        boolean emailUsedByAnotherUser =
                userRepository.findAll().stream()
                        .anyMatch(existing ->
                                !existing.getId().equals(id)
                                        && existing.getEmail().equalsIgnoreCase(
                                        updatedUser.getEmail().trim()));

        if (emailUsedByAnotherUser) {
            throw new ValidationException(
                    "Another account already uses this email.");
        }

        updatedUser.setEmail(
                updatedUser.getEmail().trim().toLowerCase(Locale.ROOT));

        return userRepository.update(id, updatedUser);
    }

    // Deactivation is safer than deleting an account's history.
    public User deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        return userRepository.update(id, user);
    }

    private void validateUser(User user) {
        if (user == null
                || blank(user.getId())
                || blank(user.getFullName())
                || blank(user.getEmail())
                || blank(user.getPasswordHash())
                || user.getRole() == null) {
            throw new ValidationException(
                    "Required user information is missing.");
        }

        if (!user.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException(
                    "Invalid email address.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }


    public UserResponse updateOwnProfile(
            String authenticatedUserId,
            UpdateProfileRequest request) {

        if (request == null) {
            throw new ValidationException(
                    "Profile information is required.");
        }

        // Load the account identified by the authenticated session
        User existing = getUserById(authenticatedUserId);

        if (!existing.isActive()) {
            throw new ValidationException(
                    "This account is deactivated.");
        }

        if (request.getFullName() == null ||
                request.getFullName().isBlank()) {
            throw new ValidationException(
                    "Full name is required.");
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {
            throw new ValidationException(
                    "Email address is required.");
        }

        // Build an updated user while preserving protected fields
        User updated = new User(
                existing.getId(),
                request.getFullName().trim(),
                request.getEmail().trim(),
                existing.getPasswordHash(),
                existing.getRole(),
                request.getPhone() == null
                        ? ""
                        : request.getPhone().trim(),
                existing.isActive()
        );

        // Reuse existing validation and duplicate-email checks
        User saved = updateUser(
                authenticatedUserId,
                updated
        );

        return new UserResponse(saved);
    }


}
