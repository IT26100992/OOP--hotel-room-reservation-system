package com.hotel.admin.service;

import com.hotel.admin.dto.DashboardSummary;
import com.hotel.admin.dto.GenerateReportRequest;
import com.hotel.admin.dto.UpdateReportRequest;
import com.hotel.admin.exception.InvalidReportDateRangeException;
import com.hotel.admin.exception.ReportNotFoundException;
import com.hotel.admin.model.Report;
import com.hotel.admin.repository.ReportRepository;

import com.hotel.payment.model.Payment;
import com.hotel.payment.model.PaymentStatus;
import com.hotel.payment.repository.PaymentRepository;

import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;

import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;

import com.hotel.user.repository.UserRepository;

import com.hotel.common.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

class AdminReportServiceTest {

    private RoomRepository roomRepository;
    private UserRepository userRepository;
    private ReservationRepository reservationRepository;
    private PaymentRepository paymentRepository;
    private ReportRepository reportRepository;

    private AdminReportService service;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        userRepository = mock(UserRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        reportRepository = mock(ReportRepository.class);

        service = new AdminReportService(
                roomRepository,
                userRepository,
                reservationRepository,
                paymentRepository,
                reportRepository
        );
    }

    @Test
    void generateReport_invalidDateRange_throwsException() {

        GenerateReportRequest request =
                new GenerateReportRequest(
                        "Invalid Report",
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 11, 1),
                        "Testing invalid dates"
                );

        assertThrows(
                InvalidReportDateRangeException.class,
                () -> service.generateReport(request)
        );

        verifyNoInteractions(reportRepository);
    }



    @Test
    void generateReport_blankTitle_throwsValidationException() {

        GenerateReportRequest request =
                new GenerateReportRequest(
                        "   ",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 30),
                        "Testing blank title"
                );

        assertThrows(
                ValidationException.class,
                () -> service.generateReport(request)
        );

        verifyNoInteractions(reportRepository);
    }


    @Test
    void getReportById_missingReport_throwsException() {

        when(reportRepository.findById("INVALID-ID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> service.getReportById("INVALID-ID")
        );

        verify(reportRepository).findById("INVALID-ID");
    }



    // Add the test methods below.
}