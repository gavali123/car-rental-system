package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Single entry in the lifecycle status history of a vehicle.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleHistoryEntryResponse {

    UUID id;
    String previousStatus;
    String newStatus;
    OffsetDateTime changedAt;
    UUID changedByUserId;
    String notes;
}
