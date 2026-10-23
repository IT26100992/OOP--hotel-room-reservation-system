//Test if the local host port 7070 is working and backend API is running.
package com.hotel;

import com.hotel.common.AppException;
import com.hotel.room.repository.RoomRepository;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.search.service.RoomAvailabilityService;
import com.hotel.search.api.RoomSearchApi;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // Serve HTML, CSS and JavaScript from resources/public
            config.staticFiles.add("/public", Location.CLASSPATH);

            /*app.exception(AppException.class, (exception, ctx) -> {
                ctx.status(exception.getStatusCode().json)
                        .json(Map.of("error", exception.getMessage()));
            });*/

            // Test the backend API
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "message", "Hotel Reservation API is running"
                    ))
            );

            // Handle our application's custom exceptions
            // Handle unexpected server errors
            config.routes.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();

                ctx.status(500).json(
                        Map.of("error", "An unexpected server error occurred")
                );
            });
            // Handle unexpected server errors
            config.routes.exception(Exception.class, (e, ctx) -> {
                ctx.status(500).json(
                        Map.of("error", "An unexpected server error occurred")
                );
            });

            // Return a consistent response for unknown routes
            config.routes.error(404, ctx -> {
                ctx.json(Map.of("error", "Resource not found"));
            });

            // Search & Availability feature - Sithum
            RoomRepository roomRepository = new RoomRepository();

            ReservationRepository reservationRepository =
                    new ReservationRepository();

            RoomAvailabilityService availabilityService =
                    new RoomAvailabilityService(
                            roomRepository,
                            reservationRepository
                    );

            RoomSearchApi roomSearchApi =
                    new RoomSearchApi(availabilityService);

            roomSearchApi.registerRoutes(config);

        });


        app.start(7070);
    }
}
