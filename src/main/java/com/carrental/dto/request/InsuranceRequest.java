package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

/**
 * Insurance details submitted as part of vehicle registration.
 *
 * <p>Coverage dates are validated at the business-rule level in the service layer
 * (BRV-12, BRV-13): start must precede end.
 */
@Value
@Builder
@Jacksonized
public class InsuranceRequest {

    /** Name of the insurance provider. */
    @NotBlank
    String insurerName;

    /** Insurance policy reference number. */
    @NotBlank
    String policyNumber;

    /** Start of insurance coverage; must not be after {@link #coverageEndDate}. */
    @NotNull
    LocalDate coverageStartDate;

    /** End of insurance coverage; must be strictly after {@link #coverageStartDate}. */
    @NotNull
    LocalDate coverageEndDate;
}
