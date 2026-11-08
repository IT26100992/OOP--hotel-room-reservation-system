package com.hotel.room.api;

import com.hotel.common.ValidationException;
import com.hotel.room.model.Room;
import com.hotel.room.service.RoomService;

import io.javalin.config.JavalinConfig;

public class RoomApi {

    private final RoomService roomService;

    public RoomApi(RoomService roomService) {
        this.roomService = roomService;
    }

    public void registerRoutes(JavalinConfig config) {

        // CREATE - Add a new room
        config.routes.post("/api/rooms", ctx -> {

            Room room;

            try {
                room = ctx.bodyAsClass(Room.class);
            } catch (Exception e) {
                throw new ValidationException(
                        "Invalid room JSON request.");
            }

            Room createdRoom = roomService.createRoom(room);

            ctx.status(201).json(createdRoom);
        });

        // READ - Get all rooms
        config.routes.get("/api/rooms", ctx -> {

            ctx.json(roomService.getAllRooms());
        });

        // READ - Get one room by ID
        config.routes.get("/api/rooms/{id}", ctx -> {

            String roomId = ctx.pathParam("id");

            ctx.json(roomService.getRoomById(roomId));
        });

        // UPDATE - Modify room details
        config.routes.put("/api/rooms/{id}", ctx -> {

            String roomId = ctx.pathParam("id");

            Room updatedRoom;

            try {
                updatedRoom = ctx.bodyAsClass(Room.class);
            } catch (Exception e) {
                throw new ValidationException(
                        "Invalid room JSON request.");
            }

            Room savedRoom = roomService.updateRoom(
                    roomId,
                    updatedRoom
            );

            ctx.json(savedRoom);
        });

        // DELETE - Remove an unused room
        config.routes.delete("/api/rooms/{id}", ctx -> {

            String roomId = ctx.pathParam("id");

            roomService.deleteRoom(roomId);

            ctx.status(204);
        });
    }
}