package com.hotel.user.exception;

import com.hotel.common.AppException;

public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super("Invalid email or password.", 401);
    }
}
