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
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.search.service.SearchPreferenceService;
import com.hotel.search.api.SearchPreferenceApi;
import com.hotel.user.repository.UserRepository;
import com.hotel.admin.api.AdminReportApi;
import com.hotel.admin.service.AdminReportService;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.admin.repository.ReportRepository;
import io.javalin.http.NotFoundResponse;

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

            //newly add missing exception handler
            config.routes.exception(AppException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(
                        Map.of("error", e.getMessage())
                );
            });


            // Handle our application's custom exceptions
            // Handle unexpected server errors
            config.routes.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();

                ctx.status(500).json(
                        Map.of("error", "An unexpected server error occurred")
                );
            });
            // Handle unexpected server errors
            /*config.routes.exception(Exception.class, (e, ctx) -> {
                ctx.status(500).json(
                        Map.of("error", "An unexpected server error occurred")
                );
            });*/

            config.routes.exception(NotFoundResponse.class, (e, ctx) -> {
                ctx.status(404).json(
                        Map.of("error", "Resource not found")
                );
            });

            config.routes.exception(AppException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(
                        Map.of("error", e.getMessage())
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


// Admin Dashboard & Reports Feature - Mihiri

            PaymentRepository paymentRepository =
                    new PaymentRepository();


            ReportRepository reportRepository =
                    new ReportRepository();

            AdminReportService adminReportService =
                    new AdminReportService(
                            roomRepository,
                            userRepository,
                            reservationRepository,
                            paymentRepository,
                            reportRepository
                    );


            AdminReportApi adminReportApi =
                    new AdminReportApi(adminReportService);

            adminReportApi.registerRoutes(config);



        });


        app.start(7070);
    }
}