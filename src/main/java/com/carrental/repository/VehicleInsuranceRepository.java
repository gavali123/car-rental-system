package com.carrental.repository;

import com.carrental.domain.entity.VehicleInsurance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reactive repository for {@link VehicleInsurance} entities.
 */
@Repository
public interface VehicleInsuranceRepository extends R2dbcRepository<VehicleInsurance, UUID> {

    /**
     * Returns the active insurance record for a vehicle.
     *
     * @param vehicleId the vehicle UUID
     * @return a {@link Mono} emitting the active insurance record, or empty if none
     */
    Mono<VehicleInsurance> findByVehicleIdAndActiveTrue(UUID vehicleId);

    /**
     * Returns all insurance records for a vehicle (active and historical).
     *
     * @param vehicleId the vehicle UUID
     * @return a {@link Flux} of all insurance records for the vehicle
     */
    Flux<VehicleInsurance> findByVehicleId(UUID vehicleId);

    /**
     * Returns all active insurance records expiring on or before the given date.
     * Used by the daily insurance expiry scan (FR-3).
     *
     * @param date the cut-off date
     * @return a {@link Flux} of expiring active insurance records
     */
    Flux<VehicleInsurance> findByActiveTrueAndCoverageEndDateLessThanEqual(LocalDate date);
}
