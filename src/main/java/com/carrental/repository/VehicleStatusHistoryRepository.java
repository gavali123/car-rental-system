package com.carrental.repository;

import com.carrental.domain.entity.VehicleStatusHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link VehicleStatusHistory} entries.
 *
 * <p>Records are append-only — no update or delete operations should be performed
 * through this repository.
 */
@Repository
public interface VehicleStatusHistoryRepository extends R2dbcRepository<VehicleStatusHistory, UUID> {

    /**
     * Returns paginated lifecycle history entries for a vehicle, ordered by
     * {@code changedAt} descending (most recent first).
     *
     * @param vehicleId the vehicle UUID
     * @param pageable  pagination parameters
     * @return a {@link Flux} of history entries
     */
    Flux<VehicleStatusHistory> findByVehicleIdOrderByChangedAtDesc(UUID vehicleId, Pageable pageable);

    /**
     * Returns the total count of history entries for a vehicle.
     *
     * @param vehicleId the vehicle UUID
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByVehicleId(UUID vehicleId);
}
