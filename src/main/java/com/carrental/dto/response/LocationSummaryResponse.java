package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Concise home location included as a nested object in vehicle detail responses.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationSummaryResponse {

    UUID id;
    String name;
}
