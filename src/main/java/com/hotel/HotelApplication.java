//Test if the local host port 7070 is working and backend API is running.
package com.hotel;

import com.hotel.user.api.AuthApi;
import com.hotel.user.repository.UserRepository;
import com.hotel.user.service.UserService;
import com.hotel.user.service.AuthenticationService;
import com.hotel.common.AppException;
import com.hotel.room.repository.RoomRepository;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.search.service.RoomAvailabilityService;
import com.hotel.search.api.RoomSearchApi;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.util.Map;
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.search.service.SearchPreferenceService;
import com.hotel.search.api.SearchPreferenceApi;

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


            // Handle our application's custom exceptions
            config.routes.exception(AppException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(
                        Map.of("error", e.getMessage())
                );
            });

            // Handle unexpected server errors
            config.routes.exception(Exception.class, (e, ctx) -> {

                // Show the actual exception in IntelliJ
                e.printStackTrace();

                ctx.status(500).json(
                        Map.of(
                                "error",
                                "An unexpected server error occurred"
                        )
                );
            });

            // Handle unknown API routes
            config.routes.error(404, ctx -> {
                ctx.json(
                        Map.of("error", "Resource not found")
                );
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

            // Saved Search Preferences - Sithum
            SearchPreferenceRepository preferenceRepository =
                    new SearchPreferenceRepository();

            UserRepository userRepository =
                    new UserRepository();

            SearchPreferenceService preferenceService =
                    new SearchPreferenceService(
                            preferenceRepository,
                            userRepository
                    );

            SearchPreferenceApi preferenceApi =
                    new SearchPreferenceApi(preferenceService);

            preferenceApi.registerRoutes(config);


            // User Registration API - Authentication Feature

            UserService userService = new UserService(userRepository);

            AuthenticationService authenticationService =
                    new AuthenticationService(
                            userService,
                            userRepository
                    );

            AuthApi authApi = new AuthApi(
                    authenticationService,
                    userService
            );

            authApi.registerRoutes(config);



        });


        app.start(7070);
    }
}
