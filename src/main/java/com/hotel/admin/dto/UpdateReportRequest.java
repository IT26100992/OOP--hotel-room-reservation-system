package com.hotel.admin.dto;

public class UpdateReportRequest {

    private String title;
    private String notes;

    public UpdateReportRequest() {
    }

    public UpdateReportRequest(
            String title,
            String notes) {

        this.title = title;
        this.notes = notes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}