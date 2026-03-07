package com.carrental.repository;

import com.carrental.domain.entity.Location;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Location} entities.
 *
 * <p>Location records are managed by an administrative module. This repository
 * provides read access used during vehicle onboarding and inventory filtering.
 */
@Repository
public interface LocationRepository extends R2dbcRepository<Location, UUID> {

    /**
     * Returns all active (operational) locations.
     *
     * @return a {@link Flux} of active locations
     */
    Flux<Location> findByActiveTrue();

    /**
     * Returns an active location by its ID, or empty if the location does not exist
     * or is inactive.
     *
     * @param id the location UUID
     * @return a {@link Mono} emitting the active location, or empty
     */
    Mono<Location> findByIdAndActiveTrue(UUID id);
}
