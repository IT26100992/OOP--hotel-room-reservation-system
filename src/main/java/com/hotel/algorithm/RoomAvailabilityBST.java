
package com.hotel.algorithm;

import com.hotel.room.model.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomAvailabilityBST {

    private static class Node {
        private final Room room;
        private Node left;
        private Node right;

        private Node(Room room) {
            this.room = room;
        }
    }

    private Node root;
    private int size;

    public void insert(Room room) {
        if (room == null || room.getRoomNumber() == null
                || room.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Room and room number are required.");
        }

        root = insertNode(root, room);
    }

    private Node insertNode(Node current, Room room) {
        if (current == null) {
            size++;
            return new Node(room);
        }

        int comparison = room.getRoomNumber().compareToIgnoreCase(
                current.room.getRoomNumber());

        if (comparison < 0) {
            current.left = insertNode(current.left, room);
        } else if (comparison > 0) {
            current.right = insertNode(current.right, room);
        } else {
            throw new IllegalArgumentException(
                    "Duplicate room number: " + room.getRoomNumber());
        }

        return current;
    }

    public Room search(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) {
            return null;
        }

        Node current = root;

        while (current != null) {
            int comparison = roomNumber.compareToIgnoreCase(
                    current.room.getRoomNumber());

            if (comparison == 0) {
                return current.room;
            }

            current = comparison < 0
                    ? current.left
                    : current.right;
        }

        return null;
    }

    public List<Room> inOrderTraversal() {
        List<Room> rooms = new ArrayList<>();
        traverse(root, rooms);
        return rooms;
    }

    private void traverse(Node current, List<Room> result) {
        if (current == null) {
            return;
        }

        traverse(current.left, result);
        result.add(current.room);
        traverse(current.right, result);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }
}