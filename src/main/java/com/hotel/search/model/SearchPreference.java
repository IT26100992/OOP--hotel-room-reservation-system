
package com.hotel.search.model;

import com.hotel.room.model.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SearchPreference {

    private String id;
    private String userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guestCount;
    private RoomType roomType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Required for Jackson JSON conversion
    public SearchPreference() {
    }

    public SearchPreference(
            String id,
            String userId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int guestCount,
            RoomType roomType,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        this.id = id;
        this.userId = userId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.roomType = roomType;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
}
