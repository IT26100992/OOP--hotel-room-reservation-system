
package com.hotel.search.service;

import com.hotel.common.ValidationException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.model.RoomType;
import com.hotel.room.repository.RoomRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomAvailabilityServiceTest {

    private RoomRepository roomRepository;
    private ReservationRepository reservationRepository;
    private RoomAvailabilityService service;

    private final LocalDate checkIn =
            LocalDate.of(2026, 11, 11);

    private final LocalDate checkOut =
            LocalDate.of(2026, 11, 12);

    @BeforeEach
    void setUp() {

        // Mock repositories: no real JSON files are touched
        roomRepository = mock(RoomRepository.class);
        reservationRepository = mock(ReservationRepository.class);

        service = new RoomAvailabilityService(
                roomRepository,
                reservationRepository
        );

        // By default there are no reservations
        when(reservationRepository.findAll())
                .thenReturn(List.of());
    }

    // Helper method to create test rooms
    private Room createRoom(
            String id,
            String number,
            RoomType type,
            int capacity,
            RoomStatus status,
            String price) {

        Room room = new Room();

        room.setId(id);
        room.setRoomNumber(number);
        room.setRoomType(type);
        room.setCapacity(capacity);
        room.setStatus(status);
        room.setPricePerNight(new BigDecimal(price));

        return room;
    }

    // Helper method to create test reservations
    private Reservation createReservation(
            String roomId,
            LocalDate start,
            LocalDate end,
            ReservationStatus status) {

        Reservation reservation = new Reservation();

        reservation.setRoomId(roomId);
        reservation.setCheckInDate(start);
        reservation.setCheckOutDate(end);
        reservation.setStatus(status);

        return reservation;
    }

    @Test
    void availableRoomShouldBeReturned() {

        Room room = createRoom(
                "ROOM-001", "R101",
                RoomType.DOUBLE, 2,
                RoomStatus.AVAILABLE, "8500"
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn, checkOut, 2
                );

        assertEquals(1, result.size());
        assertEquals("R101", result.get(0).getRoomNumber());
    }

    @Test
    void overlappingReservationShouldExcludeRoom() {

        Room room = createRoom(
                "ROOM-002", "R102",
                RoomType.DOUBLE, 2,
                RoomStatus.AVAILABLE, "8500"
        );

        Reservation reservation = createReservation(
                "ROOM-002",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 13),
                ReservationStatus.CONFIRMED
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn, checkOut, 2
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void sameDayCheckoutShouldAllowBooking() {

        Room room = createRoom(
                "ROOM-002", "R102",
                RoomType.DOUBLE, 2,
                RoomStatus.AVAILABLE, "8500"
        );

        Reservation reservation = createReservation(
                "ROOM-002",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 13),
                ReservationStatus.CONFIRMED
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation));

        List<Room> result =
                service.searchAvailableRooms(
                        LocalDate.of(2026, 11, 13),
                        LocalDate.of(2026, 11, 15),
                        2
                );

        assertEquals(1, result.size());
    }

    @Test
    void maintenanceRoomShouldBeExcluded() {

        Room room = createRoom(
                "ROOM-003", "R103",
                RoomType.DELUXE, 3,
                RoomStatus.MAINTENANCE, "12000"
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn, checkOut, 2
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void insufficientCapacityShouldExcludeRoom() {

        Room room = createRoom(
                "ROOM-001", "R101",
                RoomType.SINGLE, 1,
                RoomStatus.AVAILABLE, "5000"
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn, checkOut, 2
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void cancelledReservationShouldNotBlockRoom() {

        Room room = createRoom(
                "ROOM-002", "R102",
                RoomType.DOUBLE, 2,
                RoomStatus.AVAILABLE, "8500"
        );

        Reservation reservation = createReservation(
                "ROOM-002",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 13),
                ReservationStatus.CANCELLED
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn, checkOut, 2
                );

        assertEquals(1, result.size());
    }

    @Test
    void roomTypeAndPriceFiltersShouldWork() {

        Room doubleRoom = createRoom(
                "ROOM-002", "R102",
                RoomType.DOUBLE, 2,
                RoomStatus.AVAILABLE, "8500"
        );

        Room deluxeRoom = createRoom(
                "ROOM-003", "R103",
                RoomType.DELUXE, 3,
                RoomStatus.AVAILABLE, "12000"
        );

        when(roomRepository.findAll())
                .thenReturn(List.of(doubleRoom, deluxeRoom));

        List<Room> result =
                service.searchAvailableRooms(
                        checkIn,
                        checkOut,
                        2,
                        RoomType.DOUBLE,
                        new BigDecimal("5000"),
                        new BigDecimal("10000")
                );

        assertEquals(1, result.size());
        assertEquals("R102", result.get(0).getRoomNumber());
    }

    @Test
    void invalidDateRangeShouldThrowException() {

        assertThrows(
                ValidationException.class,
                () -> service.searchAvailableRooms(
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 10),
                        2
                )
        );
    }

    @Test
    void invalidPriceRangeShouldThrowException() {

        assertThrows(
                ValidationException.class,
                () -> service.searchAvailableRooms(
                        checkIn,
                        checkOut,
                        2,
                        null,
                        new BigDecimal("15000"),
                        new BigDecimal("5000")
                )
        );
    }
}
