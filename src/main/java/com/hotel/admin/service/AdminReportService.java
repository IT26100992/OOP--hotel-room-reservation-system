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

import com.hotel.admin.dto.GenerateReportRequest;
import com.hotel.admin.dto.UpdateReportRequest;
import com.hotel.admin.model.Report;
import com.hotel.admin.repository.ReportRepository;
import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.admin.exception.InvalidReportDateRangeException;
import com.hotel.admin.exception.ReportNotFoundException;


import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

import java.math.BigDecimal;
import java.util.List;

public class AdminReportService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReportRepository reportRepository;



    public AdminReportService(
            RoomRepository roomRepository,
            UserRepository userRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            ReportRepository reportRepository) {

        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.reportRepository = reportRepository;
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
                .filter(payment ->
                        payment.getAmount() != null)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        summary.setGrossCompletedPayments(
                grossCompletedPayments);

        return summary;
    }


    public Report generateReport(GenerateReportRequest request) {

        if (request == null) {
            throw new ValidationException(
                    "Report information is required.");
        }

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new ValidationException(
                    "Report title is required.");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new ValidationException(
                    "Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new InvalidReportDateRangeException();
        }

        // Count reservations with check-in dates in this period
        int reservationCount = (int)
                reservationRepository.findAll().stream()
                        .filter(reservation ->
                                reservation.getCheckInDate() != null)
                        .filter(reservation -> {
                            LocalDate date =
                                    reservation.getCheckInDate();

                            return !date.isBefore(startDate)
                                    && !date.isAfter(endDate);
                        })
                        .count();

        // Sum gross completed payments dated in this period
        BigDecimal completedAmount =
                paymentRepository.findAll().stream()
                        .filter(payment ->
                                payment.getStatus() ==
                                        PaymentStatus.COMPLETED)
                        .filter(payment ->
                                payment.getPaidAt() != null)
                        .filter(payment ->
                                payment.getAmount() != null)
                        .filter(payment -> {
                            LocalDate date =
                                    payment.getPaidAt().toLocalDate();

                            return !date.isBefore(startDate)
                                    && !date.isAfter(endDate);
                        })
                        .map(Payment::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // Create a snapshot of the results
        Report report = new Report(
                UUID.randomUUID().toString(),
                request.getTitle().trim(),
                startDate,
                endDate,
                LocalDateTime.now(),
                roomRepository.findAll().size(),
                reservationCount,
                completedAmount,
                request.getNotes() == null
                        ? ""
                        : request.getNotes().trim()
        );

        return reportRepository.create(report);
    }


    // READ - Get all saved reports
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // READ - Find one report by its ID

    public Report getReportById(String id) {

        return reportRepository.findById(id)
                .orElseThrow(() ->
                        new ReportNotFoundException(id));
    }


    // UPDATE - Change only the title and notes
    public Report updateReport(
            String id,
            UpdateReportRequest request) {

        if (request == null ||
                request.getTitle() == null ||
                request.getTitle().isBlank()) {

            throw new ValidationException(
                    "Report title is required.");
        }

        Report existing = getReportById(id);

        existing.setTitle(request.getTitle().trim());

        existing.setNotes(
                request.getNotes() == null
                        ? ""
                        : request.getNotes().trim()
        );

        // Preserve calculated totals, dates and original ID
        return reportRepository.update(id, existing);
    }

    // DELETE - Remove a saved report
    public boolean deleteReport(String id) {

        getReportById(id);

        return reportRepository.deleteById(id);
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

