package com.hotel.room.service;

import com.hotel.common.ValidationException;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.model.RoomType;
import com.hotel.room.repository.RoomRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hotel.room.exception.DuplicateRoomException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hotel.room.exception.RoomHasReservationsException;
import com.hotel.reservation.model.Reservation;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new RoomRepository();

        ReservationRepository reservationRepository =
                new ReservationRepository();

        roomService = new RoomService(
                roomRepository,
                reservationRepository
        );
    }

    // Create a valid Room object for use in validation tests
    private Room validRoom() {
        return new Room(
                null,
                "205",
                RoomType.values()[0],
                new BigDecimal("15000"),
                2,
                RoomStatus.AVAILABLE,
                "Test room"
        );
    }

    @Test
    void shouldRejectNullRoom() {
        assertThrows(
                ValidationException.class,
                () -> roomService.createRoom(null)
        );
    }

    @Test
    void shouldRejectNegativePrice() {
        Room room = validRoom();
        room.setPricePerNight(new BigDecimal("-500"));

        assertThrows(
                ValidationException.class,
                () -> roomService.createRoom(room)
        );
    }

    @Test
    void shouldRejectZeroCapacity() {
        Room room = validRoom();
        room.setCapacity(0);

        assertThrows(
                ValidationException.class,
                () -> roomService.createRoom(room)
        );
    }

    @Test
    void shouldRejectMissingRoomNumber() {
        Room room = validRoom();
        room.setRoomNumber("");

        assertThrows(
                ValidationException.class,
                () -> roomService.createRoom(room)
        );
    }

    @Test
    void shouldRejectMissingRoomType() {
        Room room = validRoom();
        room.setRoomType(null);

        assertThrows(
                ValidationException.class,
                () -> roomService.createRoom(room)
        );
    }


    @Test
    void shouldRejectDuplicateRoomNumber() {

        // Create simulated repositories
        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        // Simulate an existing Room 205
        Room existingRoom = new Room(
                "ROOM-001",
                "205",
                RoomType.values()[0],
                new BigDecimal("15000"),
                2,
                RoomStatus.AVAILABLE,
                "Existing room"
        );

        // Tell the mock repository what to return
        when(mockRoomRepository.findAll())
                .thenReturn(List.of(existingRoom));

        // Create service using simulated repositories
        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        // Attempt to add another room numbered 205
        Room newRoom = new Room(
                null,
                "205",
                RoomType.values()[0],
                new BigDecimal("18000"),
                3,
                RoomStatus.AVAILABLE,
                "Another room"
        );

        // Verify that duplicate room numbers are rejected
        assertThrows(
                DuplicateRoomException.class,
                () -> testService.createRoom(newRoom)
        );
    }


    @Test
    void shouldCreateRoomSuccessfully() {

        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        when(mockRoomRepository.findAll())
                .thenReturn(List.of());

        when(mockRoomRepository.create(any(Room.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        Room room = validRoom();

        Room createdRoom = testService.createRoom(room);

        assertNotNull(createdRoom.getId());
        assertFalse(createdRoom.getId().isBlank());
        assertEquals("205", createdRoom.getRoomNumber());

        verify(mockRoomRepository).create(any(Room.class));
    }


    @Test
    void shouldGetRoomById() {

        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        Room existingRoom = validRoom();
        existingRoom.setId("ROOM-001");

        when(mockRoomRepository.findById("ROOM-001"))
                .thenReturn(Optional.of(existingRoom));

        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        Room result = testService.getRoomById("ROOM-001");

        assertEquals("ROOM-001", result.getId());
        assertEquals("205", result.getRoomNumber());

        verify(mockRoomRepository).findById("ROOM-001");
    }


    @Test
    void shouldUpdateRoomWithoutChangingId() {

        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        Room existingRoom = validRoom();
        existingRoom.setId("ROOM-001");

        when(mockRoomRepository.findById("ROOM-001"))
                .thenReturn(Optional.of(existingRoom));

        when(mockRoomRepository.findAll())
                .thenReturn(List.of(existingRoom));

        when(mockRoomRepository.update(
                anyString(), any(Room.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(1));

        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        Room updatedRoom = validRoom();
        updatedRoom.setId("DIFFERENT-ID");
        updatedRoom.setPricePerNight(
                new BigDecimal("20000"));

        Room result = testService.updateRoom(
                "ROOM-001",
                updatedRoom
        );

        assertEquals("ROOM-001", result.getId());

        assertEquals(
                0,
                new BigDecimal("20000")
                        .compareTo(result.getPricePerNight())
        );

        verify(mockRoomRepository).update(
                eq("ROOM-001"),
                any(Room.class)
        );
    }


    @Test
    void shouldDeleteRoomWithoutReservations() {

        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        Room existingRoom = validRoom();
        existingRoom.setId("ROOM-001");

        when(mockRoomRepository.findById("ROOM-001"))
                .thenReturn(Optional.of(existingRoom));

        when(mockReservationRepository.findAll())
                .thenReturn(List.of());

        when(mockRoomRepository.deleteById("ROOM-001"))
                .thenReturn(true);

        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        boolean deleted = testService.deleteRoom("ROOM-001");

        assertTrue(deleted);

        verify(mockRoomRepository).deleteById("ROOM-001");
    }


    @Test
    void shouldRejectDeletionWhenRoomHasReservations() {

        RoomRepository mockRoomRepository =
                mock(RoomRepository.class);

        ReservationRepository mockReservationRepository =
                mock(ReservationRepository.class);

        Room existingRoom = validRoom();
        existingRoom.setId("ROOM-001");

        when(mockRoomRepository.findById("ROOM-001"))
                .thenReturn(Optional.of(existingRoom));

        // Simulate a reservation referencing ROOM-001
        Reservation reservation = mock(Reservation.class);

        when(reservation.getRoomId())
                .thenReturn("ROOM-001");

        when(mockReservationRepository.findAll())
                .thenReturn(List.of(reservation));

        RoomService testService = new RoomService(
                mockRoomRepository,
                mockReservationRepository
        );

        assertThrows(
                RoomHasReservationsException.class,
                () -> testService.deleteRoom("ROOM-001")
        );

        // Ensure the repository never deletes the room
        verify(mockRoomRepository, never())
                .deleteById(anyString());
    }


}