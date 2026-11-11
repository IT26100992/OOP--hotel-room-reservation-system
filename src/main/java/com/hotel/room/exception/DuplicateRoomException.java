package com.hotel.room.exception;

import com.hotel.common.AppException;

public class DuplicateRoomException extends AppException {

    public DuplicateRoomException(String roomNumber) {
        super(
                "Room number already exists: " + roomNumber,
                409
        );
    }
}