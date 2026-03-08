package com.carrental.repository;

import com.carrental.domain.entity.Vehicle;
import com.carrental.domain.enums.LifecycleStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Vehicle} entities.
 *
 * <p>Extends {@link R2dbcRepository} which provides standard CRUD operations over a
 * reactive PostgreSQL connection (R2DBC). Custom query methods follow Spring Data
 * derived-query naming conventions.
 */
@Repository
public interface VehicleRepository extends R2dbcRepository<Vehicle, UUID> {

    /**
     * Returns the vehicle with the given VIN, if present.
     *
     * @param vin the vehicle identification number to search for
     * @return a {@link Mono} emitting the matching vehicle, or empty
     */
    Mono<Vehicle> findByVin(String vin);

    /**
     * Returns the vehicle with the given license plate (case-insensitive stored as
     * uppercase), if present.
     *
     * @param licensePlate the license plate to search for (uppercase)
     * @return a {@link Mono} emitting the matching vehicle, or empty
     */
    Mono<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Returns all vehicles assigned to the given home location.
     *
     * @param homeLocationId the location UUID
     * @return a {@link Flux} of matching vehicles
     */
    Flux<Vehicle> findByHomeLocationId(UUID homeLocationId);

    /**
     * Returns all vehicles currently in the given lifecycle status.
     *
     * @param lifecycleStatus the status to filter on
     * @return a {@link Flux} of matching vehicles
     */
    Flux<Vehicle> findByLifecycleStatus(LifecycleStatus lifecycleStatus);

    /**
     * Checks whether a vehicle with the given VIN already exists.
     *
     * @param vin the VIN to check
     * @return a {@link Mono} emitting {@code true} if a duplicate exists
     */
    Mono<Boolean> existsByVin(String vin);

    /**
     * Checks whether a vehicle with the given license plate already exists.
     *
     * @param licensePlate the license plate to check (uppercase)
     * @return a {@link Mono} emitting {@code true} if a duplicate exists
     */
    Mono<Boolean> existsByLicensePlate(String licensePlate);
}
