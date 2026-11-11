
package com.hotel.payment.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;
import com.hotel.payment.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRepository extends JsonCrudRepository<Payment> {

    public PaymentRepository() {
        super(
                new JsonFileStorage<>(
                        "payments.json",
                        new TypeReference<List<Payment>>() {}
                ),
                Payment::getId
        );
    }

    @Override
    protected void validate(Payment payment) {
        if (payment == null) {
            throw new ValidationException(
                    "Payment cannot be null."
            );
        }

        if (isBlank(payment.getId())
                || isBlank(payment.getReservationId())
                || payment.getAmount() == null
                || payment.getMethod() == null
                || payment.getStatus() == null
                || isBlank(payment.getReceiptNumber())) {
            throw new ValidationException(
                    "Payment is missing required fields."
            );
        }

        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Payment amount must be greater than zero."
            );
        }

        if (payment.getAmount().scale() > 2) {
            throw new ValidationException(
                    "Payment amount cannot have more than two decimal places."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}