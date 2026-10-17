
package com.hotel.search.service;

import com.hotel.algorithm.RoomAvailabilityBST;
import com.hotel.common.ValidationException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;

public class RoomAvailabilityService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomAvailabilityService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Room> searchAvailableRooms(
            LocalDate checkIn,
            LocalDate checkOut,
            int guestCount) {

        if (checkIn == null || checkOut == null) {
            throw new ValidationException(
                    "Check-in and check-out dates are required.");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException(
                    "Check-out must be after check-in.");
        }

        if (guestCount <= 0) {
            throw new ValidationException(
                    "Guest count must be greater than zero.");
        }

        List<Reservation> reservations =
                reservationRepository.findAll();

        RoomAvailabilityBST tree = new RoomAvailabilityBST();

        for (Room room : roomRepository.findAll()) {
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                continue;
            }

            if (room.getCapacity() < guestCount) {
                continue;
            }

            boolean hasConflict = reservations.stream()
                    .filter(reservation ->
                            reservation.getRoomId().equals(room.getId()))
                    .filter(reservation ->
                            reservation.getStatus()
                                    == ReservationStatus.PENDING
                                    || reservation.getStatus()
                                    == ReservationStatus.CONFIRMED)
                    .anyMatch(reservation ->
                            checkIn.isBefore(
                                    reservation.getCheckOutDate())
                                    && checkOut.isAfter(
                                    reservation.getCheckInDate()));

            if (!hasConflict) {
                tree.insert(room);
            }
        }

        return tree.inOrderTraversal();
    }
}