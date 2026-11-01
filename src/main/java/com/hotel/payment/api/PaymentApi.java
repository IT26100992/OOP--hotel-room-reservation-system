package com.hotel.payment.api;

import com.hotel.payment.model.Payment;
import com.hotel.payment.service.PaymentService;
import io.javalin.config.JavalinConfig;

import java.util.List;
import java.util.Map;

public class PaymentApi {
    private final PaymentService paymentService;

    public PaymentApi(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void registerRoutes(JavalinConfig config) {
        config.routes.post("/api/payments", ctx -> {
            Payment created = paymentService.createPayment(ctx.bodyAsClass(Payment.class));
            ctx.status(201).json(created);
        });

        config.routes.get("/api/payments/generate-id", ctx ->
                ctx.json(Map.of("paymentId", paymentService.generatePaymentId())));

        config.routes.get("/api/payments/generate-receipt-number", ctx ->
                ctx.json(Map.of("receiptNumber", paymentService.generateReceiptNumber())));

        config.routes.get("/api/payments", ctx ->
                ctx.json(paymentService.getAllPayments()));

        config.routes.get("/api/payments/reservation/{reservationId}", ctx ->
                ctx.json(paymentService.getPaymentsByReservationId(
                        ctx.pathParam("reservationId"))));

        config.routes.get("/api/payments/{id}/receipt", ctx -> {
            ctx.contentType("text/plain; charset=utf-8");
            ctx.result(paymentService.generateReceipt(ctx.pathParam("id")));
        });

        config.routes.post("/api/payments/{id}/process", ctx ->
                ctx.json(paymentService.processPayment(ctx.pathParam("id"))));

        config.routes.post("/api/payments/{id}/fail", ctx ->
                ctx.json(paymentService.failPayment(ctx.pathParam("id"))));

        config.routes.get("/api/payments/{id}", ctx ->
                ctx.json(paymentService.getPaymentById(ctx.pathParam("id"))));

        config.routes.put("/api/payments/{id}", ctx ->
                ctx.json(paymentService.updatePayment(
                        ctx.pathParam("id"), ctx.bodyAsClass(Payment.class))));

        config.routes.delete("/api/payments/{id}", ctx -> {
            paymentService.deletePayment(ctx.pathParam("id"));
            ctx.status(204);
        });
    }
}
