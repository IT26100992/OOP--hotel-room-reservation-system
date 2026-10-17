
package com.hotel.payment.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.payment.model.Payment;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.repository.ReservationRepository;

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    public Payment createPayment(Payment payment) {
        validatePayment(payment);
        getReservation(payment.getReservationId());

        boolean receiptExists = paymentRepository.findAll().stream()
                .anyMatch(existing ->
                        existing.getReceiptNumber().equalsIgnoreCase(
                                payment.getReceiptNumber()));

        if (receiptExists) {
            throw new ValidationException(
                    "Receipt number already exists.");
        }

        return paymentRepository.create(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + id));
    }

    public Payment updatePayment(String id, Payment updated) {
        getPaymentById(id);
        validatePayment(updated);
        getReservation(updated.getReservationId());

        boolean receiptUsedElsewhere =
                paymentRepository.findAll().stream()
                        .anyMatch(existing ->
                                !existing.getId().equals(id)
                                        && existing.getReceiptNumber()
                                        .equalsIgnoreCase(
                                                updated.getReceiptNumber()));

        if (receiptUsedElsewhere) {
            throw new ValidationException(
                    "Another payment uses this receipt number.");
        }

        return paymentRepository.update(id, updated);
    }

    private Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId));
    }

    private void validatePayment(Payment payment) {
        if (payment == null
                || blank(payment.getId())
                || blank(payment.getReservationId())
                || payment.getAmount() == null
                || payment.getMethod() == null
                || payment.getStatus() == null
                || blank(payment.getReceiptNumber())) {
            throw new ValidationException(
                    "Required payment information is missing.");
        }

        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Payment amount must be greater than zero.");
        }

        if (payment.getAmount().scale() > 2) {
            throw new ValidationException(
                    "Payment amount cannot have more than two decimal places.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}