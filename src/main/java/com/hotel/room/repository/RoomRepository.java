
package com.hotel.room.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;
import com.hotel.room.model.Room;

import java.math.BigDecimal;
import java.util.List;

public class RoomRepository extends JsonCrudRepository<Room> {

    public RoomRepository() {
        super(
                new JsonFileStorage<>(
                        "rooms.json",
                        new TypeReference<List<Room>>() {}
                ),
                Room::getId
        );
    }

    @Override
    protected void validate(Room room) {
        if (room == null) {
            throw new ValidationException(
                    "Room cannot be null."
            );
        }

        if (isBlank(room.getId())
                || isBlank(room.getRoomNumber())
                || room.getRoomType() == null
                || room.getStatus() == null
                || room.getPricePerNight() == null) {
            throw new ValidationException(
                    "Room ID, number, type, status, and price are required."
            );
        }

        if (room.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Room price must be greater than zero."
            );
        }

        if (room.getCapacity() <= 0) {
            throw new ValidationException(
                    "Room capacity must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}