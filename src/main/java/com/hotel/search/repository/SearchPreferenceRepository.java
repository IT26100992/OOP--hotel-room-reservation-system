
package com.hotel.search.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;
import com.hotel.search.model.SearchPreference;

import java.util.List;

public class SearchPreferenceRepository
        extends JsonCrudRepository<SearchPreference> {

    public SearchPreferenceRepository() {

        super(
                new JsonFileStorage<>(
                        "search-preferences.json",
                        new TypeReference<List<SearchPreference>>() {}
                ),
                SearchPreference::getId
        );
    }

    @Override
    protected void validate(SearchPreference preference) {

        if (preference == null) {
            throw new ValidationException(
                    "Search preference is required.");
        }

        if (preference.getId() == null ||
                preference.getId().isBlank()) {
            throw new ValidationException(
                    "Search preference ID is required.");
        }

        if (preference.getUserId() == null ||
                preference.getUserId().isBlank()) {
            throw new ValidationException(
                    "User ID is required.");
        }

        if (preference.getCheckInDate() == null ||
                preference.getCheckOutDate() == null) {
            throw new ValidationException(
                    "Check-in and check-out dates are required.");
        }

        if (!preference.getCheckOutDate()
                .isAfter(preference.getCheckInDate())) {
            throw new ValidationException(
                    "Check-out must be after check-in.");
        }

        if (preference.getGuestCount() <= 0) {
            throw new ValidationException(
                    "Guest count must be greater than zero.");
        }

        if (preference.getMinPrice() != null &&
                preference.getMinPrice().signum() < 0) {
            throw new ValidationException(
                    "Minimum price cannot be negative.");
        }

        if (preference.getMaxPrice() != null &&
                preference.getMaxPrice().signum() < 0) {
            throw new ValidationException(
                    "Maximum price cannot be negative.");
        }

        if (preference.getMinPrice() != null &&
                preference.getMaxPrice() != null &&
                preference.getMinPrice()
                        .compareTo(preference.getMaxPrice()) > 0) {

            throw new ValidationException(
                    "Minimum price cannot exceed maximum price.");
        }
    }
}
