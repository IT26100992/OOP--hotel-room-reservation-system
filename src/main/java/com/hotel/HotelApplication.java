package com.hotel;

import io.javalin.Javalin;
import java.util.Map;

public class HotelApplication {

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "message", "Hotel Reservation API is running"
                    ))
            );
        });

        app.start(7070);
    }
}
