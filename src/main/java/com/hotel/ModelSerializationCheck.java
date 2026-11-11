//This is for the smoke test for test if jackson can convert java obj in to json format.
package com.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ModelSerializationCheck {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Reservation original = new Reservation(
                "RES001",
                "USR001",
                "ROOM101",
                LocalDate.parse("2026-11-12"),
                LocalDate.parse("2026-11-15"),
                2,
                ReservationStatus.CONFIRMED,
                LocalDateTime.parse("2026-10-17T14:30:00")
        );

        // Java object -> JSON
        String json = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(original);

        System.out.println("Serialized JSON:");
        System.out.println(json);

        // JSON -> Java object
        Reservation restored =
                mapper.readValue(json, Reservation.class);

        System.out.println("\nDeserialized object:");
        System.out.println("ID: " + restored.getId());
        System.out.println("Check-in: " + restored.getCheckInDate());
        System.out.println("Check-out: " + restored.getCheckOutDate());
        System.out.println("Status: " + restored.getStatus());

        if (!original.getId().equals(restored.getId())
                || !original.getCheckInDate()
                .equals(restored.getCheckInDate())
                || original.getStatus() != restored.getStatus()) {
            throw new IllegalStateException(
                    "JSON serialization check failed!"
            );
        }

        System.out.println("\nJSON serialization check passed!");
    }
}