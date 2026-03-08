package com.carrental.exception;

import com.carrental.domain.enums.LifecycleStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested lifecycle status transition is not permitted by the
 * transition matrix defined in FR-2.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidLifecycleTransitionException extends RuntimeException {

    public InvalidLifecycleTransitionException(LifecycleStatus from, LifecycleStatus to) {
        super(buildMessage(from, to));
    }

    public InvalidLifecycleTransitionException(String message) {
        super(message);
    }

    private static String buildMessage(LifecycleStatus from, LifecycleStatus to) {
        if (from.isTerminal()) {
            return "SOLD is a terminal state; no further transitions are permitted";
        }
        return "Transition from " + from + " to " + to + " is not permitted";
    }
}
