
package com.hotel.reservation.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.repository.RoomRepository;
import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import com.hotel.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private ReservationService reservationService;

    private User customer;
    private Room room;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        userRepository = mock(UserRepository.class);
        roomRepository = mock(RoomRepository.class);

        reservationService = new ReservationService(
                reservationRepository,
                userRepository,
                roomRepository
        );

        customer = mock(User.class);
        when(customer.isActive()).thenReturn(true);
        when(customer.getRole()).thenReturn(UserRole.CUSTOMER);

        room = mock(Room.class);
        when(room.getStatus()).thenReturn(RoomStatus.AVAILABLE);
        when(room.getCapacity()).thenReturn(3);

        reservation = createTestReservation(
                "U001",
                "R001",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                2
        );

        when(userRepository.findById("U001"))
                .thenReturn(Optional.of(customer));

        when(roomRepository.findById("R001"))
                .thenReturn(Optional.of(room));

        when(reservationRepository.findAll())
                .thenReturn(List.of());
    }

    @Test
    void createReservationSuccessfully() {
        when(reservationRepository.create(reservation))
                .thenReturn(reservation);

        Reservation result =
                reservationService.createReservation(reservation);

        assertNotNull(result);
        assertEquals("U001", result.getUserId());
        assertEquals("R001", result.getRoomId());

        verify(reservationRepository).create(reservation);
    }

    @Test
    void createReservationWithInactiveUserThrowsException() {
        when(customer.isActive()).thenReturn(false);

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithAdminUserThrowsException() {
        when(customer.getRole()).thenReturn(UserRole.ADMIN);

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithUnavailableRoomThrowsException() {
        when(room.getStatus()).thenReturn(RoomStatus.MAINTENANCE);

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithGuestCountExceedingCapacityThrowsException() {
        when(room.getCapacity()).thenReturn(1);

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithOverlappingDatesThrowsException() {
        Reservation existing = createTestReservation(
                "U002",
                "R001",
                LocalDate.of(2026, 12, 12),
                LocalDate.of(2026, 12, 17),
                2
        );

        when(reservationRepository.findAll())
                .thenReturn(List.of(existing));

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithMissingUserThrowsException() {
        when(userRepository.findById("U001"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithMissingRoomThrowsException() {
        when(roomRepository.findById("R001"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    @Test
    void createReservationWithInvalidDatesThrowsException() {
        reservation.setCheckInDate(LocalDate.of(2026, 12, 15));
        reservation.setCheckOutDate(LocalDate.of(2026, 12, 10));

        assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation(reservation)
        );

        verify(reservationRepository, never()).create(any());
    }

    private Reservation createTestReservation(
            String userId,
            String roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestCount
    ) {
        Reservation testReservation = new Reservation();

        testReservation.setId("RES001");
        testReservation.setUserId(userId);
        testReservation.setRoomId(roomId);
        testReservation.setCheckInDate(checkIn);
        testReservation.setCheckOutDate(checkOut);
        testReservation.setGuestCount(guestCount);
        testReservation.setStatus(ReservationStatus.PENDING);
        testReservation.setCreatedAt(LocalDate.now().atStartOfDay());

        return testReservation;
    }
}
