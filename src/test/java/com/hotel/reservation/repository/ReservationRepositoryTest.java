
package com.hotel.reservation.repository;

import com.hotel.common.ValidationException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.ReservationStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReservationRepositoryTest {

    private ReservationRepository repository;
    private Path filePath;
    private String originalContent;

    @BeforeEach
    void setUp() throws IOException {

        filePath = Path.of("data", "reservations.json");

        Files.createDirectories(filePath.getParent());

        originalContent = Files.exists(filePath)
                ? Files.readString(filePath)
                : null;

        Files.writeString(filePath, "[]");

        repository = new ReservationRepository();
    }

    @AfterEach
    void restoreFile() throws IOException {

        if (originalContent == null) {
            Files.deleteIfExists(filePath);
        } else {
            Files.writeString(filePath, originalContent);
        }
    }

    private Reservation createReservation(String id) {

        return new Reservation(
                id,
                "USER001",
                "ROOM001",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                2,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );
    }

    @Test
    void createShouldSaveReservation() {

        Reservation reservation = createReservation("RES001");

        Reservation result = repository.create(reservation);

        assertNotNull(result);
        assertEquals("RES001", result.getId());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdShouldReturnExistingReservation() {

        repository.create(createReservation("RES001"));

        Optional<Reservation> result =
                repository.findById("RES001");

        assertTrue(result.isPresent());
        assertEquals("RES001", result.get().getId());
    }

    @Test
    void findByIdShouldReturnEmptyForMissingReservation() {

        Optional<Reservation> result =
                repository.findById("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllShouldReturnAllReservations() {

        repository.create(createReservation("RES001"));
        repository.create(createReservation("RES002"));

        List<Reservation> result = repository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void updateShouldModifyExistingReservation() {

        repository.create(createReservation("RES001"));

        Reservation updated = createReservation("RES001");
        updated.setGuestCount(3);

        Reservation result =
                repository.update("RES001", updated);

        assertEquals(3, result.getGuestCount());
        assertEquals(3,
                repository.findById("RES001")
                        .orElseThrow()
                        .getGuestCount());
    }

    @Test
    void deleteByIdShouldRemoveReservation() {

        repository.create(createReservation("RES001"));

        boolean result = repository.deleteById("RES001");

        assertTrue(result);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void deleteByIdShouldReturnFalseForMissingReservation() {

        boolean result = repository.deleteById("UNKNOWN");

        assertFalse(result);
    }

    @Test
    void duplicateReservationIdShouldBeRejected() {

        repository.create(createReservation("RES001"));

        assertThrows(
                ValidationException.class,
                () -> repository.create(createReservation("RES001"))
        );
    }

    @Test
    void invalidDatesShouldBeRejected() {

        Reservation reservation = createReservation("RES001");

        reservation.setCheckOutDate(
                LocalDate.of(2026, 12, 5)
        );

        assertThrows(
                ValidationException.class,
                () -> repository.create(reservation)
        );
    }

    @Test
    void zeroGuestCountShouldBeRejected() {

        Reservation reservation = createReservation("RES001");
        reservation.setGuestCount(0);

        assertThrows(
                ValidationException.class,
                () -> repository.create(reservation)
        );
    }

    @Test
    void nullReservationShouldBeRejected() {

        assertThrows(
                ValidationException.class,
                () -> repository.create(null)
        );
    }
}
