
package com.hotel.search.service;

import com.hotel.common.ResourceNotFoundException;
import com.hotel.search.model.SearchPreference;
import com.hotel.search.repository.SearchPreferenceRepository;
import com.hotel.user.model.User;
import com.hotel.user.repository.UserRepository;
import com.hotel.room.model.RoomType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SearchPreferenceServiceTest {

    private SearchPreferenceRepository preferenceRepository;
    private UserRepository userRepository;
    private SearchPreferenceService service;

    @BeforeEach
    void setUp() {
        preferenceRepository =
                mock(SearchPreferenceRepository.class);

        userRepository = mock(UserRepository.class);

        service = new SearchPreferenceService(
                preferenceRepository,
                userRepository
        );

        // Simulate an existing customer
        User user = new User();
        user.setId("USER-001");

        when(userRepository.findById("USER-001"))
                .thenReturn(Optional.of(user));
    }

    // Reusable example saved search
    private SearchPreference createPreference() {

        return new SearchPreference(
                "SEARCH-001",
                "USER-001",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                2,
                RoomType.DOUBLE,
                new BigDecimal("5000"),
                new BigDecimal("10000")
        );
    }

    @Test
    void createShouldGenerateIdAndSavePreference() {

        SearchPreference preference = createPreference();
        preference.setId(null);

        when(preferenceRepository.create(any(SearchPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SearchPreference result =
                service.createPreference(preference);

        assertNotNull(result.getId());
        assertFalse(result.getId().isBlank());
        assertEquals("USER-001", result.getUserId());

        verify(preferenceRepository).create(preference);
    }

    @Test
    void createWithMissingUserShouldThrowException() {

        SearchPreference preference = createPreference();
        preference.setUserId("USER-999");

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createPreference(preference)
        );

        verify(preferenceRepository, never())
                .create(any(SearchPreference.class));
    }

    @Test
    void getPreferencesByUserShouldFilterResults() {

        SearchPreference ownPreference = createPreference();

        SearchPreference otherPreference = createPreference();
        otherPreference.setId("SEARCH-002");
        otherPreference.setUserId("USER-002");

        when(preferenceRepository.findAll())
                .thenReturn(List.of(
                        ownPreference,
                        otherPreference
                ));

        List<SearchPreference> results =
                service.getPreferencesByUser("USER-001");

        assertEquals(1, results.size());
        assertEquals(
                "SEARCH-001",
                results.get(0).getId()
        );
    }

    @Test
    void getPreferenceByIdShouldReturnCorrectRecord() {

        SearchPreference preference = createPreference();

        when(preferenceRepository.findById("SEARCH-001"))
                .thenReturn(Optional.of(preference));

        SearchPreference result =
                service.getPreferenceById(
                        "SEARCH-001",
                        "USER-001"
                );

        assertEquals("SEARCH-001", result.getId());
        assertEquals("USER-001", result.getUserId());
    }

    @Test
    void anotherUserPreferenceShouldBeRejected() {

        SearchPreference preference = createPreference();
        preference.setUserId("USER-002");

        when(preferenceRepository.findById("SEARCH-001"))
                .thenReturn(Optional.of(preference));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getPreferenceById(
                        "SEARCH-001",
                        "USER-001"
                )
        );
    }

    @Test
    void updateShouldPreserveIdAndOwner() {

        SearchPreference existing = createPreference();

        when(preferenceRepository.findById("SEARCH-001"))
                .thenReturn(Optional.of(existing));

        when(preferenceRepository.update(
                eq("SEARCH-001"),
                any(SearchPreference.class)
        )).thenAnswer(invocation -> invocation.getArgument(1));

        SearchPreference updated = createPreference();

        // Attempt to change ownership and ID
        updated.setId("CHANGED-ID");
        updated.setUserId("USER-002");

        // Legitimate updates to search conditions
        updated.setGuestCount(3);
        updated.setRoomType(RoomType.DELUXE);
        updated.setMaxPrice(new BigDecimal("15000"));

        SearchPreference result =
                service.updatePreference(
                        "SEARCH-001",
                        "USER-001",
                        updated
                );

        assertEquals("SEARCH-001", result.getId());
        assertEquals("USER-001", result.getUserId());

        assertEquals(3, result.getGuestCount());
        assertEquals(RoomType.DELUXE, result.getRoomType());

        assertEquals(
                0,
                new BigDecimal("15000")
                        .compareTo(result.getMaxPrice())
        );

        verify(preferenceRepository).update(
                eq("SEARCH-001"),
                any(SearchPreference.class)
        );
    }

    @Test
    void deleteShouldCallRepository() {

        SearchPreference preference = createPreference();

        when(preferenceRepository.findById("SEARCH-001"))
                .thenReturn(Optional.of(preference));

        when(preferenceRepository.deleteById("SEARCH-001"))
                .thenReturn(true);

        boolean result =
                service.deletePreference(
                        "SEARCH-001",
                        "USER-001"
                );

        assertTrue(result);

        verify(preferenceRepository)
                .deleteById("SEARCH-001");
    }

    @Test
    void deleteMissingPreferenceShouldThrowException() {

        when(preferenceRepository.findById("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deletePreference(
                        "UNKNOWN",
                        "USER-001"
                )
        );

        verify(preferenceRepository, never())
                .deleteById(anyString());
    }
}
