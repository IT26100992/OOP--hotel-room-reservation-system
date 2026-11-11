
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


    @Test
    void getDashboardSummary_countsRoomStatusesCorrectly() {

        Room room1 = new Room();
        room1.setStatus(RoomStatus.AVAILABLE);

        Room room2 = new Room();
        room2.setStatus(RoomStatus.AVAILABLE);

        Room room3 = new Room();
        room3.setStatus(RoomStatus.AVAILABLE);

        Room room4 = new Room();
        room4.setStatus(RoomStatus.MAINTENANCE);

        Room room5 = new Room();
        room5.setStatus(RoomStatus.INACTIVE);

        when(roomRepository.findAll())
                .thenReturn(List.of(
                        room1, room2, room3, room4, room5
                ));

        when(userRepository.findAll())
                .thenReturn(List.of());

        when(reservationRepository.findAll())
                .thenReturn(List.of());

        when(paymentRepository.findAll())
                .thenReturn(List.of());

        DashboardSummary summary =
                service.getDashboardSummary();

        assertEquals(5, summary.getTotalRooms());
        assertEquals(3, summary.getAvailableRooms());
        assertEquals(1, summary.getMaintenanceRooms());
        assertEquals(1, summary.getInactiveRooms());
    }


    @Test
    void getDashboardSummary_countsReservationStatusesCorrectly() {

        Reservation reservation1 = new Reservation();
        reservation1.setStatus(ReservationStatus.PENDING);

        Reservation reservation2 = new Reservation();
        reservation2.setStatus(ReservationStatus.CONFIRMED);

        Reservation reservation3 = new Reservation();
        reservation3.setStatus(ReservationStatus.CONFIRMED);

        Reservation reservation4 = new Reservation();
        reservation4.setStatus(ReservationStatus.CANCELLED);

        Reservation reservation5 = new Reservation();
        reservation5.setStatus(ReservationStatus.COMPLETED);

        when(roomRepository.findAll())
                .thenReturn(List.of());

        when(userRepository.findAll())
                .thenReturn(List.of());

        when(reservationRepository.findAll())
                .thenReturn(List.of(
                        reservation1,
                        reservation2,
                        reservation3,
                        reservation4,
                        reservation5
                ));

        when(paymentRepository.findAll())
                .thenReturn(List.of());

        DashboardSummary summary =
                service.getDashboardSummary();

        assertEquals(5, summary.getTotalReservations());
        assertEquals(1, summary.getPendingReservations());
        assertEquals(2, summary.getConfirmedReservations());
        assertEquals(1, summary.getCancelledReservations());
        assertEquals(1, summary.getCompletedReservations());
    }


    @Test
    void getDashboardSummary_calculatesCompletedPaymentsCorrectly() {

        Payment payment1 = new Payment();
        payment1.setStatus(PaymentStatus.COMPLETED);
        payment1.setAmount(new BigDecimal("15000"));

        Payment payment2 = new Payment();
        payment2.setStatus(PaymentStatus.COMPLETED);
        payment2.setAmount(new BigDecimal("25000"));

        Payment payment3 = new Payment();
        payment3.setStatus(PaymentStatus.PENDING);
        payment3.setAmount(new BigDecimal("20000"));

        Payment payment4 = new Payment();
        payment4.setStatus(PaymentStatus.FAILED);
        payment4.setAmount(new BigDecimal("10000"));

        Payment payment5 = new Payment();
        payment5.setStatus(PaymentStatus.REFUNDED);
        payment5.setAmount(new BigDecimal("5000"));

        when(roomRepository.findAll())
                .thenReturn(List.of());

        when(userRepository.findAll())
                .thenReturn(List.of());

        when(reservationRepository.findAll())
                .thenReturn(List.of());

        when(paymentRepository.findAll())
                .thenReturn(List.of(
                        payment1,
                        payment2,
                        payment3,
                        payment4,
                        payment5
                ));

        DashboardSummary summary =
                service.getDashboardSummary();

        assertEquals(5, summary.getTotalPayments());
        assertEquals(2, summary.getCompletedPayments());
        assertEquals(1, summary.getRefundedPayments());

        assertEquals(
                0,
                new BigDecimal("40000").compareTo(
                        summary.getGrossCompletedPayments()
                )
        );
    }


    @Test
    void getDashboardSummary_handlesNullPaymentAmount() {

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(null);

        when(roomRepository.findAll())
                .thenReturn(List.of());

        when(userRepository.findAll())
                .thenReturn(List.of());

        when(reservationRepository.findAll())
                .thenReturn(List.of());

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment));

        DashboardSummary summary =
                service.getDashboardSummary();

        assertEquals(1, summary.getTotalPayments());
        assertEquals(1, summary.getCompletedPayments());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        summary.getGrossCompletedPayments()
                )
        );
    }


    @Test
    void generateReport_filtersDatesAndCalculatesTotals() {

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        Reservation r1 = new Reservation();
        r1.setCheckInDate(LocalDate.of(2026, 11, 5));
        r1.setStatus(ReservationStatus.CONFIRMED);

        Reservation r2 = new Reservation();
        r2.setCheckInDate(LocalDate.of(2026, 11, 20));
        r2.setStatus(ReservationStatus.CANCELLED);

        Reservation r3 = new Reservation();
        r3.setCheckInDate(LocalDate.of(2026, 12, 2));
        r3.setStatus(ReservationStatus.CONFIRMED);

        Payment p1 = new Payment();
        p1.setStatus(PaymentStatus.COMPLETED);
        p1.setAmount(new BigDecimal("15000"));
        p1.setPaidAt(LocalDateTime.of(2026, 11, 10, 10, 0));

        Payment p2 = new Payment();
        p2.setStatus(PaymentStatus.PENDING);
        p2.setAmount(new BigDecimal("20000"));
        p2.setPaidAt(LocalDateTime.of(2026, 11, 15, 10, 0));

        Payment p3 = new Payment();
        p3.setStatus(PaymentStatus.COMPLETED);
        p3.setAmount(new BigDecimal("30000"));
        p3.setPaidAt(LocalDateTime.of(2026, 12, 1, 10, 0));

        when(roomRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.findAll())
                .thenReturn(List.of(r1, r2, r3));
        when(paymentRepository.findAll())
                .thenReturn(List.of(p1, p2, p3));

        when(reportRepository.create(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GenerateReportRequest request =
                new GenerateReportRequest(
                        "November Report",
                        start,
                        end,
                        "Monthly report"
                );

        Report report = service.generateReport(request);

        assertNotNull(report.getId());
        assertNotNull(report.getGeneratedAt());
        assertEquals(2, report.getTotalReservations());

        assertEquals(
                0,
                new BigDecimal("15000")
                        .compareTo(report.getTotalRevenue())
        );

        verify(reportRepository).create(any(Report.class));
    }


    @Test
    void generateReport_savesCorrectMetadata() {

        when(roomRepository.findAll())
                .thenReturn(List.of());

        when(reservationRepository.findAll())
                .thenReturn(List.of());

        when(paymentRepository.findAll())
                .thenReturn(List.of());

        when(reportRepository.create(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GenerateReportRequest request =
                new GenerateReportRequest(
                        "  Monthly Summary  ",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        "  October statistics  "
                );

        Report report = service.generateReport(request);

        assertEquals("Monthly Summary", report.getTitle());
        assertEquals("October statistics", report.getNotes());

        assertEquals(
                LocalDate.of(2026, 10, 1),
                report.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 10, 31),
                report.getEndDate()
        );

        assertEquals(0, report.getTotalRooms());

        verify(reportRepository).create(any(Report.class));
    }


    @Test
    void updateReport_preservesCalculatedTotals() {

        Report existing = new Report(
                "REPORT-001",
                "Original Report",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                LocalDateTime.of(2026, 12, 1, 9, 0),
                20,
                15,
                new BigDecimal("150000"),
                "Original notes"
        );

        LocalDateTime originalGeneratedAt =
                existing.getGeneratedAt();

        when(reportRepository.findById("REPORT-001"))
                .thenReturn(Optional.of(existing));

        when(reportRepository.update(eq("REPORT-001"), any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        UpdateReportRequest request =
                new UpdateReportRequest(
                        "Updated Report",
                        "Reviewed by administrator"
                );

        Report updated =
                service.updateReport("REPORT-001", request);

        assertEquals("REPORT-001", updated.getId());
        assertEquals("Updated Report", updated.getTitle());

        assertEquals(
                "Reviewed by administrator",
                updated.getNotes()
        );

        assertEquals(20, updated.getTotalRooms());
        assertEquals(15, updated.getTotalReservations());

        assertEquals(
                0,
                new BigDecimal("150000")
                        .compareTo(updated.getTotalRevenue())
        );

        assertEquals(
                LocalDate.of(2026, 11, 1),
                updated.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 11, 30),
                updated.getEndDate()
        );

        assertEquals(
                originalGeneratedAt,
                updated.getGeneratedAt()
        );

        verify(reportRepository)
                .update(eq("REPORT-001"), any(Report.class));
    }


    @Test
    void deleteReport_existingReport_returnsTrue() {

        Report existing = new Report();
        existing.setId("REPORT-001");

        when(reportRepository.findById("REPORT-001"))
                .thenReturn(Optional.of(existing));

        when(reportRepository.deleteById("REPORT-001"))
                .thenReturn(true);

        boolean deleted = service.deleteReport("REPORT-001");

        assertTrue(deleted);

        verify(reportRepository)
                .findById("REPORT-001");

        verify(reportRepository)
                .deleteById("REPORT-001");
    }


    @Test
    void deleteReport_missingReport_throwsException() {

        when(reportRepository.findById("INVALID-ID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> service.deleteReport("INVALID-ID")
        );

        verify(reportRepository, never())
                .deleteById(anyString());
    }


    // Add the test methods below.
}
