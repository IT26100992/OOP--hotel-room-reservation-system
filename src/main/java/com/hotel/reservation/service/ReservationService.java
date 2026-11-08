
package com.hotel.reservation.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.algorithm.ReservationQuickSort;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import com.hotel.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    // -----------------------------------------
    // CREATE RESERVATION
    // -----------------------------------------

    public Reservation createReservation(Reservation reservation) {

        if (reservation == null) {
            throw new ValidationException(
                    "Reservation cannot be null.");
        }

        // Generate reservation information on the backend.
        reservation.setId(UUID.randomUUID().toString());

        reservation.setStatus(ReservationStatus.PENDING);

        reservation.setCreatedAt(LocalDateTime.now());

        validateDates(reservation);

        // Validate customer.
        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + reservation.getUserId()));

        validateCustomer(user);

        // Validate room.
        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + reservation.getRoomId()));

        validateRoom(room, reservation);

        // Prevent double booking.
        ensureNoOverlap(reservation, null);

        return reservationRepository.create(reservation);
    }

    // -----------------------------------------
    // GET ALL RESERVATIONS
    // -----------------------------------------

    public List<Reservation> getAllReservations() {

        List<Reservation> reservations =
                reservationRepository.findAll();

        ReservationQuickSort.sortByCheckInDate(reservations);

        return reservations;
    }

    // -----------------------------------------
    // GET RESERVATION BY ID
    // -----------------------------------------

    public Reservation getReservationById(String id) {

        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + id));
    }

    // -----------------------------------------
    // UPDATE RESERVATION
    // -----------------------------------------

    public Reservation updateReservation(
            String id,
            Reservation updated) {

        if (updated == null) {
            throw new ValidationException(
                    "Updated reservation cannot be null.");
        }

        // Get existing reservation.
        Reservation existing = getReservationById(id);

        if (existing.getStatus() == ReservationStatus.CANCELLED
                || existing.getStatus() == ReservationStatus.COMPLETED) {

            throw new ValidationException(
                    "Cancelled or completed reservations cannot be updated.");
        }

        // Preserve backend-managed information.
        updated.setId(existing.getId());

        updated.setCreatedAt(existing.getCreatedAt());

        updated.setStatus(existing.getStatus());

        validateDates(updated);

        // Validate customer.
        User user = userRepository.findById(updated.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + updated.getUserId()));

        validateCustomer(user);

        // Validate room.
        Room room = roomRepository.findById(updated.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + updated.getRoomId()));

        validateRoom(room, updated);

        // Exclude the current reservation from overlap checking.
        ensureNoOverlap(updated, id);

        return reservationRepository.update(id, updated);
    }

    // -----------------------------------------
    // CANCEL RESERVATION
    // -----------------------------------------

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

    // -----------------------------------------
    // CUSTOMER VALIDATION
    // -----------------------------------------

    private void validateCustomer(User user) {

        if (!user.isActive()) {
            throw new ValidationException(
                    "An inactive user cannot make or hold a reservation.");
        }

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ValidationException(
                    "Only customers can make reservations.");
        }
    }

    // -----------------------------------------
    // ROOM VALIDATION
    // -----------------------------------------

    private void validateRoom(
            Room room,
            Reservation reservation) {

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new ValidationException(
                    "This room is not operational for booking.");
        }

        if (reservation.getGuestCount() > room.getCapacity()) {
            throw new ValidationException(
                    "Guest count exceeds room capacity.");
        }
    }

    // -----------------------------------------
    // DOUBLE BOOKING PREVENTION
    // -----------------------------------------

    private void ensureNoOverlap(
            Reservation candidate,
            String reservationIdBeingUpdated) {

        boolean overlap = reservationRepository.findAll().stream()

                .filter(existing -> existing != null)

                .filter(existing ->
                        reservationIdBeingUpdated == null
                                || !Objects.equals(
                                existing.getId(),
                                reservationIdBeingUpdated))

                .filter(existing ->
                        Objects.equals(
                                existing.getRoomId(),
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

    // -----------------------------------------
    // DATE AND FIELD VALIDATION
    // -----------------------------------------

    private void validateDates(Reservation reservation) {

        if (reservation == null
                || reservation.getCheckInDate() == null
                || reservation.getCheckOutDate() == null
                || reservation.getGuestCount() <= 0
                || reservation.getUserId() == null
                || reservation.getUserId().isBlank()
                || reservation.getRoomId() == null
                || reservation.getRoomId().isBlank()) {

            throw new ValidationException(
                    "Required reservation information is missing.");
        }

        LocalDate checkIn = reservation.getCheckInDate();

        LocalDate checkOut = reservation.getCheckOutDate();

        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException(
                    "Check-out must be after check-in.");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException(
                    "Check-in date cannot be in the past.");
        }
    }
}
