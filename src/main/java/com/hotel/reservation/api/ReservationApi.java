
package com.hotel.reservation.api;

import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.service.ReservationService;
import io.javalin.config.JavalinConfig;

public class ReservationApi {

    private final ReservationService reservationService;

    public ReservationApi(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public void registerRoutes(JavalinConfig config) {

        // GET: Retrieve all reservations
        config.routes.get("/api/reservations", ctx -> {
            ctx.json(reservationService.getAllReservations());
        });

        // GET: Retrieve reservation by ID
        config.routes.get("/api/reservations/{id}", ctx -> {

            String id = ctx.pathParam("id");

            ctx.json(
                    reservationService.getReservationById(id)
            );
        });

        // POST: Create a new reservation
        config.routes.post("/api/reservations", ctx -> {

            Reservation reservation =
                    ctx.bodyAsClass(Reservation.class);

            Reservation created =
                    reservationService.createReservation(reservation);

            ctx.status(201).json(created);
        });

        // PUT: Update an existing reservation
        config.routes.put("/api/reservations/{id}", ctx -> {

            String id = ctx.pathParam("id");

            Reservation updated =
                    ctx.bodyAsClass(Reservation.class);

            Reservation result =
                    reservationService.updateReservation(id, updated);

            ctx.json(result);
        });

        // PATCH: Cancel a reservation
        config.routes.patch("/api/reservations/{id}/cancel", ctx -> {

            String id = ctx.pathParam("id");

            Reservation cancelled =
                    reservationService.cancelReservation(id);

            ctx.json(cancelled);
        });
    }
}
