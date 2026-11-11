package com.hotel.room.exception;

import com.hotel.common.AppException;

public class RoomHasReservationsException extends AppException {

    public RoomHasReservationsException(String roomId) {
        super(
                "Room " + roomId +
                        " has reservation history. " +
                        "Set its status to INACTIVE instead.",
                409
        );
    }
}