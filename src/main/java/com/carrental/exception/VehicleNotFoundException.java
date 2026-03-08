package com.carrental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Thrown when no vehicle exists for the requested {@code vehicleId}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(UUID vehicleId) {
        super("Vehicle not found: " + vehicleId);
    }
}
