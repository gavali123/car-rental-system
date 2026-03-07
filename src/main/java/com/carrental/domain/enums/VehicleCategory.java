package com.carrental.domain.enums;

/**
 * Derived category of a rental vehicle, computed from the combination of
 * {@link SizeType} and {@link VehicleClass}.
 *
 * <p>Stored in the database for query-efficiency (avoids runtime derivation on every
 * reservation allocation query).
 */
public enum VehicleCategory {
    ECONOMY_SMALL,
    ECONOMY_MEDIUM,
    LUXURY_SMALL,
    LUXURY_MEDIUM;

    /**
     * Derives the {@link VehicleCategory} from a {@link SizeType} and a {@link VehicleClass}.
     *
     * @param sizeType     the physical size of the vehicle
     * @param vehicleClass the tier of the vehicle
     * @return the corresponding {@link VehicleCategory}
     */
    public static VehicleCategory from(SizeType sizeType, VehicleClass vehicleClass) {
        return switch (vehicleClass) {
            case ECONOMY -> switch (sizeType) {
                case SMALL -> ECONOMY_SMALL;
                case MEDIUM -> ECONOMY_MEDIUM;
            };
            case LUXURY -> switch (sizeType) {
                case SMALL -> LUXURY_SMALL;
                case MEDIUM -> LUXURY_MEDIUM;
            };
        };
    }
}
