package com.hotel.admin.dto;

import java.math.BigDecimal;

    public class DashboardSummary {

        // Room statistics
        private int totalRooms;
        private int availableRooms;
        private int maintenanceRooms;
        private int inactiveRooms;

        // User statistics
        private int totalUsers;
        private int activeUsers;

        // Reservation statistics
        private int totalReservations;
        private int pendingReservations;
        private int confirmedReservations;
        private int cancelledReservations;
        private int completedReservations;

        // Payment statistics
        private int totalPayments;
        private int completedPayments;
        private int refundedPayments;
        private BigDecimal grossCompletedPayments =
                BigDecimal.ZERO;

        public DashboardSummary() {
        }

        // Room getters and setters

        public int getTotalRooms() {
            return totalRooms;
        }

        public void setTotalRooms(int totalRooms) {
            this.totalRooms = totalRooms;
        }

        public int getAvailableRooms() {
            return availableRooms;
        }

        public void setAvailableRooms(int availableRooms) {
            this.availableRooms = availableRooms;
        }

        public int getMaintenanceRooms() {
            return maintenanceRooms;
        }

        public void setMaintenanceRooms(int maintenanceRooms) {
            this.maintenanceRooms = maintenanceRooms;
        }

        public int getInactiveRooms() {
            return inactiveRooms;
        }

        public void setInactiveRooms(int inactiveRooms) {
            this.inactiveRooms = inactiveRooms;
        }

        // User getters and setters

        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getActiveUsers() {
            return activeUsers;
        }

        public void setActiveUsers(int activeUsers) {
            this.activeUsers = activeUsers;
        }

        // Reservation getters and setters

        public int getTotalReservations() {
            return totalReservations;
        }

        public void setTotalReservations(int totalReservations) {
            this.totalReservations = totalReservations;
        }

        public int getPendingReservations() {
            return pendingReservations;
        }

        public void setPendingReservations(int pendingReservations) {
            this.pendingReservations = pendingReservations;
        }

        public int getConfirmedReservations() {
            return confirmedReservations;
        }

        public void setConfirmedReservations(int confirmedReservations) {
            this.confirmedReservations = confirmedReservations;
        }

        public int getCancelledReservations() {
            return cancelledReservations;
        }

        public void setCancelledReservations(int cancelledReservations) {
            this.cancelledReservations = cancelledReservations;
        }

        public int getCompletedReservations() {
            return completedReservations;
        }

        public void setCompletedReservations(int completedReservations) {
            this.completedReservations = completedReservations;
        }

        // Payment getters and setters

        public int getTotalPayments() {
            return totalPayments;
        }

        public void setTotalPayments(int totalPayments) {
            this.totalPayments = totalPayments;
        }

        public int getCompletedPayments() {
            return completedPayments;
        }

        public void setCompletedPayments(int completedPayments) {
            this.completedPayments = completedPayments;
        }

        public int getRefundedPayments() {
            return refundedPayments;
        }

        public void setRefundedPayments(int refundedPayments) {
            this.refundedPayments = refundedPayments;
        }

        public BigDecimal getGrossCompletedPayments() {
            return grossCompletedPayments;
        }

        public void setGrossCompletedPayments(
                BigDecimal grossCompletedPayments) {
            this.grossCompletedPayments =
                    grossCompletedPayments;
        }
    }

