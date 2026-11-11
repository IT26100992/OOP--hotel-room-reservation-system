
package com.hotel.algorithm;

import com.hotel.room.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomAvailabilityBSTTest {

    private RoomAvailabilityBST tree;

    @BeforeEach
    void setUp() {
        // Create a fresh BST before every test
        tree = new RoomAvailabilityBST();
    }

    // Helper to create room objects for testing
    private Room createRoom(String number) {
        Room room = new Room();
        room.setRoomNumber(number);
        return room;
    }

    @Test
    void newTreeShouldBeEmpty() {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
    }

    @Test
    void insertShouldIncreaseTreeSize() {
        tree.insert(createRoom("R102"));
        tree.insert(createRoom("R101"));
        tree.insert(createRoom("R103"));

        assertEquals(3, tree.size());
        assertFalse(tree.isEmpty());
    }

    @Test
    void searchShouldFindExistingRoom() {
        tree.insert(createRoom("R102"));
        tree.insert(createRoom("R101"));

        Room result = tree.search("R101");

        assertNotNull(result);
        assertEquals("R101", result.getRoomNumber());
    }

    @Test
    void searchShouldReturnNullForMissingRoom() {
        tree.insert(createRoom("R101"));

        Room result = tree.search("R999");

        assertNull(result);
    }

    @Test
    void traversalShouldReturnSortedRooms() {
        tree.insert(createRoom("R103"));
        tree.insert(createRoom("R101"));
        tree.insert(createRoom("R102"));

        List<Room> result = tree.inOrderTraversal();

        assertEquals(3, result.size());

        assertEquals("R101", result.get(0).getRoomNumber());
        assertEquals("R102", result.get(1).getRoomNumber());
        assertEquals("R103", result.get(2).getRoomNumber());
    }

    @Test
    void duplicateRoomNumbersShouldBeRejected() {
        tree.insert(createRoom("R101"));

        assertThrows(
                IllegalArgumentException.class,
                () -> tree.insert(createRoom("R101"))
        );

        assertEquals(1, tree.size());
    }

    @Test
    void nullRoomShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tree.insert(null)
        );
    }

    @Test
    void searchShouldIgnoreLetterCase() {
        tree.insert(createRoom("R101"));

        Room result = tree.search("r101");

        assertNotNull(result);
        assertEquals("R101", result.getRoomNumber());
    }
}
