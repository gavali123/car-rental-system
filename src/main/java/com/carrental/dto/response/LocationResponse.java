package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Location record returned by {@code GET /api/v1/locations}.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationResponse {

    UUID id;
    String name;
    String city;
    String country;
    boolean active;
}
