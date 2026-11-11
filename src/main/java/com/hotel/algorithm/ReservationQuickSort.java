
package com.hotel.algorithm;

import com.hotel.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public class ReservationQuickSort {

    // Public method to sort reservations by check-in date
    public static void sortByCheckInDate(
            List<Reservation> reservations) {

        if (reservations == null) {
            throw new IllegalArgumentException(
                    "Reservation list cannot be null.");
        }

        // Validate reservations before sorting
        for (Reservation reservation : reservations) {

            if (reservation == null
                    || reservation.getCheckInDate() == null) {

                throw new IllegalArgumentException(
                        "Reservation and check-in date are required.");
            }
        }

        quickSort(reservations, 0, reservations.size() - 1);
    }

    // Quick Sort implementation
    private static void quickSort(
            List<Reservation> reservations,
            int low,
            int high) {

        if (low >= high) {
            return;
        }

        // Choose the middle element as pivot
        LocalDate pivot = reservations
                .get(low + (high - low) / 2)
                .getCheckInDate();

        int left = low;
        int current = low;
        int right = high;

        // Three-way partitioning
        while (current <= right) {

            LocalDate currentDate = reservations
                    .get(current)
                    .getCheckInDate();

            int comparison = currentDate.compareTo(pivot);

            if (comparison < 0) {

                swap(reservations, left, current);
                left++;
                current++;

            } else if (comparison > 0) {

                swap(reservations, current, right);
                right--;

            } else {

                current++;
            }
        }

        // Sort the remaining partitions
        quickSort(reservations, low, left - 1);
        quickSort(reservations, right + 1, high);
    }

    // Swap two reservations
    private static void swap(
            List<Reservation> reservations,
            int first,
            int second) {

        Reservation temp = reservations.get(first);

        reservations.set(first, reservations.get(second));

        reservations.set(second, temp);
    }
}
