package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
 * Paginated lifecycle history response for
 * {@code GET /api/v1/vehicles/{vehicleId}/lifecycle-history}.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleHistoryResponse {

    UUID vehicleId;
    long totalRecords;
    int page;
    int pageSize;
    List<LifecycleHistoryEntryResponse> history;
}
