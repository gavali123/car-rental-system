package com.carrental.service;

import com.carrental.domain.entity.Location;
import com.carrental.domain.entity.Vehicle;
import com.carrental.domain.entity.VehicleInsurance;
import com.carrental.domain.enums.FuelType;
import com.carrental.domain.enums.LifecycleStatus;
import com.carrental.domain.enums.SizeType;
import com.carrental.domain.enums.VehicleCategory;
import com.carrental.domain.enums.VehicleClass;
import com.carrental.dto.request.InsuranceRequest;
import com.carrental.dto.request.RegisterVehicleRequest;
import com.carrental.dto.request.UpdateLifecycleStatusRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VehicleService}.
 *
 * <p>Uses Mockito to stub all repository dependencies so that tests run without a
 * real database connection.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleInsuranceRepository insuranceRepository;

    @Mock
    private VehicleStatusHistoryRepository statusHistoryRepository;

    @Mock
    private LocationRepository locationRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(
                vehicleRepository,
                insuranceRepository,
                statusHistoryRepository,
                locationRepository);
    }

    // ── Register Vehicle ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerVehicle")
    class RegisterVehicleTests {

        private RegisterVehicleRequest validRequest;
        private UUID locationId;

        @BeforeEach
        void setUpRequest() {
            locationId = UUID.randomUUID();
            validRequest = RegisterVehicleRequest.builder()
                    .vin("ABC12345678901234")
                    .licensePlate("ABC-1234")
                    .purchaseDate(LocalDate.now().minusDays(10))
                    .purchaseCost(BigDecimal.valueOf(25000))
                    .odometerAtAcquisition(0)
                    .brand("Toyota")
                    .model("Camry")
                    .manufacturingYear((short) (LocalDate.now().getYear()))
                    .sizeType("SMALL")
                    .vehicleClass("ECONOMY")
                    .numberOfSeats((short) 4)
                    .fuelType("GAS")
                    .homeLocationId(locationId)
                    .insurance(InsuranceRequest.builder()
                            .insurerName("SafeGuard Insurance")
                            .policyNumber("POL-001")
                            .coverageStartDate(LocalDate.now())
                            .coverageEndDate(LocalDate.now().plusYears(1))
                            .build())
                    .build();
        }

        @Test
        @DisplayName("should register a vehicle successfully and return INCOMING status")
        void registerVehicle_success() {
            UUID vehicleId = UUID.randomUUID();
            Location location = Location.builder()
                    .id(locationId)
                    .name("Downtown Branch")
                    .active(true)
                    .build();
            Vehicle savedVehicle = Vehicle.builder()
                    .id(vehicleId)
                    .vin("ABC12345678901234")
                    .licensePlate("ABC-1234")
                    .lifecycleStatus(LifecycleStatus.INCOMING)
                    .vehicleCategory(VehicleCategory.ECONOMY_SMALL)
                    .createdAt(OffsetDateTime.now())
                    .build();
            VehicleInsurance savedInsurance = VehicleInsurance.builder()
                    .id(UUID.randomUUID())
                    .vehicleId(vehicleId)
                    .active(true)
                    .build();

            when(vehicleRepository.existsByVin("ABC12345678901234")).thenReturn(Mono.just(false));
            when(vehicleRepository.existsByLicensePlate("ABC-1234")).thenReturn(Mono.just(false));
            when(locationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Mono.just(location));
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(Mono.just(savedVehicle));
            when(insuranceRepository.save(any(VehicleInsurance.class))).thenReturn(Mono.just(savedInsurance));

            StepVerifier.create(vehicleService.registerVehicle(validRequest))
                    .assertNext(response -> {
                        assertThat(response.getId()).isEqualTo(vehicleId);
                        assertThat(response.getVin()).isEqualTo("ABC12345678901234");
                        assertThat(response.getLifecycleStatus()).isEqualTo("INCOMING");
                        assertThat(response.getVehicleCategory()).isEqualTo("ECONOMY_SMALL");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject registration when VIN already exists")
        void registerVehicle_duplicateVin() {
            when(vehicleRepository.existsByVin("ABC12345678901234")).thenReturn(Mono.just(true));

            StepVerifier.create(vehicleService.registerVehicle(validRequest))
                    .expectError(DuplicateVinException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject registration when license plate already exists")
        void registerVehicle_duplicateLicensePlate() {
            when(vehicleRepository.existsByVin("ABC12345678901234")).thenReturn(Mono.just(false));
            when(vehicleRepository.existsByLicensePlate("ABC-1234")).thenReturn(Mono.just(true));

            StepVerifier.create(vehicleService.registerVehicle(validRequest))
                    .expectError(DuplicateLicensePlateException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject registration when location is not found")
        void registerVehicle_locationNotFound() {
            when(vehicleRepository.existsByVin("ABC12345678901234")).thenReturn(Mono.just(false));
            when(vehicleRepository.existsByLicensePlate("ABC-1234")).thenReturn(Mono.just(false));
            when(locationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Mono.empty());

            StepVerifier.create(vehicleService.registerVehicle(validRequest))
                    .expectError(LocationNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject registration when VIN is not exactly 17 alphanumeric characters")
        void registerVehicle_invalidVin() {
            RegisterVehicleRequest badRequest = RegisterVehicleRequest.builder()
                    .vin("SHORT")
                    .licensePlate("ABC-1234")
                    .purchaseDate(LocalDate.now().minusDays(1))
                    .purchaseCost(BigDecimal.TEN)
                    .odometerAtAcquisition(0)
                    .brand("Toyota")
                    .model("Camry")
                    .manufacturingYear((short) 2024)
                    .sizeType("SMALL")
                    .vehicleClass("ECONOMY")
                    .numberOfSeats((short) 4)
                    .fuelType("GAS")
                    .homeLocationId(UUID.randomUUID())
                    .insurance(InsuranceRequest.builder()
                            .insurerName("Insurer")
                            .policyNumber("POL-001")
                            .coverageStartDate(LocalDate.now())
                            .coverageEndDate(LocalDate.now().plusYears(1))
                            .build())
                    .build();

            StepVerifier.create(vehicleService.registerVehicle(badRequest))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    // ── Get Vehicle Detail ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getVehicleDetail")
    class GetVehicleDetailTests {

        @Test
        @DisplayName("should return vehicle detail when vehicle exists")
        void getVehicleDetail_found() {
            UUID vehicleId = UUID.randomUUID();
            UUID locationId = UUID.randomUUID();
            Vehicle vehicle = Vehicle.builder()
                    .id(vehicleId)
                    .vin("ABC12345678901234")
                    .licensePlate("ABC-1234")
                    .sizeType(SizeType.SMALL)
                    .vehicleClass(VehicleClass.ECONOMY)
                    .vehicleCategory(VehicleCategory.ECONOMY_SMALL)
                    .fuelType(FuelType.GAS)
                    .lifecycleStatus(LifecycleStatus.INCOMING)
                    .homeLocationId(locationId)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            Location location = Location.builder()
                    .id(locationId)
                    .name("Airport Hub")
                    .build();
            VehicleInsurance insurance = VehicleInsurance.builder()
                    .id(UUID.randomUUID())
                    .vehicleId(vehicleId)
                    .insurerName("Insurer")
                    .policyNumber("POL-001")
                    .coverageStartDate(LocalDate.now())
                    .coverageEndDate(LocalDate.now().plusYears(1))
                    .active(true)
                    .build();

            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.just(vehicle));
            when(locationRepository.findById(locationId)).thenReturn(Mono.just(location));
            when(insuranceRepository.findByVehicleIdAndActiveTrue(vehicleId))
                    .thenReturn(Mono.just(insurance));

            StepVerifier.create(vehicleService.getVehicleDetail(vehicleId))
                    .assertNext((VehicleDetailResponse response) -> {
                        assertThat(response.getId()).isEqualTo(vehicleId);
                        assertThat(response.getLifecycleStatus()).isEqualTo("INCOMING");
                        assertThat(response.getHomeLocation()).isNotNull();
                        assertThat(response.getHomeLocation().getName()).isEqualTo("Airport Hub");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw VehicleNotFoundException when vehicle does not exist")
        void getVehicleDetail_notFound() {
            UUID vehicleId = UUID.randomUUID();
            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.empty());

            StepVerifier.create(vehicleService.getVehicleDetail(vehicleId))
                    .expectError(VehicleNotFoundException.class)
                    .verify();
        }
    }

    // ── Update Lifecycle Status ───────────────────────────────────────────────

    @Nested
    @DisplayName("updateLifecycleStatus")
    class UpdateLifecycleStatusTests {

        @Test
        @DisplayName("should transition INCOMING -> ACTIVE successfully")
        void updateStatus_incomingToActive() {
            UUID vehicleId = UUID.randomUUID();
            UUID actingUserId = UUID.randomUUID();
            Vehicle vehicle = Vehicle.builder()
                    .id(vehicleId)
                    .lifecycleStatus(LifecycleStatus.INCOMING)
                    .build();
            UpdateLifecycleStatusRequest request = UpdateLifecycleStatusRequest.builder()
                    .status("ACTIVE")
                    .notes("Passed inspection")
                    .build();

            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.just(vehicle));
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(Mono.just(vehicle));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(vehicleService.updateLifecycleStatus(
                            vehicleId, request, actingUserId, "manager@example.com"))
                    .assertNext(response -> {
                        assertThat(response.getPreviousStatus()).isEqualTo("INCOMING");
                        assertThat(response.getCurrentStatus()).isEqualTo("ACTIVE");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject invalid transition ACTIVE -> INCOMING")
        void updateStatus_invalidTransition() {
            UUID vehicleId = UUID.randomUUID();
            Vehicle vehicle = Vehicle.builder()
                    .id(vehicleId)
                    .lifecycleStatus(LifecycleStatus.ACTIVE)
                    .build();
            UpdateLifecycleStatusRequest request = UpdateLifecycleStatusRequest.builder()
                    .status("INCOMING")
                    .build();

            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.just(vehicle));

            StepVerifier.create(vehicleService.updateLifecycleStatus(
                            vehicleId, request, UUID.randomUUID(), "manager@example.com"))
                    .expectError(InvalidLifecycleTransitionException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject any transition from terminal SOLD state")
        void updateStatus_soldIsTerminal() {
            UUID vehicleId = UUID.randomUUID();
            Vehicle vehicle = Vehicle.builder()
                    .id(vehicleId)
                    .lifecycleStatus(LifecycleStatus.SOLD)
                    .build();
            UpdateLifecycleStatusRequest request = UpdateLifecycleStatusRequest.builder()
                    .status("ACTIVE")
                    .build();

            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.just(vehicle));

            StepVerifier.create(vehicleService.updateLifecycleStatus(
                            vehicleId, request, UUID.randomUUID(), "manager@example.com"))
                    .expectError(InvalidLifecycleTransitionException.class)
                    .verify();
        }

        @Test
        @DisplayName("should throw VehicleNotFoundException when vehicle does not exist")
        void updateStatus_vehicleNotFound() {
            UUID vehicleId = UUID.randomUUID();
            when(vehicleRepository.findById(vehicleId)).thenReturn(Mono.empty());
            UpdateLifecycleStatusRequest request = UpdateLifecycleStatusRequest.builder()
                    .status("ACTIVE")
                    .build();

            StepVerifier.create(vehicleService.updateLifecycleStatus(
                            vehicleId, request, UUID.randomUUID(), "user@example.com"))
                    .expectError(VehicleNotFoundException.class)
                    .verify();
        }
    }
}
