
package com.hotel;

import com.hotel.common.AppException;

import com.hotel.room.repository.RoomRepository;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.service.ReservationService;
import com.hotel.reservation.api.ReservationApi;

import com.hotel.user.repository.UserRepository;

import com.hotel.search.service.RoomAvailabilityService;
import com.hotel.search.api.RoomSearchApi;

import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.search.service.SearchPreferenceService;
import com.hotel.search.api.SearchPreferenceApi;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // Serve frontend files
            config.staticFiles.add(
                    "/public",
                    Location.CLASSPATH
            );

            // Health check API
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "message", "Hotel Reservation API is running"
                    ))
            );

            // Handle application exceptions
            config.routes.exception(AppException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(
                        Map.of("error", e.getMessage())
                );
            });

            // Handle unexpected exceptions
            config.routes.exception(Exception.class, (e, ctx) -> {

                e.printStackTrace();

                ctx.status(500).json(
                        Map.of(
                                "error",
                                "An unexpected server error occurred"
                        )
                );
            });

            // Handle unknown routes
            config.routes.error(404, ctx -> {
                ctx.json(Map.of(
                        "error",
                        "Resource not found"
                ));
            });

            // Shared repositories
            RoomRepository roomRepository =
                    new RoomRepository();

            ReservationRepository reservationRepository =
                    new ReservationRepository();

            UserRepository userRepository =
                    new UserRepository();

            // --------------------------------
            // RESERVATION MANAGEMENT - ASHAN
            // --------------------------------

            ReservationService reservationService =
                    new ReservationService(
                            reservationRepository,
                            userRepository,
                            roomRepository
                    );

            ReservationApi reservationApi =
                    new ReservationApi(reservationService);

            reservationApi.registerRoutes(config);

            // --------------------------------
            // ROOM SEARCH & AVAILABILITY
            // --------------------------------

            RoomAvailabilityService availabilityService =
                    new RoomAvailabilityService(
                            roomRepository,
                            reservationRepository
                    );

            RoomSearchApi roomSearchApi =
                    new RoomSearchApi(availabilityService);

            roomSearchApi.registerRoutes(config);

            // --------------------------------
            // SAVED SEARCH PREFERENCES
            // --------------------------------

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

        });

        // Start backend server
        app.start(7070);

        System.out.println(
                "Hotel Reservation System running on port 7070"
        );
    }
}
