package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Active insurance record details included in vehicle detail responses.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsuranceResponse {

    UUID id;
    String insurerName;
    String policyNumber;
    LocalDate coverageStartDate;
    LocalDate coverageEndDate;
}
