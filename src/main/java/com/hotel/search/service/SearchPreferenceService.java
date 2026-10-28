
package com.hotel.search.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.search.model.SearchPreference;
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class SearchPreferenceService {

    private final SearchPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public SearchPreferenceService(
            SearchPreferenceRepository preferenceRepository,
            UserRepository userRepository) {

        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    // CREATE - Save a new search preference
    public SearchPreference createPreference(
            SearchPreference preference) {

        if (preference == null) {
            throw new ValidationException(
                    "Search preference is required.");
        }

        validateUser(preference.getUserId());

        // Generate a unique ID for each saved search
        preference.setId(UUID.randomUUID().toString());

        return preferenceRepository.create(preference);
    }

    // READ - Get all preferences belonging to one user
    public List<SearchPreference> getPreferencesByUser(
            String userId) {

        validateUser(userId);

        return preferenceRepository.findAll().stream()
                .filter(preference ->
                        userId.equals(preference.getUserId()))
                .toList();
    }

    // READ - Get one preference owned by a user
    public SearchPreference getPreferenceById(
            String id, String userId) {

        validateUser(userId);

        SearchPreference preference =
                preferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Search preference not found."));

        checkOwnership(preference, userId);

        return preference;
    }

    // UPDATE - Modify an existing preference
    public SearchPreference updatePreference(
            String id,
            String userId,
            SearchPreference updatedPreference) {

        SearchPreference existing =
                getPreferenceById(id, userId);

        if (updatedPreference == null) {
            throw new ValidationException(
                    "Updated search preference is required.");
        }

        // Keep the original ID and owner
        existing.setCheckInDate(
                updatedPreference.getCheckInDate());
        existing.setCheckOutDate(
                updatedPreference.getCheckOutDate());
        existing.setGuestCount(
                updatedPreference.getGuestCount());
        existing.setRoomType(
                updatedPreference.getRoomType());
        existing.setMinPrice(
                updatedPreference.getMinPrice());
        existing.setMaxPrice(
                updatedPreference.getMaxPrice());

        return preferenceRepository.update(id, existing);
    }

    // DELETE - Remove a saved search
    public boolean deletePreference(
            String id, String userId) {

        getPreferenceById(id, userId);

        return preferenceRepository.deleteById(id);
    }

    // Check whether the referenced user exists
    private void validateUser(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new ValidationException(
                    "User ID is required.");
        }

        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "User not found.");
        }
    }

    // Check whether the preference belongs to the user
    private void checkOwnership(
            SearchPreference preference,
            String userId) {

        if (!userId.equals(preference.getUserId())) {
            throw new ResourceNotFoundException(
                    "Search preference not found.");
        }
    }
}
