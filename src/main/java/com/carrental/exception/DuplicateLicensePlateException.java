package com.carrental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a vehicle with the same license plate already exists in the system.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLicensePlateException extends RuntimeException {

    public DuplicateLicensePlateException(String licensePlate) {
        super("A vehicle with license plate '" + licensePlate + "' already exists");
    }
}
