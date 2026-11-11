
package com.hotel.common;

public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message, 400);
    }
}
