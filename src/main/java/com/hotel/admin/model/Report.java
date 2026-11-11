package com.hotel.admin.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

    public class Report {

        private String id;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generatedAt;

        private int totalRooms;
        private int totalReservations;
        private BigDecimal totalRevenue;

        private String notes;

        // Required for Jackson JSON deserialization
        public Report() {
        }

        public Report(
                String id,
                String title,
                LocalDate startDate,
                LocalDate endDate,
                LocalDateTime generatedAt,
                int totalRooms,
                int totalReservations,
                BigDecimal totalRevenue,
                String notes) {

            this.id = id;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.generatedAt = generatedAt;
            this.totalRooms = totalRooms;
            this.totalReservations = totalReservations;
            this.totalRevenue = totalRevenue;
            this.notes = notes;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public int getTotalRooms() {
            return totalRooms;
        }

        public void setTotalRooms(int totalRooms) {
            this.totalRooms = totalRooms;
        }

        public int getTotalReservations() {
            return totalReservations;
        }

        public void setTotalReservations(int totalReservations) {
            this.totalReservations = totalReservations;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }


    }