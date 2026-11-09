
package com.hotel.reservation.api;

import com.hotel.common.AccessControl;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.service.ReservationService;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import io.javalin.config.JavalinConfig;

public class ReservationApi {

    private final ReservationService reservationService;
    private final AccessControl accessControl;

    public ReservationApi(
            ReservationService reservationService,
            AccessControl accessControl) {

        this.reservationService = reservationService;
        this.accessControl = accessControl;
    }

    public void registerRoutes(JavalinConfig config) {

        // READ - Admin sees all reservations.
        // Customers only see their own.
        config.routes.get("/api/reservations", ctx -> {

            User user = accessControl.requireLogin(ctx);

            var reservations =
                    reservationService.getAllReservations();

            if (user.getRole() == UserRole.ADMIN) {
                ctx.json(reservations);
            } else {
                ctx.json(reservations.stream()
                        .filter(reservation ->
                                user.getId().equals(
                                        reservation.getUserId()))
                        .toList());
            }
        });

        // READ - Retrieve one reservation.
        config.routes.get("/api/reservations/{id}", ctx -> {

            Reservation reservation =
                    reservationService.getReservationById(
                            ctx.pathParam("id"));

            accessControl.requireOwnerOrAdmin(
                    ctx, reservation.getUserId());

            ctx.json(reservation);
        });

        // CREATE - Logged-in customer only.
        config.routes.post("/api/reservations", ctx -> {

            User user = accessControl.requireCustomer(ctx);

            Reservation reservation =
                    ctx.bodyAsClass(Reservation.class);

            // Never trust userId supplied by the frontend.
            reservation.setUserId(user.getId());

            Reservation created =
                    reservationService.createReservation(
                            reservation);

            ctx.status(201).json(created);
        });

        // UPDATE - Owner or admin only.
        config.routes.put("/api/reservations/{id}", ctx -> {

            String id = ctx.pathParam("id");

            Reservation existing =
                    reservationService.getReservationById(id);

            accessControl.requireOwnerOrAdmin(
                    ctx, existing.getUserId());

            Reservation updated =
                    ctx.bodyAsClass(Reservation.class);

            // Prevent transferring reservation ownership.
            updated.setUserId(existing.getUserId());

            Reservation result =
                    reservationService.updateReservation(
                            id, updated);

            ctx.json(result);
        });

        // CANCEL - Owner or admin only.
        config.routes.patch(
                "/api/reservations/{id}/cancel", ctx -> {

                    String id = ctx.pathParam("id");

                    Reservation reservation =
                            reservationService.getReservationById(id);

                    accessControl.requireOwnerOrAdmin(
                            ctx, reservation.getUserId());

                    Reservation cancelled =
                            reservationService.cancelReservation(id);

                    ctx.json(cancelled);
                });
    }
}
