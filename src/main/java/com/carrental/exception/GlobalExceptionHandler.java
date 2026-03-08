package com.carrental.exception;

import com.carrental.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralised exception handler that maps application exceptions to standardised
 * {@link ErrorResponse} payloads.
 *
 * <p>Uses SLF4J for structured logging. Domain exceptions are logged at WARN level;
 * unexpected errors at ERROR level.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Handles {@code @Valid} / {@code @Validated} field-level constraint violations. */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse body = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("One or more fields failed validation")
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFound(VehicleNotFoundException ex) {
        log.warn("Vehicle not found: {}", ex.getMessage());
        return notFound("VEHICLE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        log.warn("Location not found: {}", ex.getMessage());
        return notFound("LOCATION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateVinException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateVin(DuplicateVinException ex) {
        log.warn("Duplicate VIN: {}", ex.getMessage());
        return conflict("DUPLICATE_VIN", ex.getMessage());
    }

    @ExceptionHandler(DuplicateLicensePlateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLicensePlate(DuplicateLicensePlateException ex) {
        log.warn("Duplicate license plate: {}", ex.getMessage());
        return conflict("DUPLICATE_LICENSE_PLATE", ex.getMessage());
    }

    @ExceptionHandler(InvalidLifecycleTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidLifecycleTransitionException ex) {
        log.warn("Invalid lifecycle transition: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .errorCode("INVALID_LIFECYCLE_TRANSITION")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Catch-all for unexpected errors. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse body = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> notFound(String code, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder().errorCode(code).message(message).build());
    }

    private ResponseEntity<ErrorResponse> conflict(String code, String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder().errorCode(code).message(message).build());
    }
}
