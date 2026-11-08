
package com.hotel.search.api;

import com.hotel.common.ValidationException;
import com.hotel.search.service.RoomAvailabilityService;
import io.javalin.config.JavalinConfig;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.hotel.room.model.RoomType;
import java.math.BigDecimal;
import java.util.Locale;

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

                String roomTypeText = ctx.queryParam("roomType");
                String minPriceText = ctx.queryParam("minPrice");
                String maxPriceText = ctx.queryParam("maxPrice");

                RoomType roomType = null;
                BigDecimal minPrice = null;
                BigDecimal maxPrice = null;

                if (roomTypeText != null && !roomTypeText.isBlank()) {
                    roomType = RoomType.valueOf(
                            roomTypeText.trim().toUpperCase(Locale.ROOT)
                    );
                }

                if (minPriceText != null && !minPriceText.isBlank()) {
                    minPrice = new BigDecimal(minPriceText.trim());
                }

                if (maxPriceText != null && !maxPriceText.isBlank()) {
                    maxPrice = new BigDecimal(maxPriceText.trim());
                }

                ctx.json(availabilityService.searchAvailableRooms(
                        checkIn,
                        checkOut,
                        guests,
                        roomType,
                        minPrice,
                        maxPrice
                ));

            } catch (DateTimeParseException |
                     IllegalArgumentException e) {

                throw new ValidationException(
                        "Invalid search parameters. Check dates, guests, " +
                                "room type and price values."
                );
            }



        });
    }
}
