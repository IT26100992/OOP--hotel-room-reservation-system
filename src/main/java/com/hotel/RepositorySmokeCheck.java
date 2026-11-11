
package com.hotel;

import com.hotel.common.ValidationException;
import com.hotel.payment.model.*;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.reservation.model.*;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.*;
import com.hotel.room.repository.RoomRepository;
import com.hotel.user.model.*;
import com.hotel.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class RepositorySmokeCheck {

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        String suffix = UUID.randomUUID().toString();

        UserRepository users = new UserRepository();
        RoomRepository rooms = new RoomRepository();
        ReservationRepository reservations =
                new ReservationRepository();
        PaymentRepository payments = new PaymentRepository();

        String userId = "TEST-USER-" + suffix;
        String roomId = "TEST-ROOM-" + suffix;
        String reservationId = "TEST-RES-" + suffix;
        String paymentId = "TEST-PAY-" + suffix;

        try {
            // USER: Create, read, update
            User user = new User(
                    userId, "Test User",
                    "test-" + suffix + "@example.com",
                    "$argon2id$example-hash-for-testing-only",
                    UserRole.CUSTOMER, "0710000000", true
            );

            users.create(user);
            check(users.findById(userId).isPresent(),
                    "User create/read failed.");

            user.setFullName("Updated Test User");
            users.update(userId, user);
            check(users.findById(userId).get()
                            .getFullName().equals("Updated Test User"),
                    "User update failed.");

            // ROOM: Create, read, update
            Room room = new Room(
                    roomId, "TEST-" + suffix,
                    RoomType.DOUBLE, new BigDecimal("12500.00"),
                    2, RoomStatus.AVAILABLE, "Test room"
            );

            rooms.create(room);
            check(rooms.findById(roomId).isPresent(),
                    "Room create/read failed.");

            room.setPricePerNight(new BigDecimal("13000.00"));
            rooms.update(roomId, room);
            check(rooms.findById(roomId).get()
                            .getPricePerNight()
                            .compareTo(new BigDecimal("13000.00")) == 0,
                    "Room update failed.");

            // RESERVATION: Create, read, update
            Reservation reservation = new Reservation(
                    reservationId, userId, roomId,
                    LocalDate.parse("2026-11-12"),
                    LocalDate.parse("2026-11-15"),
                    2, ReservationStatus.PENDING,
                    LocalDateTime.now()
            );

            reservations.create(reservation);
            check(reservations.findById(reservationId).isPresent(),
                    "Reservation create/read failed.");

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservations.update(reservationId, reservation);
            check(reservations.findById(reservationId).get()
                            .getStatus() == ReservationStatus.CONFIRMED,
                    "Reservation update failed.");

            // PAYMENT: Create, read, update
            Payment payment = new Payment(
                    paymentId, reservationId,
                    new BigDecimal("25000.00"),
                    PaymentMethod.BANK_TRANSFER,
                    PaymentStatus.PENDING, null,
                    "TEST-RECEIPT-" + suffix
            );

            payments.create(payment);
            check(payments.findById(paymentId).isPresent(),
                    "Payment create/read failed.");

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
            payments.update(paymentId, payment);
            check(payments.findById(paymentId).get()
                            .getStatus() == PaymentStatus.COMPLETED,
                    "Payment update failed.");

            // Validation: invalid room capacity must be rejected
            Room invalidRoom = new Room(
                    "INVALID-" + suffix, "INVALID-" + suffix,
                    RoomType.SINGLE, new BigDecimal("1000.00"),
                    0, RoomStatus.AVAILABLE, "Invalid test"
            );

            boolean rejected = false;
            try {
                rooms.create(invalidRoom);
            } catch (ValidationException exception) {
                rejected = true;
            }

            check(rejected, "Invalid room was not rejected.");

            System.out.println(
                    "CRUD and validation checks passed."
            );

        } finally {
            // Remove test records so repeated runs remain clean.
            payments.deleteById(paymentId);
            reservations.deleteById(reservationId);
            rooms.deleteById(roomId);
            users.deleteById(userId);
        }
    }
}