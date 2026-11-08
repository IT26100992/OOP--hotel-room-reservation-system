
package com.hotel;

// ======================================
// USER AUTHENTICATION - ASHINI
// ======================================

import com.hotel.user.api.AuthApi;
import com.hotel.user.repository.UserRepository;
import com.hotel.user.service.UserService;
import com.hotel.user.service.AuthenticationService;

// ======================================
// ROOM MANAGEMENT - RENU
// ======================================

import com.hotel.room.repository.RoomRepository;
import com.hotel.room.api.RoomApi;
import com.hotel.room.service.RoomService;

// ======================================
// RESERVATION MANAGEMENT - ASHAN
// ======================================

import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.service.ReservationService;
import com.hotel.reservation.api.ReservationApi;

// ======================================
// SEARCH & AVAILABILITY - SITHUM
// ======================================

import com.hotel.search.service.RoomAvailabilityService;
import com.hotel.search.api.RoomSearchApi;
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.search.service.SearchPreferenceService;
import com.hotel.search.api.SearchPreferenceApi;

// ======================================
// PAYMENT & RECEIPT - KAVEESHA
// ======================================

import com.hotel.payment.api.PaymentApi;
import com.hotel.payment.repository.PaymentRepository;
import com.hotel.payment.service.PaymentService;

// ======================================
// ADMIN DASHBOARD & REPORTS - MIHIRI
// ======================================

import com.hotel.admin.api.AdminReportApi;
import com.hotel.admin.service.AdminReportService;
import com.hotel.admin.repository.ReportRepository;

// ======================================
// EXCEPTION HANDLING & JAVALIN
// ======================================

import com.hotel.common.AppException;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // ======================================
            // FRONTEND STATIC FILES
            // ======================================

            config.staticFiles.add(
                    "/public",
                    Location.CLASSPATH
            );

            // ======================================
            // HEALTH CHECK
            // ======================================

            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "message",
                            "Hotel Reservation API is running"
                    ))
            );

            // ======================================
            // EXCEPTION HANDLING
            // ======================================

            // Handle custom application exceptions
            config.routes.exception(
                    AppException.class,
                    (exception, ctx) -> {

                        ctx.status(exception.getStatusCode());

                        ctx.json(Map.of(
                                "error",
                                exception.getMessage()
                        ));
                    }
            );

            // Handle unexpected server exceptions
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

            // Handle unknown API routes
            config.routes.error(404, ctx -> {

                String responseBody = ctx.result();

                if (responseBody == null ||
                        responseBody.isBlank()) {

                    ctx.json(Map.of(
                            "error",
                            "Resource not found"
                    ));
                }
            });

            // ======================================
            // SHARED REPOSITORIES
            // ======================================

            UserRepository userRepository =
                    new UserRepository();

            RoomRepository roomRepository =
                    new RoomRepository();

            ReservationRepository reservationRepository =
                    new ReservationRepository();

            PaymentRepository paymentRepository =
                    new PaymentRepository();

            ReportRepository reportRepository =
                    new ReportRepository();

            // ======================================
            // USER AUTHENTICATION - ASHINI
            // ======================================

            UserService userService =
                    new UserService(userRepository);

            AuthenticationService authenticationService =
                    new AuthenticationService(
                            userService,
                            userRepository
                    );

            AuthApi authApi =
                    new AuthApi(
                            authenticationService,
                            userService
                    );

            authApi.registerRoutes(config);

            // ======================================
            // ROOM MANAGEMENT - RENU
            // ======================================

            RoomService roomService =
                    new RoomService(
                            roomRepository,
                            reservationRepository
                    );

            RoomApi roomApi =
                    new RoomApi(roomService);

            roomApi.registerRoutes(config);

            // ======================================
            // RESERVATION MANAGEMENT - ASHAN
            // ======================================

            ReservationService reservationService =
                    new ReservationService(
                            reservationRepository,
                            userRepository,
                            roomRepository
                    );

            ReservationApi reservationApi =
                    new ReservationApi(reservationService);

            reservationApi.registerRoutes(config);

            // ======================================
            // SEARCH & AVAILABILITY - SITHUM
            // ======================================

            RoomAvailabilityService availabilityService =
                    new RoomAvailabilityService(
                            roomRepository,
                            reservationRepository
                    );

            RoomSearchApi roomSearchApi =
                    new RoomSearchApi(availabilityService);

            roomSearchApi.registerRoutes(config);

            // ======================================
            // SAVED SEARCH PREFERENCES - SITHUM
            // ======================================

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

            // ======================================
            // PAYMENT & RECEIPT - KAVEESHA
            // ======================================

            PaymentService paymentService =
                    new PaymentService(
                            paymentRepository,
                            reservationRepository
                    );

            PaymentApi paymentApi =
                    new PaymentApi(paymentService);

            paymentApi.registerRoutes(config);

            // ======================================
            // ADMIN DASHBOARD & REPORTS - MIHIRI
            // ======================================

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

        // ======================================
        // START SERVER
        // ======================================

        app.start(7070);

        System.out.println(
                "Hotel Reservation System running on port 7070"
        );
    }
}
