
package com.hotel;

import com.hotel.common.AppException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // Serve HTML, CSS and JavaScript from resources/public
            config.staticFiles.add("/public", Location.CLASSPATH);

            // Test the backend API
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "message", "Hotel Reservation API is running"
                    ))
            );

            // Handle our application's custom exceptions
            config.routes.exception(AppException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(
                        Map.of("error", e.getMessage())
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
        });

        app.start(7070);
    }
}
