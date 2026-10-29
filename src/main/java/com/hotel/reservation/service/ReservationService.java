
package com.hotel.reservation.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;
import com.hotel.user.model.User;
import com.hotel.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        validateDates(reservation);

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + reservation.getUserId()));

        if (!user.isActive()) {
            throw new ValidationException(
                    "An inactive user cannot make a reservation.");
        }

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + reservation.getRoomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new ValidationException(
                    "This room is not operational for booking.");
        }

        if (reservation.getGuestCount() > room.getCapacity()) {
            throw new ValidationException(
                    "Guest count exceeds room capacity.");
        }

        ensureNoOverlap(reservation, null);

        return reservationRepository.create(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + id));
    }

    public Reservation updateReservation(
            String id, Reservation updated) {
        getReservationById(id);
        validateDates(updated);

        User user = userRepository.findById(updated.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + updated.getUserId()));

        if (!user.isActive()) {
            throw new ValidationException(
                    "An inactive user cannot hold a reservation.");
        }

        Room room = roomRepository.findById(updated.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + updated.getRoomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new ValidationException(
                    "This room is not operational for booking.");
        }

        if (updated.getGuestCount() > room.getCapacity()) {
            throw new ValidationException(
                    "Guest count exceeds room capacity.");
        }

        ensureNoOverlap(updated, id);
        return reservationRepository.update(id, updated);
    }

    public Reservation cancelReservation(String id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ValidationException(
                    "This reservation cannot be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.update(id, reservation);
    }

    private void ensureNoOverlap(
            Reservation candidate, String reservationIdBeingUpdated) {

        boolean overlap = reservationRepository.findAll().stream()
                .filter(existing ->
                        reservationIdBeingUpdated == null
                                || !existing.getId().equals(
                                reservationIdBeingUpdated))
                .filter(existing ->
                        existing.getRoomId().equals(
                                candidate.getRoomId()))
                .filter(existing ->
                        existing.getStatus() == ReservationStatus.PENDING
                                || existing.getStatus()
                                == ReservationStatus.CONFIRMED)
                .anyMatch(existing ->
                        candidate.getCheckInDate().isBefore(
                                existing.getCheckOutDate())
                                && candidate.getCheckOutDate().isAfter(
                                existing.getCheckInDate()));

        if (overlap) {
            throw new ValidationException(
                    "The room is already reserved for some "
                            + "or all of those dates.");
        }
    }

    private void validateDates(Reservation reservation) {
        if (reservation == null
                || reservation.getCheckInDate() == null
                || reservation.getCheckOutDate() == null
                || reservation.getGuestCount() <= 0
                || reservation.getUserId() == null
                || reservation.getRoomId() == null
                || reservation.getStatus() == null) {
            throw new ValidationException(
                    "Required reservation information is missing.");
        }

        LocalDate checkIn = reservation.getCheckInDate();
        LocalDate checkOut = reservation.getCheckOutDate();

        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException(
                    "Check-out must be after check-in.");
        }
    }
}