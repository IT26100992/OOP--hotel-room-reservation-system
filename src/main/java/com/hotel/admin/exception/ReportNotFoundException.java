package com.hotel.admin.exception;

import com.hotel.common.AppException;

public class ReportNotFoundException extends AppException {

    public ReportNotFoundException(String reportId) {
        super(
                "Report not found: " + reportId,
                404
        );
    }
}