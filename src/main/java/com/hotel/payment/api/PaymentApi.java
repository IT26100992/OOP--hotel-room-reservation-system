package com.hotel.payment.api;

import com.hotel.payment.model.Payment;
import com.hotel.payment.service.PaymentService;
import io.javalin.config.JavalinConfig;

import java.util.Map;

public class PaymentApi {

    private final PaymentService paymentService;

    public PaymentApi(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void registerRoutes(JavalinConfig config) {

        // Create a pending payment.
        config.routes.post("/api/payments", ctx -> {
            Payment payment = ctx.bodyAsClass(Payment.class);

            Payment created = paymentService.createPayment(payment);

            ctx.status(201).json(created);
        });

        // List all payments, or filter by reservation.
        config.routes.get("/api/payments", ctx -> {
            String reservationId = ctx.queryParam("reservationId");

            if (reservationId == null) {
                ctx.json(paymentService.getAllPayments());
            } else {
                ctx.json(
                        paymentService.getPaymentsByReservationId(
                                reservationId)
                );
            }
        });

        // Find one payment.
        config.routes.get("/api/payments/{id}", ctx -> {
            ctx.json(
                    paymentService.getPaymentById(
                            ctx.pathParam("id"))
            );
        });

        // Update permitted payment details.
        config.routes.put("/api/payments/{id}", ctx -> {
            Payment updated = ctx.bodyAsClass(Payment.class);

            ctx.json(
                    paymentService.updatePayment(
                            ctx.pathParam("id"),
                            updated)
            );
        });

        // Delete an eligible payment.
        config.routes.delete("/api/payments/{id}", ctx -> {
            paymentService.deletePayment(ctx.pathParam("id"));

            ctx.json(Map.of(
                    "message", "Payment deleted successfully."
            ));
        });

        // Simulate successful payment processing.
        config.routes.post("/api/payments/{id}/process", ctx -> {
            ctx.json(
                    paymentService.processPayment(
                            ctx.pathParam("id"))
            );
        });

        // Mark a pending payment as failed.
        config.routes.post("/api/payments/{id}/fail", ctx -> {
            ctx.json(
                    paymentService.failPayment(
                            ctx.pathParam("id"))
            );
        });

        // Return a completed payment's printable receipt.
        config.routes.get("/api/payments/{id}/receipt", ctx -> {
            String receipt = paymentService.generateReceipt(
                    ctx.pathParam("id"));

            ctx.contentType("text/plain; charset=utf-8");
            ctx.result(receipt);
        });
    }
}