package com.hotel.payment.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.payment.model.Payment;
import com.hotel.payment.model.PaymentMethod;
import com.hotel.payment.model.PaymentStatus;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private ReservationRepository reservationRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        reservationRepository = mock(ReservationRepository.class);

        paymentService = new PaymentService(
                paymentRepository,
                reservationRepository
        );
    }

    @Test
    void completedPaymentProducesReceipt() {
        Payment payment = paymentWithStatus(PaymentStatus.COMPLETED);

        Reservation reservation = new Reservation();
        reservation.setId("RES-001");
        reservation.setUserId("USER-001");
        reservation.setRoomId("ROOM-001");
        reservation.setCheckInDate(LocalDate.of(2026, 12, 10));
        reservation.setCheckOutDate(LocalDate.of(2026, 12, 12));
        reservation.setGuestCount(2);

        when(paymentRepository.findById("PAY-001"))
                .thenReturn(Optional.of(payment));

        when(reservationRepository.findById("RES-001"))
                .thenReturn(Optional.of(reservation));

        String receipt = paymentService.generateReceipt("PAY-001");

        assertAll(
                () -> assertTrue(receipt.contains("REC-001")),
                () -> assertTrue(receipt.contains("PAY-001")),
                () -> assertTrue(receipt.contains("RES-001")),
                () -> assertTrue(receipt.contains("ROOM-001")),
                () -> assertTrue(receipt.contains("USER-001")),
                () -> assertTrue(receipt.contains("15000.00")),
                () -> assertTrue(receipt.contains("COMPLETED")),
                () -> assertTrue(
                        receipt.contains("2026-12-10 10:30:00"))
        );

        verify(paymentRepository, never())
                .update(anyString(), any(Payment.class));
    }

    @Test
    void nonCompletedPaymentsCannotProduceReceipts() {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status == PaymentStatus.COMPLETED) {
                continue;
            }

            when(paymentRepository.findById("PAY-001"))
                    .thenReturn(Optional.of(paymentWithStatus(status)));

            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> paymentService.generateReceipt("PAY-001"),
                    "Receipt should be rejected for status: " + status
            );

            assertEquals(
                    "Receipts are available only for completed payments.",
                    exception.getMessage()
            );
        }

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void missingPaymentCannotProduceReceipt() {
        when(paymentRepository.findById("PAY-MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.generateReceipt("PAY-MISSING")
        );

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void missingReservationCannotProduceReceipt() {
        when(paymentRepository.findById("PAY-001"))
                .thenReturn(Optional.of(
                        paymentWithStatus(PaymentStatus.COMPLETED)));

        when(reservationRepository.findById("RES-001"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.generateReceipt("PAY-001")
        );
    }

    private Payment paymentWithStatus(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId("PAY-001");
        payment.setReservationId("RES-001");
        payment.setAmount(new BigDecimal("15000.00"));
        payment.setMethod(PaymentMethod.CARD);
        payment.setStatus(status);
        payment.setReceiptNumber("REC-001");

        if (status == PaymentStatus.COMPLETED) {
            payment.setPaidAt(
                    LocalDateTime.of(2026, 12, 10, 10, 30));
        }

        return payment;
    }
}