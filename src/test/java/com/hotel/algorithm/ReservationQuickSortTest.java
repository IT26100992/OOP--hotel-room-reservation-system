
package com.hotel.algorithm;

import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationQuickSortTest {

    private Reservation createReservation(
            String id, LocalDate checkInDate) {

        return new Reservation(
                id,
                "USER001",
                "ROOM001",
                checkInDate,
                checkInDate.plusDays(2),
                2,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );
    }

    @Test
    void sortShouldArrangeReservationsByCheckInDate() {

        List<Reservation> reservations = new ArrayList<>();

        reservations.add(createReservation(
                "R001", LocalDate.of(2026, 12, 15)));

        reservations.add(createReservation(
                "R002", LocalDate.of(2026, 11, 5)));

        reservations.add(createReservation(
                "R003", LocalDate.of(2026, 12, 1)));

        ReservationQuickSort.sortByCheckInDate(reservations);

        assertEquals("R002", reservations.get(0).getId());
        assertEquals("R003", reservations.get(1).getId());
        assertEquals("R001", reservations.get(2).getId());
    }

    @Test
    void sortShouldHandleEmptyList() {

        List<Reservation> reservations = new ArrayList<>();

        ReservationQuickSort.sortByCheckInDate(reservations);

        assertTrue(reservations.isEmpty());
    }

    @Test
    void sortShouldHandleSingleReservation() {

        List<Reservation> reservations = new ArrayList<>();

        reservations.add(createReservation(
                "R001", LocalDate.of(2026, 11, 5)));

        ReservationQuickSort.sortByCheckInDate(reservations);

        assertEquals(1, reservations.size());
        assertEquals("R001", reservations.get(0).getId());
    }

    @Test
    void sortShouldHandleDuplicateCheckInDates() {

        List<Reservation> reservations = new ArrayList<>();

        reservations.add(createReservation(
                "R001", LocalDate.of(2026, 11, 5)));

        reservations.add(createReservation(
                "R002", LocalDate.of(2026, 11, 5)));

        reservations.add(createReservation(
                "R003", LocalDate.of(2026, 10, 5)));

        ReservationQuickSort.sortByCheckInDate(reservations);

        assertEquals(
                LocalDate.of(2026, 10, 5),
                reservations.get(0).getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 11, 5),
                reservations.get(1).getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 11, 5),
                reservations.get(2).getCheckInDate()
        );
    }

    @Test
    void sortShouldRejectNullList() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ReservationQuickSort.sortByCheckInDate(null)
        );
    }

    @Test
    void sortShouldRejectNullReservation() {

        List<Reservation> reservations = new ArrayList<>();

        reservations.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> ReservationQuickSort.sortByCheckInDate(reservations)
        );
    }
}
