package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * Standardised error response body returned by the {@code GlobalExceptionHandler}.
 *
 * <p>Field-level validation errors are included in {@link #fieldErrors} when present.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** Short machine-readable error code (e.g., {@code VALIDATION_ERROR}). */
    String errorCode;

    /** Human-readable description of the error. */
    String message;

    /**
     * Per-field validation errors.
     * Key = field name, Value = validation message.
     */
    Map<String, String> fieldErrors;

    /** Optional list of global validation errors (not tied to a specific field). */
    List<String> errors;
}
