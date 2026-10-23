
package com.hotel.search.api;

import com.hotel.common.ValidationException;
import com.hotel.search.service.RoomAvailabilityService;
import io.javalin.config.JavalinConfig;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RoomSearchApi {

    private final RoomAvailabilityService availabilityService;

    public RoomSearchApi(RoomAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    public void registerRoutes(JavalinConfig config) {

        config.routes.get("/api/rooms/available", ctx -> {

            String checkInText = ctx.queryParam("checkIn");
            String checkOutText = ctx.queryParam("checkOut");
            String guestsText = ctx.queryParam("guests");

            if (checkInText == null ||
                    checkOutText == null ||
                    guestsText == null) {

                throw new ValidationException(
                        "Check-in, check-out and guests are required."
                );
            }

            try {
                LocalDate checkIn = LocalDate.parse(checkInText);
                LocalDate checkOut = LocalDate.parse(checkOutText);
                int guests = Integer.parseInt(guestsText);

                ctx.json(availabilityService.searchAvailableRooms(
                        checkIn,
                        checkOut,
                        guests
                ));

            } catch (DateTimeParseException |
                     NumberFormatException e) {

                throw new ValidationException(
                        "Invalid dates or guest count. Use YYYY-MM-DD dates."
                );
            }
        });
    }
}
