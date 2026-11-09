
package com.hotel.payment.api;

import com.hotel.common.AccessControl;
import com.hotel.payment.model.Payment;
import com.hotel.payment.service.PaymentService;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.service.ReservationService;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

import java.util.Map;
import java.util.stream.Collectors;

public class PaymentApi {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final AccessControl accessControl;

    public PaymentApi(
            PaymentService paymentService,
            ReservationService reservationService,
            AccessControl accessControl) {

        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.accessControl = accessControl;
    }

    // Check the owner of the reservation linked to a payment.
    private void requirePaymentAccess(
            Context ctx, String paymentId) {

        // Authenticate before retrieving records.
        accessControl.requireLogin(ctx);

        Payment payment =
                paymentService.getPaymentById(paymentId);

        Reservation reservation =
                reservationService.getReservationById(
                        payment.getReservationId());

        accessControl.requireOwnerOrAdmin(
                ctx, reservation.getUserId());
    }

    public void registerRoutes(JavalinConfig config) {

        // CREATE - Customer pays for their own reservation.
        config.routes.post("/api/payments", ctx -> {

            User customer =
                    accessControl.requireCustomer(ctx);

            Payment payment =
                    ctx.bodyAsClass(Payment.class);

            Reservation reservation =
                    reservationService.getReservationById(
                            payment.getReservationId());

            // The reservation must belong to this customer.
            accessControl.requireOwnerOrAdmin(
                    ctx, reservation.getUserId());

            Payment created =
                    paymentService.createPayment(payment);

            ctx.status(201).json(created);
        });

        // READ - Admin sees all; customer sees own payments.
        config.routes.get("/api/payments", ctx -> {

            User user = accessControl.requireLogin(ctx);

            String reservationId =
                    ctx.queryParam("reservationId");

            if (reservationId != null) {

                // Protect filtered payment queries.
                Reservation reservation =
                        reservationService.getReservationById(
                                reservationId);

                accessControl.requireOwnerOrAdmin(
                        ctx, reservation.getUserId());

                ctx.json(
                        paymentService.getPaymentsByReservationId(
                                reservationId));

            } else if (user.getRole() == UserRole.ADMIN) {

                ctx.json(paymentService.getAllPayments());

            } else {

                // Find reservations owned by this customer.
                var ownedReservationIds =
                        reservationService.getAllReservations()
                                .stream()
                                .filter(r -> user.getId().equals(
                                        r.getUserId()))
                                .map(Reservation::getId)
                                .collect(Collectors.toSet());

                // Return only payments linked to those reservations.
                ctx.json(
                        paymentService.getAllPayments()
                                .stream()
                                .filter(p -> ownedReservationIds
                                        .contains(p.getReservationId()))
                                .toList());
            }
        });

        // READ - One payment.
        config.routes.get("/api/payments/{id}", ctx -> {

            String id = ctx.pathParam("id");

            requirePaymentAccess(ctx, id);

            ctx.json(paymentService.getPaymentById(id));
        });

        // UPDATE - Own pending payment or admin.
        config.routes.put("/api/payments/{id}", ctx -> {

            String id = ctx.pathParam("id");

            requirePaymentAccess(ctx, id);

            Payment existing =
                    paymentService.getPaymentById(id);

            Payment updated =
                    ctx.bodyAsClass(Payment.class);

            // Preserve backend-managed identity and ownership.
            updated.setId(existing.getId());
            updated.setReservationId(
                    existing.getReservationId());
            updated.setStatus(existing.getStatus());
            updated.setReceiptNumber(
                    existing.getReceiptNumber());

            ctx.json(
                    paymentService.updatePayment(id, updated));
        });

        // DELETE - Own eligible pending payment or admin.
        config.routes.delete("/api/payments/{id}", ctx -> {

            String id = ctx.pathParam("id");

            requirePaymentAccess(ctx, id);

            paymentService.deletePayment(id);

            ctx.json(Map.of(
                    "message", "Payment deleted successfully."
            ));
        });

        // PROCESS - Own pending payment or admin.
        config.routes.post(
                "/api/payments/{id}/process", ctx -> {

                    String id = ctx.pathParam("id");

                    requirePaymentAccess(ctx, id);

                    ctx.json(paymentService.processPayment(id));
                });

        // FAIL - Own pending payment or admin.
        config.routes.post(
                "/api/payments/{id}/fail", ctx -> {

                    String id = ctx.pathParam("id");

                    requirePaymentAccess(ctx, id);

                    ctx.json(paymentService.failPayment(id));
                });

        // CANCEL - Own pending payment or admin.
        config.routes.post(
                "/api/payments/{id}/cancel", ctx -> {

                    String id = ctx.pathParam("id");

                    requirePaymentAccess(ctx, id);

                    ctx.json(paymentService.cancelPayment(id));
                });

        // REFUND - Administrators only.
        config.routes.post(
                "/api/payments/{id}/refund", ctx -> {

                    accessControl.requireAdmin(ctx);

                    ctx.json(
                            paymentService.refundPayment(
                                    ctx.pathParam("id")));
                });

        // RECEIPT - Owner or administrator.
        config.routes.get(
                "/api/payments/{id}/receipt", ctx -> {

                    String id = ctx.pathParam("id");

                    requirePaymentAccess(ctx, id);

                    String receipt =
                            paymentService.generateReceipt(id);

                    ctx.contentType(
                            "text/plain; charset=utf-8");

                    ctx.result(receipt);
                });
    }
}
