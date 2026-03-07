package com.carrental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a vehicle with the same VIN already exists in the system.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateVinException extends RuntimeException {

    public DuplicateVinException(String vin) {
        super("A vehicle with VIN '" + vin + "' already exists");
    }
}
