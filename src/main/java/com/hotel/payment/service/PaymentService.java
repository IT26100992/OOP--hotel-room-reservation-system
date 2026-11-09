package com.hotel.payment.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.payment.model.Payment;
import com.hotel.payment.model.PaymentStatus;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.repository.RoomRepository;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class PaymentService {

    // Shared repositories
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // Receipt date formatter
    private static final DateTimeFormatter RECEIPT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor - all three repositories are required
    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            RoomRepository roomRepository) {

        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "PaymentRepository is required."
        );

        this.reservationRepository = Objects.requireNonNull(
                reservationRepository,
                "ReservationRepository is required."
        );

        this.roomRepository = Objects.requireNonNull(
                roomRepository,
                "RoomRepository is required."
        );
    }


    // CREATE - Create a pending payment
    public synchronized Payment createPayment(Payment payment) {

        if (payment == null) {
            throw new ValidationException(
                    "Payment cannot be null.");
        }

        if (blank(payment.getId())) {
            payment.setId(generatePaymentId());
        }

        if (blank(payment.getReceiptNumber())) {
            payment.setReceiptNumber(generateReceiptNumber());
        }

        validatePayment(payment);

        // Check the reservation exists and can be paid.
        ensureReservationCanBePaid(
                payment.getReservationId()
        );

        // Check the amount against the room price
        // and number of reservation nights.
        validateAmountMatchesReservation(payment);

        // Prevent multiple active payments.
        ensureNoActivePayment(
                payment.getReservationId(),
                null
        );

        if (paymentRepository.findById(
                payment.getId()).isPresent()) {

            throw new ValidationException(
                    "Payment ID already exists.");
        }

        ensureReceiptIsUnique(
                payment.getReceiptNumber(),
                null
        );

        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(null);

        return paymentRepository.create(payment);
    }


    // Simulate successful payment processing.

    public synchronized Payment processPayment(String paymentId) {

        // 1. Find the payment.
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be processed.");
        }

        // 2. Find the reservation linked to this payment.
        Reservation reservation =
                getReservation(payment.getReservationId());

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ValidationException(
                    "Cancelled reservations cannot be paid.");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ValidationException(
                    "Completed reservations cannot be paid again.");
        }

        // 3. Prevent duplicate active payments.
        ensureNoActivePayment(
                payment.getReservationId(),
                paymentId
        );
        validateAmountMatchesReservation(payment);

        // 4. Simulate successful payment.
        // No real money is transferred.
        PaymentStatus previousStatus = payment.getStatus();
        LocalDateTime previousPaidAt = payment.getPaidAt();

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment =
                paymentRepository.update(paymentId, payment);

        // 5. Confirm the linked reservation.
        if (reservation.getStatus() == ReservationStatus.PENDING) {

            reservation.setStatus(ReservationStatus.CONFIRMED);

            try {
                reservationRepository.update(
                        reservation.getId(),
                        reservation
                );

            } catch (RuntimeException error) {

                // Try to restore payment if reservation update fails.
                payment.setStatus(previousStatus);
                payment.setPaidAt(previousPaidAt);

                try {
                    paymentRepository.update(paymentId, payment);
                } catch (RuntimeException rollbackError) {
                    error.addSuppressed(rollbackError);
                }

                throw new IllegalStateException(
                        "Payment processing could not complete " +
                                "because reservation confirmation failed.",
                        error
                );
            }
        }

        return savedPayment;
    }

    // Mark a pending payment as failed.
    public synchronized Payment failPayment(String paymentId) {

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be marked as failed.");
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaidAt(null);

        return paymentRepository.update(paymentId, payment);
    }

    // Cancel a pending payment.
    public synchronized Payment cancelPayment(String paymentId) {

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be cancelled.");
        }

        payment.setStatus(PaymentStatus.VOIDED);
        payment.setPaidAt(null);

        return paymentRepository.update(paymentId, payment);
    }

    // Simulate a full refund of a completed payment.
    public synchronized Payment refundPayment(String paymentId) {

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new ValidationException(
                    "Only completed payments can be refunded.");
        }

        /*
         * University project simulation.
         * No real money is transferred.
         */
        payment.setStatus(PaymentStatus.REFUNDED);

        // Preserve the original payment date and receipt number.
        return paymentRepository.update(paymentId, payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(String id) {

        if (blank(id)) {
            throw new ValidationException(
                    "Payment ID is required.");
        }

        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found: " + id));
    }

    public List<Payment> getPaymentsByReservationId(
            String reservationId) {

        if (blank(reservationId)) {
            throw new ValidationException(
                    "Reservation ID is required.");
        }

        getReservation(reservationId);

        return paymentRepository.findAll()
                .stream()
                .filter(payment ->
                        reservationId.equals(
                                payment.getReservationId()))
                .toList();
    }

    // Only pending payments can be edited.
    public synchronized Payment updatePayment(
            String id,
            Payment updated) {

        Payment existing = getPaymentById(id);

        if (updated == null) {
            throw new ValidationException(
                    "Updated payment cannot be null.");
        }

        validatePayment(updated);

        if (!id.equals(updated.getId())) {
            throw new ValidationException(
                    "URL payment ID must match the updated payment ID.");
        }

        if (existing.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be edited.");
        }

        if (updated.getStatus() != existing.getStatus()) {
            throw new ValidationException(
                    "Use the payment-processing methods to change payment status.");
        }

        ensureReservationCanBePaid(updated.getReservationId());
        validateAmountMatchesReservation(updated);

        ensureNoActivePayment(
                updated.getReservationId(),
                id
        );

        ensureReceiptIsUnique(
                updated.getReceiptNumber(),
                id
        );

        updated.setPaidAt(existing.getPaidAt());

        return paymentRepository.update(id, updated);
    }

    // Preserve completed, failed, refunded and voided records.
    public synchronized boolean deletePayment(String id) {

        Payment existing = getPaymentById(id);

        if (existing.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException(
                    "Only pending payments can be deleted.");
        }

        return paymentRepository.deleteById(id);
    }

    public String generatePaymentId() {

        return "PAY-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    public String generateReceiptNumber() {

        String receiptNumber;

        do {
            receiptNumber = "REC-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase(Locale.ROOT);

        } while (receiptNumberExists(receiptNumber));

        return receiptNumber;
    }

    public String generateReceipt(String paymentId) {

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new ValidationException(
                    "Receipts are available only for completed payments.");
        }

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
                Payment ID     : %s
                Reservation ID : %s
                Room ID        : %s
                Guest/User ID  : %s
                Check-in Date  : %s
                Check-out Date : %s
                Guest Count    : %d
                ----------------------------------------
                Amount         : %s
                Payment Method : %s
                Payment Status : %s
                Payment Date   : %s
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

    private void validatePayment(Payment payment) {

        if (payment == null) {
            throw new ValidationException(
                    "Payment cannot be null.");
        }

        if (blank(payment.getId())) {
            throw new ValidationException(
                    "Payment ID is required.");
        }

        if (blank(payment.getReservationId())) {
            throw new ValidationException(
                    "Reservation ID is required.");
        }

        if (payment.getAmount() == null
                || payment.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ValidationException(
                    "Payment amount must be greater than zero.");
        }

        if (payment.getAmount().scale() > 2) {
            throw new ValidationException(
                    "Payment amount cannot have more than two decimal places.");
        }

        if (payment.getMethod() == null) {
            throw new ValidationException(
                    "Payment method is required.");
        }

        if (blank(payment.getReceiptNumber())) {
            throw new ValidationException(
                    "Receipt number is required.");
        }
    }

    private Reservation getReservation(String reservationId) {

        if (blank(reservationId)) {
            throw new ValidationException(
                    "Reservation ID is required.");
        }

        return reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found: "
                                        + reservationId));
    }

    private void ensureReservationCanBePaid(String reservationId) {

        Reservation reservation = getReservation(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ValidationException(
                    "Cancelled reservations cannot be paid.");
        }
    }

    private void ensureNoActivePayment(
            String reservationId,
            String excludedPaymentId) {

        boolean duplicate = paymentRepository.findAll()
                .stream()
                .anyMatch(existing ->
                        reservationId.equals(
                                existing.getReservationId())
                                && !Objects.equals(
                                existing.getId(),
                                excludedPaymentId)
                                && (existing.getStatus()
                                == PaymentStatus.PENDING
                                || existing.getStatus()
                                == PaymentStatus.COMPLETED));

        if (duplicate) {
            throw new ValidationException(
                    "This reservation already has a pending or completed payment.");
        }
    }

    private void ensureReceiptIsUnique(
            String receiptNumber,
            String excludedPaymentId) {

        boolean duplicate = paymentRepository.findAll()
                .stream()
                .anyMatch(existing ->
                        existing.getReceiptNumber() != null
                                && existing.getReceiptNumber()
                                .equalsIgnoreCase(receiptNumber)
                                && !Objects.equals(
                                existing.getId(),
                                excludedPaymentId));

        if (duplicate) {
            throw new ValidationException(
                    "Receipt number already exists.");
        }
    }

    private boolean receiptNumberExists(String receiptNumber) {

        return paymentRepository.findAll()
                .stream()
                .anyMatch(payment ->
                        payment.getReceiptNumber() != null
                                && payment.getReceiptNumber()
                                .equalsIgnoreCase(receiptNumber));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }


    private void validateAmountMatchesReservation(Payment payment) {

        Reservation reservation =
                getReservation(payment.getReservationId());

        Room room = roomRepository
                .findById(reservation.getRoomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found: " +
                                        reservation.getRoomId()
                        ));

        long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (nights <= 0) {
            throw new ValidationException(
                    "Invalid reservation date range.");
        }

        if (room.getPricePerNight() == null ||
                room.getPricePerNight().signum() <= 0) {
            throw new ValidationException(
                    "Invalid room price.");
        }

        BigDecimal expectedAmount =
                room.getPricePerNight().multiply(
                        BigDecimal.valueOf(nights)
                );

        if (payment.getAmount().compareTo(expectedAmount) != 0) {
            throw new ValidationException(
                    "Incorrect payment amount. Expected LKR " +
                            expectedAmount.toPlainString()
            );
        }
    }

}