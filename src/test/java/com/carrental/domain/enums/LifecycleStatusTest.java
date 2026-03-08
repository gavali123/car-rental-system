package com.carrental.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LifecycleStatus} transition matrix.
 */
@DisplayName("LifecycleStatus transition matrix")
class LifecycleStatusTest {

    @ParameterizedTest(name = "{0} -> {1} should be allowed")
    @CsvSource({
            "INCOMING, ACTIVE",
            "INCOMING, DECOMMISSIONING",
            "ACTIVE, MAINTENANCE",
            "ACTIVE, DECOMMISSIONING",
            "MAINTENANCE, ACTIVE",
            "MAINTENANCE, DECOMMISSIONING",
            "DECOMMISSIONING, SOLD"
    })
    void allowedTransitions(String from, String to) {
        LifecycleStatus fromStatus = LifecycleStatus.valueOf(from);
        LifecycleStatus toStatus = LifecycleStatus.valueOf(to);
        assertThat(fromStatus.canTransitionTo(toStatus)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} should be rejected")
    @CsvSource({
            "INCOMING, MAINTENANCE",
            "INCOMING, SOLD",
            "ACTIVE, INCOMING",
            "ACTIVE, SOLD",
            "MAINTENANCE, INCOMING",
            "MAINTENANCE, SOLD",
            "DECOMMISSIONING, INCOMING",
            "DECOMMISSIONING, ACTIVE",
            "DECOMMISSIONING, MAINTENANCE",
            "SOLD, INCOMING",
            "SOLD, ACTIVE",
            "SOLD, MAINTENANCE",
            "SOLD, DECOMMISSIONING"
    })
    void rejectedTransitions(String from, String to) {
        LifecycleStatus fromStatus = LifecycleStatus.valueOf(from);
        LifecycleStatus toStatus = LifecycleStatus.valueOf(to);
        assertThat(fromStatus.canTransitionTo(toStatus)).isFalse();
    }

    @Test
    @DisplayName("same-status transition should be rejected")
    void sameStatusTransitionRejected() {
        for (LifecycleStatus status : LifecycleStatus.values()) {
            assertThat(status.canTransitionTo(status)).isFalse();
        }
    }

    @Test
    @DisplayName("SOLD should be terminal")
    void soldIsTerminal() {
        assertThat(LifecycleStatus.SOLD.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("Non-SOLD statuses should not be terminal")
    void nonSoldIsNotTerminal() {
        for (LifecycleStatus status : LifecycleStatus.values()) {
            if (status != LifecycleStatus.SOLD) {
                assertThat(status.isTerminal()).isFalse();
            }
        }
    }
}
