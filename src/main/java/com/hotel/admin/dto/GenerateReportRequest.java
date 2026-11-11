package com.hotel.admin.dto;

import java.time.LocalDate;

public class GenerateReportRequest {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    public GenerateReportRequest() {
    }

    public GenerateReportRequest(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String notes) {

        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}