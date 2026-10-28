
package com.hotel.reservation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;
import com.hotel.reservation.model.Reservation;

import java.util.List;

public class ReservationRepository
        extends JsonCrudRepository<Reservation> {

    public ReservationRepository() {
        super(
                new JsonFileStorage<>(
                        "reservations.json",
                        new TypeReference<List<Reservation>>() {}
                ),
                Reservation::getId
        );
    }

    @Override
    protected void validate(Reservation reservation) {
        if (reservation == null) {
            throw new ValidationException(
                    "Reservation cannot be null."
            );
        }

        if (isBlank(reservation.getId())
                || isBlank(reservation.getUserId())
                || isBlank(reservation.getRoomId())
                || reservation.getCheckInDate() == null
                || reservation.getCheckOutDate() == null
                || reservation.getStatus() == null
                || reservation.getCreatedAt() == null) {
            throw new ValidationException(
                    "Reservation is missing required fields."
            );
        }

        if (!reservation.getCheckOutDate()
                .isAfter(reservation.getCheckInDate())) {
            throw new ValidationException(
                    "Check-out must be after check-in."
            );
        }

        if (reservation.getGuestCount() <= 0) {
            throw new ValidationException(
                    "Guest count must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}