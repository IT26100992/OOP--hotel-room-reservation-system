package com.hotel.admin.exception;

import com.hotel.common.AppException;

public class InvalidReportDateRangeException
        extends AppException {

    public InvalidReportDateRangeException() {
        super(
                "End date cannot be before start date.",
                400
        );
    }
}