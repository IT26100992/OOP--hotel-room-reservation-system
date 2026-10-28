
package com.hotel.payment.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.payment.model.Payment;
import com.hotel.payment.model.PaymentStatus;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository) {

        this.paymentRepository = Objects.requireNonNull(
                paymentRepository, "PaymentRepository is required.");

        this.reservationRepository = Objects.requireNonNull(
                reservationRepository, "ReservationRepository is required.");
    }

    // CREATE PAYMENT
    public Payment createPayment(Payment payment) {
        validatePayment(payment);

        getReservation(payment.getReservationId());

        if (paymentRepository.findById(payment.getId()).isPresent()) {
            throw new ValidationException("Payment ID already exists.");
        }

        ensureReceiptIsUnique(
                payment.getReceiptNumber(), null);

        // The service controls the initial status.
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(null);

        return paymentRepository.create(payment);
    }

    // SIMULATED PAYMENT PROCESSING
    public Payment processPayment(String paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be processed.");
        }

        // Confirm that the referenced reservation still exists.
        getReservation(payment.getReservationId());

        /*
         * This is a university-project simulation.
         * It does not contact a bank or payment gateway.
         * For demonstration purposes, a valid pending payment succeeds.
         */
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        return paymentRepository.update(paymentId, payment);
    }

    // SIMULATE A FAILED PAYMENT
    public Payment failPayment(String paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be marked as failed.");
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaidAt(null);

        return paymentRepository.update(paymentId, payment);
    }

    // READ ALL PAYMENTS
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // READ PAYMENT BY ID
    public Payment getPaymentById(String id) {
        if (blank(id)) {
            throw new ValidationException("Payment ID is required.");
        }

        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + id));
    }

    // READ PAYMENTS FOR A RESERVATION
    public List<Payment> getPaymentsByReservationId(
            String reservationId) {

        if (blank(reservationId)) {
            throw new ValidationException("Reservation ID is required.");
        }

        getReservation(reservationId);

        return paymentRepository.findAll().stream()
                .filter(payment -> reservationId.equals(
                        payment.getReservationId()))
                .toList();
    }

    // UPDATE PAYMENT
    public Payment updatePayment(String id, Payment updated) {
        Payment existing = getPaymentById(id);

        validatePayment(updated);

        if (!id.equals(updated.getId())) {
            throw new ValidationException(
                    "URL payment ID must match the updated payment ID.");
        }

        if (existing.getStatus() == PaymentStatus.COMPLETED) {
            throw new ValidationException(
                    "Completed payments cannot be edited.");
        }

        if (updated.getStatus() != existing.getStatus()) {
            throw new ValidationException(
                    "Use the payment-processing methods to change payment status.");
        }

        getReservation(updated.getReservationId());

        ensureReceiptIsUnique(updated.getReceiptNumber(), id);

        // Preserve service-managed fields.
        updated.setPaidAt(existing.getPaidAt());

        return paymentRepository.update(id, updated);
    }

    // DELETE PAYMENT
    public boolean deletePayment(String id) {
        Payment existing = getPaymentById(id);

        if (existing.getStatus() == PaymentStatus.COMPLETED) {
            throw new ValidationException(
                    "Completed payments cannot be deleted.");
        }

        return paymentRepository.deleteById(id);
    }

    // GENERATE A NEW PAYMENT ID
    public String generatePaymentId() {
        return "PAY-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    // GENERATE A RECEIPT NUMBER
    public String generateReceiptNumber() {
        String receiptNumber;

        do {
            receiptNumber = "REC-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 12)
                    .toUpperCase(Locale.ROOT);
        } while (receiptNumberExists(receiptNumber));

        return receiptNumber;
    }

    // GENERATE RECEIPT TEXT
    public String generateReceipt(String paymentId) {
        Payment payment = getPaymentById(paymentId);
        Reservation reservation =
                getReservation(payment.getReservationId());

        String paymentDate = payment.getPaidAt() == null
                ? "Not paid"
                : payment.getPaidAt().format(RECEIPT_DATE_FORMAT);

        return """
                ========================================
                       HOTEL PAYMENT RECEIPT
                ========================================
                Receipt Number : %s
                Payment ID    : %s
                Reservation ID: %s
                Room ID       : %s
                Guest/User ID : %s
                Check-in Date : %s
                Check-out Date: %s
                Guest Count   : %d
                ----------------------------------------
                Amount        : %s
                Payment Method: %s
                Payment Status: %s
                Payment Date  : %s
                ========================================
                     Thank you for your booking!
                ========================================
                """.formatted(
                payment.getReceiptNumber(),
                payment.getId(),
                reservation.getId(),
                reservation.getRoomId(),
                reservation.getUserId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getGuestCount(),
                payment.getAmount().toPlainString(),
                payment.getMethod(),
                payment.getStatus(),
                paymentDate
        );
    }

    // VALIDATE REQUIRED PAYMENT DETAILS
    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new ValidationException("Payment cannot be null.");
        }

        if (blank(payment.getId())) {
            throw new ValidationException("Payment ID is required.");
        }

        if (blank(payment.getReservationId())) {
            throw new ValidationException("Reservation ID is required.");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Payment amount must be greater than zero.");
        }

        if (payment.getAmount().scale() > 2) {
            throw new ValidationException(
                    "Payment amount cannot have more than two decimal places.");
        }

        if (payment.getMethod() == null) {
            throw new ValidationException("Payment method is required.");
        }

        if (payment.getStatus() == null) {
            throw new ValidationException("Payment status is required.");
        }

        if (blank(payment.getReceiptNumber())) {
            throw new ValidationException("Receipt number is required.");
        }
    }

    // VERIFY THAT THE RESERVATION EXISTS
    private Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId));
    }

    // ENSURE RECEIPT NUMBER IS UNIQUE
    private void ensureReceiptIsUnique(
            String receiptNumber, String excludedPaymentId) {

        boolean duplicate = paymentRepository.findAll().stream()
                .anyMatch(existing ->
                        existing.getReceiptNumber() != null
                                && existing.getReceiptNumber()
                                .equalsIgnoreCase(receiptNumber)
                                && !Objects.equals(
                                existing.getId(), excludedPaymentId));

        if (duplicate) {
            throw new ValidationException(
                    "Receipt number already exists.");
        }
    }

    private boolean receiptNumberExists(String receiptNumber) {
        return paymentRepository.findAll().stream()
                .anyMatch(payment ->
                        payment.getReceiptNumber() != null
                                && payment.getReceiptNumber()
                                .equalsIgnoreCase(receiptNumber));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
