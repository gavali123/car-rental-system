package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response body returned after a successful lifecycle status update
 * ({@code PATCH /api/v1/vehicles/{vehicleId}/lifecycle-status} → {@code 200 OK}).
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleStatusUpdateResponse {

    UUID vehicleId;
    String previousStatus;
    String currentStatus;
    OffsetDateTime changedAt;
    UUID changedByUserId;
}
