package com.hotel;

import com.hotel.common.AppException;

import com.hotel.room.repository.RoomRepository;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.user.repository.UserRepository;

import com.hotel.search.api.RoomSearchApi;
import com.hotel.search.api.SearchPreferenceApi;
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.search.service.RoomAvailabilityService;
import com.hotel.search.service.SearchPreferenceService;

import com.hotel.payment.api.PaymentApi;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.payment.service.PaymentService;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // Serve HTML, CSS and JavaScript.
            config.staticFiles.add(
                    "/public",
                    Location.CLASSPATH
            );

            // Health check.
            config.routes.get("/api/health", ctx -> {
                ctx.json(Map.of(
                        "status", "ok",
                        "message", "Hotel Reservation API is running"
                ));
            });

            // Handle application-specific exceptions.
            config.routes.exception(
                    AppException.class,
                    (exception, ctx) -> {
                        ctx.status(exception.getStatusCode());
                        ctx.json(Map.of(
                                "error", exception.getMessage()
                        ));
                    }
            );

            // Handle unexpected server errors.
            config.routes.exception(
                    Exception.class,
                    (exception, ctx) -> {
                        exception.printStackTrace();

                        ctx.status(500);
                        ctx.json(Map.of(
                                "error",
                                "An unexpected server error occurred"
                        ));
                    }
            );

            // Preserve specific not-found messages.
            config.routes.error(404, ctx -> {
                String responseBody = ctx.result();

                if (responseBody == null || responseBody.isBlank()) {
                    ctx.json(Map.of(
                            "error", "Resource not found"
                    ));
                }
            });

            // Shared repositories.
            RoomRepository roomRepository =
                    new RoomRepository();

            ReservationRepository reservationRepository =
                    new ReservationRepository();

            UserRepository userRepository =
                    new UserRepository();

            // Room Search & Availability - Sithum.
            RoomAvailabilityService availabilityService =
                    new RoomAvailabilityService(
                            roomRepository,
                            reservationRepository
                    );

            RoomSearchApi roomSearchApi =
                    new RoomSearchApi(availabilityService);

            roomSearchApi.registerRoutes(config);

            // Saved Search Preferences - Sithum.
            SearchPreferenceRepository preferenceRepository =
                    new SearchPreferenceRepository();

            SearchPreferenceService preferenceService =
                    new SearchPreferenceService(
                            preferenceRepository,
                            userRepository
                    );

            SearchPreferenceApi preferenceApi =
                    new SearchPreferenceApi(preferenceService);

            preferenceApi.registerRoutes(config);

            // Payment & Receipt Management - Kaveesha.
            PaymentRepository paymentRepository =
                    new PaymentRepository();

            PaymentService paymentService =
                    new PaymentService(
                            paymentRepository,
                            reservationRepository
                    );

            PaymentApi paymentApi =
                    new PaymentApi(paymentService);

            paymentApi.registerRoutes(config);
        });

        // Start the server.
        app.start(7070);
    }
}