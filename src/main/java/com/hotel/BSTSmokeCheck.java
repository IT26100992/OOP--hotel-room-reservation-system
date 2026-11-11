
package com.hotel;

import com.hotel.algorithm.RoomAvailabilityBST;
import com.hotel.room.model.Room;
import com.hotel.room.model.RoomStatus;
import com.hotel.room.model.RoomType;

import java.math.BigDecimal;
import java.util.List;

public class BSTSmokeCheck {

    private static Room room(String number) {
        return new Room(
                "ID-" + number,
                number,
                RoomType.SINGLE,
                new BigDecimal("100.00"),
                1,
                RoomStatus.AVAILABLE,
                "BST test"
        );
    }

    public static void main(String[] args) {
        RoomAvailabilityBST tree = new RoomAvailabilityBST();

        tree.insert(room("R203"));
        tree.insert(room("R101"));
        tree.insert(room("R305"));
        tree.insert(room("R102"));

        if (tree.size() != 4) {
            throw new AssertionError("Incorrect BST size.");
        }

        if (tree.search("R101") == null) {
            throw new AssertionError("BST search failed.");
        }

        if (tree.search("R999") != null) {
            throw new AssertionError("Missing room was found.");
        }

        List<Room> ordered = tree.inOrderTraversal();

        for (int i = 1; i < ordered.size(); i++) {
            if (ordered.get(i - 1).getRoomNumber()
                    .compareToIgnoreCase(
                            ordered.get(i).getRoomNumber()) >= 0) {
                throw new AssertionError(
                        "BST traversal is not in order.");
            }
        }

        System.out.println("BST checks passed.");
    }
}