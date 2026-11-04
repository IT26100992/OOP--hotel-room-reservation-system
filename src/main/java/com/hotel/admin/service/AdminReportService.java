package com.hotel.admin.service;

import com.hotel.admin.dto.DashboardSummary;

import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;

import com.hotel.user.model.User;
import com.hotel.user.repository.UserRepository;

import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;

import com.hotel.payment.model.Payment;
import com.hotel.payment.model.PaymentStatus;
import com.hotel.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;

    public class AdminReportService {

        private final RoomRepository roomRepository;
        private final UserRepository userRepository;
        private final ReservationRepository reservationRepository;
        private final PaymentRepository paymentRepository;

        public AdminReportService(
                RoomRepository roomRepository,
                UserRepository userRepository,
                ReservationRepository reservationRepository,
                PaymentRepository paymentRepository) {

            this.roomRepository = roomRepository;
            this.userRepository = userRepository;
            this.reservationRepository = reservationRepository;
            this.paymentRepository = paymentRepository;
        }

        // Calculate current dashboard statistics
        public DashboardSummary getDashboardSummary() {

            DashboardSummary summary = new DashboardSummary();

            // Retrieve data from shared repositories
            List<Room> rooms = roomRepository.findAll();
            List<User> users = userRepository.findAll();
            List<Reservation> reservations =
                    reservationRepository.findAll();
            List<Payment> payments =
                    paymentRepository.findAll();

            // ==================================
            // Room Statistics
            // ==================================

            summary.setTotalRooms(rooms.size());

            summary.setAvailableRooms((int) rooms.stream()
                    .filter(room ->
                            room.getStatus() == RoomStatus.AVAILABLE)
                    .count());

            summary.setMaintenanceRooms((int) rooms.stream()
                    .filter(room ->
                            room.getStatus() == RoomStatus.MAINTENANCE)
                    .count());

            summary.setInactiveRooms((int) rooms.stream()
                    .filter(room ->
                            room.getStatus() == RoomStatus.INACTIVE)
                    .count());

            // ==================================
            // User Statistics
            // ==================================

            summary.setTotalUsers(users.size());

            summary.setActiveUsers((int) users.stream()
                    .filter(User::isActive)
                    .count());

            // ==================================
            // Reservation Statistics
            // ==================================

            summary.setTotalReservations(reservations.size());

            summary.setPendingReservations(
                    countReservationsByStatus(
                            reservations,
                            ReservationStatus.PENDING));

            summary.setConfirmedReservations(
                    countReservationsByStatus(
                            reservations,
                            ReservationStatus.CONFIRMED));

            summary.setCancelledReservations(
                    countReservationsByStatus(
                            reservations,
                            ReservationStatus.CANCELLED));

            summary.setCompletedReservations(
                    countReservationsByStatus(
                            reservations,
                            ReservationStatus.COMPLETED));

            // ==================================
            // Payment Statistics
            // ==================================

            summary.setTotalPayments(payments.size());

            summary.setCompletedPayments((int) payments.stream()
                    .filter(payment ->
                            payment.getStatus() == PaymentStatus.COMPLETED)
                    .count());

            summary.setRefundedPayments((int) payments.stream()
                    .filter(payment ->
                            payment.getStatus() == PaymentStatus.REFUNDED)
                    .count());

            BigDecimal grossCompletedPayments = payments.stream()
                    .filter(payment ->
                            payment.getStatus() == PaymentStatus.COMPLETED)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summary.setGrossCompletedPayments(
                    grossCompletedPayments);

            return summary;
        }

        // Reusable helper for reservation counts
        private int countReservationsByStatus(
                List<Reservation> reservations,
                ReservationStatus status) {

            return (int) reservations.stream()
                    .filter(reservation ->
                            reservation.getStatus() == status)
                    .count();
        }
    }

