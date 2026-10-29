
package com.hotel.user.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;
import com.hotel.user.model.User;

import java.util.List;

public class UserRepository extends JsonCrudRepository<User> {

    public UserRepository() {
        super(
                new JsonFileStorage<>(
                        "users.json",
                        new TypeReference<List<User>>() {}
                ),
                User::getId
        );
    }

    @Override
    protected void validate(User user) {
        if (user == null) {
            throw new ValidationException(
                    "User cannot be null."
            );
        }

        if (isBlank(user.getId())
                || isBlank(user.getFullName())
                || isBlank(user.getEmail())
                || isBlank(user.getPasswordHash())
                || user.getRole() == null) {
            throw new ValidationException(
                    "User ID, name, email, password hash, and role are required."
            );
        }

        if (!user.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException(
                    "Please provide a valid email address."
            );
        }

        // Do not store a plain-text password in passwordHash.
        if (user.getPasswordHash().length() < 20) {
            throw new ValidationException(
                    "Password hash does not look valid."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}