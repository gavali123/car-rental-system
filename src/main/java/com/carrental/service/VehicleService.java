package com.carrental.service;

import com.carrental.domain.entity.Vehicle;
import com.carrental.domain.entity.VehicleInsurance;
import com.carrental.domain.entity.VehicleStatusHistory;
import com.carrental.domain.enums.FuelType;
import com.carrental.domain.enums.LifecycleStatus;
import com.carrental.domain.enums.SizeType;
import com.carrental.domain.enums.VehicleCategory;
import com.carrental.domain.enums.VehicleClass;
import com.carrental.dto.request.RegisterVehicleRequest;
import com.carrental.dto.request.UpdateLifecycleStatusRequest;
import com.carrental.dto.response.InsuranceResponse;
import com.carrental.dto.response.LifecycleHistoryEntryResponse;
import com.carrental.dto.response.LifecycleHistoryResponse;
import com.carrental.dto.response.LifecycleStatusUpdateResponse;
import com.carrental.dto.response.LocationSummaryResponse;
import com.carrental.dto.response.VehicleCreatedResponse;
import com.carrental.dto.response.VehicleDetailResponse;
import com.carrental.exception.DuplicateLicensePlateException;
import com.carrental.exception.DuplicateVinException;
import com.carrental.exception.InvalidLifecycleTransitionException;
import com.carrental.exception.LocationNotFoundException;
import com.carrental.exception.VehicleNotFoundException;
import com.carrental.repository.LocationRepository;
import com.carrental.repository.VehicleInsuranceRepository;
import com.carrental.repository.VehicleRepository;
import com.carrental.repository.VehicleStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for vehicle onboarding and lifecycle management.
 *
 * <p>Follows the service layer responsibilities:
 * <ul>
 *   <li>Validates business rules beyond annotation-level constraints.</li>
 *   <li>Orchestrates repository calls within transactional boundaries.</li>
 *   <li>Maps domain entities to DTOs; never exposes entities to the controller layer.</li>
 * </ul>
 */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final VehicleInsuranceRepository insuranceRepository;
    private final VehicleStatusHistoryRepository statusHistoryRepository;
    private final LocationRepository locationRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            VehicleInsuranceRepository insuranceRepository,
            VehicleStatusHistoryRepository statusHistoryRepository,
            LocationRepository locationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.insuranceRepository = insuranceRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.locationRepository = locationRepository;
    }

    // ── Register Vehicle (FR-1) ───────────────────────────────────────────────

    /**
     * Registers a new vehicle into the rental fleet.
     *
     * <p>Validates business rules BRV-01 through BRV-13, BRU-01, and BRU-02 before
     * persisting the vehicle and its initial insurance record.
     *
     * @param request the vehicle registration request
     * @return a {@link Mono} emitting the created vehicle summary
     */
    @Transactional
    public Mono<VehicleCreatedResponse> registerVehicle(RegisterVehicleRequest request) {
        log.info("Registering vehicle with VIN: {}", request.getVin());

        return validateVehicleRequest(request)
                .then(Mono.defer(() -> locationRepository.findByIdAndActiveTrue(request.getHomeLocationId())
                        .switchIfEmpty(Mono.error(new LocationNotFoundException(request.getHomeLocationId())))))
                .flatMap(location -> {
                    SizeType sizeType = parseSizeType(request.getSizeType());
                    VehicleClass vehicleClass = parseVehicleClass(request.getVehicleClass());
                    FuelType fuelType = parseFuelType(request.getFuelType());

                    validateInsuranceDates(request);
                    validatePurchaseDate(request.getPurchaseDate());
                    validateManufacturingYear(request.getManufacturingYear());

                    VehicleCategory vehicleCategory = VehicleCategory.from(sizeType, vehicleClass);
                    OffsetDateTime now = OffsetDateTime.now();

                    Vehicle vehicle = Vehicle.builder()
                            .id(UUID.randomUUID())
                            .vin(request.getVin().toUpperCase())
                            .licensePlate(request.getLicensePlate().toUpperCase())
                            .purchaseDate(request.getPurchaseDate())
                            .purchaseCost(request.getPurchaseCost())
                            .odometerAtAcquisition(request.getOdometerAtAcquisition())
                            .brand(request.getBrand())
                            .model(request.getModel())
                            .manufacturingYear(request.getManufacturingYear())
                            .sizeType(sizeType)
                            .vehicleClass(vehicleClass)
                            .vehicleCategory(vehicleCategory)
                            .numberOfSeats(request.getNumberOfSeats())
                            .fuelType(fuelType)
                            .lifecycleStatus(LifecycleStatus.INCOMING)
                            .homeLocationId(request.getHomeLocationId())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return vehicleRepository.save(vehicle);
                })
                .flatMap(savedVehicle -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    VehicleInsurance insurance = VehicleInsurance.builder()
                            .id(UUID.randomUUID())
                            .vehicleId(savedVehicle.getId())
                            .insurerName(request.getInsurance().getInsurerName())
                            .policyNumber(request.getInsurance().getPolicyNumber())
                            .coverageStartDate(request.getInsurance().getCoverageStartDate())
                            .coverageEndDate(request.getInsurance().getCoverageEndDate())
                            .active(true)
                            .createdAt(now)
                            .build();
                    return insuranceRepository.save(insurance).thenReturn(savedVehicle);
                })
                .map(vehicle -> VehicleCreatedResponse.builder()
                        .id(vehicle.getId())
                        .vin(vehicle.getVin())
                        .licensePlate(vehicle.getLicensePlate())
                        .lifecycleStatus(vehicle.getLifecycleStatus().name())
                        .vehicleCategory(vehicle.getVehicleCategory().name())
                        .createdAt(vehicle.getCreatedAt())
                        .build())
                .doOnSuccess(r -> log.info("Vehicle registered successfully: {}", r.getId()));
    }

    // ── Get Vehicle Detail (FR-1) ─────────────────────────────────────────────

    /**
     * Retrieves full vehicle details including home location summary and active insurance.
     *
     * @param vehicleId the vehicle UUID
     * @return a {@link Mono} emitting the full vehicle detail
     */
    public Mono<VehicleDetailResponse> getVehicleDetail(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(new VehicleNotFoundException(vehicleId)))
                .flatMap(vehicle -> {
                    Mono<LocationSummaryResponse> locationMono = locationRepository
                            .findById(vehicle.getHomeLocationId())
                            .map(l -> LocationSummaryResponse.builder()
                                    .id(l.getId())
                                    .name(l.getName())
                                    .build())
                            .defaultIfEmpty(LocationSummaryResponse.builder()
                                    .id(vehicle.getHomeLocationId())
                                    .build());

                    Mono<InsuranceResponse> insuranceMono = insuranceRepository
                            .findByVehicleIdAndActiveTrue(vehicleId)
                            .map(ins -> InsuranceResponse.builder()
                                    .id(ins.getId())
                                    .insurerName(ins.getInsurerName())
                                    .policyNumber(ins.getPolicyNumber())
                                    .coverageStartDate(ins.getCoverageStartDate())
                                    .coverageEndDate(ins.getCoverageEndDate())
                                    .build())
                            .defaultIfEmpty(InsuranceResponse.builder().build());

                    return Mono.zip(locationMono, insuranceMono)
                            .map(tuple -> toDetailResponse(vehicle, tuple.getT1(), tuple.getT2()));
                });
    }

    // ── Update Lifecycle Status (FR-2) ────────────────────────────────────────

    /**
     * Updates a vehicle's lifecycle status according to the transition matrix defined
     * in FR-2. Records the change in the immutable status history table.
     *
     * @param vehicleId  the vehicle UUID
     * @param request    the status update request
     * @param actingUserId  UUID of the authenticated fleet manager
     * @param actingUserEmail  email of the authenticated fleet manager
     * @return a {@link Mono} emitting the status update result
     */
    @Transactional
    public Mono<LifecycleStatusUpdateResponse> updateLifecycleStatus(
            UUID vehicleId,
            UpdateLifecycleStatusRequest request,
            UUID actingUserId,
            String actingUserEmail) {

        LifecycleStatus targetStatus = parseLifecycleStatus(request.getStatus());

        return vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(new VehicleNotFoundException(vehicleId)))
                .flatMap(vehicle -> {
                    LifecycleStatus current = vehicle.getLifecycleStatus();

                    if (!current.canTransitionTo(targetStatus)) {
                        return Mono.error(new InvalidLifecycleTransitionException(current, targetStatus));
                    }

                    OffsetDateTime now = OffsetDateTime.now();
                    vehicle.setLifecycleStatus(targetStatus);
                    vehicle.setUpdatedAt(now);

                    VehicleStatusHistory historyEntry = VehicleStatusHistory.builder()
                            .id(UUID.randomUUID())
                            .vehicleId(vehicleId)
                            .previousStatus(current)
                            .newStatus(targetStatus)
                            .changedByUserId(actingUserId)
                            .changedAt(now)
                            .notes(request.getNotes())
                            .createdAt(now)
                            .updatedAt(now)
                            .createdBy(actingUserEmail)
                            .updatedBy(actingUserEmail)
                            .deleted(false)
                            .build();

                    return vehicleRepository.save(vehicle)
                            .flatMap(saved -> statusHistoryRepository.save(historyEntry)
                                    .thenReturn(LifecycleStatusUpdateResponse.builder()
                                            .vehicleId(vehicleId)
                                            .previousStatus(current.name())
                                            .currentStatus(targetStatus.name())
                                            .changedAt(now)
                                            .changedByUserId(actingUserId)
                                            .build()));
                });
    }

    // ── Lifecycle History (FR-2) ──────────────────────────────────────────────

    /**
     * Returns the paginated lifecycle status history for a vehicle.
     *
     * @param vehicleId the vehicle UUID
     * @param page      1-based page number
     * @param pageSize  number of records per page (max 100)
     * @return a {@link Mono} emitting the history response
     */
    public Mono<LifecycleHistoryResponse> getLifecycleHistory(UUID vehicleId, int page, int pageSize) {
        int clampedPageSize = Math.min(pageSize, 100);
        int zeroBasedPage = Math.max(page - 1, 0);

        return vehicleRepository.existsById(vehicleId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new VehicleNotFoundException(vehicleId));
                    }
                    PageRequest pageRequest = PageRequest.of(zeroBasedPage, clampedPageSize);
                    return statusHistoryRepository
                            .findByVehicleIdOrderByChangedAtDesc(vehicleId, pageRequest)
                            .map(entry -> LifecycleHistoryEntryResponse.builder()
                                    .id(entry.getId())
                                    .previousStatus(entry.getPreviousStatus().name())
                                    .newStatus(entry.getNewStatus().name())
                                    .changedAt(entry.getChangedAt())
                                    .changedByUserId(entry.getChangedByUserId())
                                    .notes(entry.getNotes())
                                    .build())
                            .collectList()
                            .zipWith(statusHistoryRepository.countByVehicleId(vehicleId))
                            .map(tuple -> {
                                List<LifecycleHistoryEntryResponse> history = tuple.getT1();
                                long totalRecords = tuple.getT2();
                                return LifecycleHistoryResponse.builder()
                                        .vehicleId(vehicleId)
                                        .totalRecords(totalRecords)
                                        .page(page)
                                        .pageSize(clampedPageSize)
                                        .history(history)
                                        .build();
                            });
                });
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private Mono<Void> validateVehicleRequest(RegisterVehicleRequest request) {
        String normalizedVin = request.getVin().toUpperCase();
        String normalizedPlate = request.getLicensePlate().toUpperCase();

        if (!normalizedVin.matches("[A-Z0-9]{17}")) {
            return Mono.error(new IllegalArgumentException(
                    "VIN must be exactly 17 alphanumeric characters"));
        }

        return vehicleRepository.existsByVin(normalizedVin)
                .flatMap(vinExists -> {
                    if (Boolean.TRUE.equals(vinExists)) {
                        return Mono.error(new DuplicateVinException(normalizedVin));
                    }
                    return vehicleRepository.existsByLicensePlate(normalizedPlate);
                })
                .flatMap(plateExists -> {
                    if (Boolean.TRUE.equals(plateExists)) {
                        return Mono.error(new DuplicateLicensePlateException(normalizedPlate));
                    }
                    return Mono.<Void>empty();
                });
    }

    private void validatePurchaseDate(LocalDate purchaseDate) {
        if (purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Purchase date must not be in the future");
        }
    }

    private void validateManufacturingYear(short year) {
        int currentYear = LocalDate.now().getYear();
        if (year < 1900 || year > currentYear + 1) {
            throw new IllegalArgumentException(
                    "Manufacturing year must be between 1900 and " + (currentYear + 1));
        }
    }

    private void validateInsuranceDates(RegisterVehicleRequest request) {
        LocalDate start = request.getInsurance().getCoverageStartDate();
        LocalDate end = request.getInsurance().getCoverageEndDate();
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Insurance coverage start date must be before coverage end date");
        }
    }

    // ── Enum parsing helpers ──────────────────────────────────────────────────

    private SizeType parseSizeType(String value) {
        try {
            return SizeType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sizeType: " + value + ". Must be SMALL or MEDIUM");
        }
    }

    private VehicleClass parseVehicleClass(String value) {
        try {
            return VehicleClass.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid vehicleClass: " + value + ". Must be ECONOMY or LUXURY");
        }
    }

    private FuelType parseFuelType(String value) {
        try {
            return FuelType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid fuelType: " + value + ". Must be GAS, ELECTRIC or HYBRID");
        }
    }

    private LifecycleStatus parseLifecycleStatus(String value) {
        try {
            return LifecycleStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: " + value
                    + ". Must be one of INCOMING, ACTIVE, MAINTENANCE, DECOMMISSIONING, SOLD");
        }
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private VehicleDetailResponse toDetailResponse(
            Vehicle vehicle,
            LocationSummaryResponse location,
            InsuranceResponse insurance) {
        return VehicleDetailResponse.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .licensePlate(vehicle.getLicensePlate())
                .purchaseDate(vehicle.getPurchaseDate())
                .purchaseCost(vehicle.getPurchaseCost())
                .odometerAtAcquisition(vehicle.getOdometerAtAcquisition())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .manufacturingYear(vehicle.getManufacturingYear())
                .sizeType(vehicle.getSizeType().name())
                .vehicleClass(vehicle.getVehicleClass().name())
                .vehicleCategory(vehicle.getVehicleCategory().name())
                .numberOfSeats(vehicle.getNumberOfSeats())
                .fuelType(vehicle.getFuelType().name())
                .lifecycleStatus(vehicle.getLifecycleStatus().name())
                .homeLocation(location)
                .insurance(insurance)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
