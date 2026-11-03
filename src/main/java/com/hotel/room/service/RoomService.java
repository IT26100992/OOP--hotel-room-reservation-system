package com.hotel.room.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.common.ValidationException;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.room.model.Room;
import com.hotel.room.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }


    public Room createRoom(Room room) {

        if (room == null) {
            throw new ValidationException(
                    "Room information is required.");
        }

        // Generate internal ID on the server
        room.setId(UUID.randomUUID().toString());

        validateRoom(room);

        boolean numberExists = roomRepository.findAll().stream()
                .anyMatch(existing ->
                        existing.getRoomNumber().equalsIgnoreCase(
                                room.getRoomNumber().trim()));

        if (numberExists) {
            throw new ValidationException(
                    "Room number already exists.");
        }

        room.setRoomNumber(room.getRoomNumber().trim());

        return roomRepository.create(room);
    }


    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + id));
    }

    public Room updateRoom(String id, Room updatedRoom) {
        Room existingRoom = getRoomById(id);

        if (updatedRoom == null) {
            throw new ValidationException(
                    "Updated room information is required.");
        }

// Preserve the original room ID
        updatedRoom.setId(existingRoom.getId());

        validateRoom(updatedRoom);

        boolean numberUsedByAnotherRoom =
                roomRepository.findAll().stream()
                        .anyMatch(existing ->
                                !existing.getId().equals(id)
                                        && existing.getRoomNumber()
                                        .equalsIgnoreCase(
                                                updatedRoom.getRoomNumber().trim()));

        if (numberUsedByAnotherRoom) {
            throw new ValidationException(
                    "Another room already uses this room number.");
        }

        updatedRoom.setRoomNumber(
                updatedRoom.getRoomNumber().trim());

        return roomRepository.update(id, updatedRoom);
    }

    public boolean deleteRoom(String id) {
        getRoomById(id);

        boolean referenced = reservationRepository.findAll().stream()
                .anyMatch(reservation ->
                        reservation.getRoomId().equals(id));

        if (referenced) {
            throw new ValidationException(
                    "This room has reservation history. "
                            + "Set its status to INACTIVE instead.");
        }

        return roomRepository.deleteById(id);
    }

    private void validateRoom(Room room) {
        if (room == null
                || blank(room.getId())
                || blank(room.getRoomNumber())
                || room.getRoomType() == null
                || room.getStatus() == null
                || room.getPricePerNight() == null) {
            throw new ValidationException(
                    "Required room information is missing.");
        }

        if (room.getPricePerNight()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Price must be greater than zero.");
        }

        if (room.getCapacity() <= 0) {
            throw new ValidationException(
                    "Room capacity must be greater than zero.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}