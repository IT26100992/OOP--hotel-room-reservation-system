package com.hotel.admin.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.admin.model.Report;
import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;
import com.hotel.common.repository.JsonCrudRepository;

import java.math.BigDecimal;
import java.util.List;

    public class ReportRepository extends JsonCrudRepository<Report> {

        public ReportRepository() {
            super(
                    new JsonFileStorage<>(
                            "reports.json",
                            new TypeReference<List<Report>>() {}
                    ),
                    Report::getId
            );
        }

        @Override
        protected void validate(Report report) {

            if (report == null) {
                throw new ValidationException(
                        "Report cannot be null.");
            }

            if (blank(report.getId()) ||
                    blank(report.getTitle()) ||
                    report.getStartDate() == null ||
                    report.getEndDate() == null ||
                    report.getGeneratedAt() == null) {

                throw new ValidationException(
                        "Required report information is missing.");
            }

            if (report.getEndDate()
                    .isBefore(report.getStartDate())) {

                throw new ValidationException(
                        "Report end date cannot be before start date.");
            }

            if (report.getTotalRooms() < 0 ||
                    report.getTotalReservations() < 0 ||
                    report.getTotalRevenue() == null ||
                    report.getTotalRevenue()
                            .compareTo(BigDecimal.ZERO) < 0) {

                throw new ValidationException(
                        "Report totals cannot be negative.");
            }
        }

        private boolean blank(String value) {
            return value == null || value.isBlank();
        }
    }

