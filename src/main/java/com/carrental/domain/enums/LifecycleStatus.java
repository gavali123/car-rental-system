package com.carrental.domain.enums;

import java.util.Set;

/**
 * Operational lifecycle state of a rental vehicle.
 *
 * <p>Valid transitions are defined in the status transition matrix (FR-2 TRD). The
 * {@link #SOLD} state is terminal — no further transitions are permitted once a vehicle
 * reaches this state.
 */
public enum LifecycleStatus {

    INCOMING,
    ACTIVE,
    MAINTENANCE,
    DECOMMISSIONING,
    SOLD;

    /**
     * Returns {@code true} if a transition from {@code this} status to {@code target}
     * is permitted by the status transition matrix defined in FR-2.
     *
     * @param target the desired target status
     * @return {@code true} if the transition is allowed
     */
    public boolean canTransitionTo(LifecycleStatus target) {
        if (this == target) {
            return false;
        }
        Set<LifecycleStatus> allowed = switch (this) {
            case INCOMING -> Set.of(ACTIVE, DECOMMISSIONING);
            case ACTIVE -> Set.of(MAINTENANCE, DECOMMISSIONING);
            case MAINTENANCE -> Set.of(ACTIVE, DECOMMISSIONING);
            case DECOMMISSIONING -> Set.of(SOLD);
            case SOLD -> Set.of();
        };
        return allowed.contains(target);
    }

    /**
     * Returns {@code true} if this status is a terminal state (i.e., {@link #SOLD}).
     *
     * @return {@code true} for terminal states
     */
    public boolean isTerminal() {
        return this == SOLD;
    }
}
