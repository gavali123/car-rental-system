package com.carrental.domain.enums;

/**
 * Fuel system type of a rental vehicle.
 *
 * <ul>
 *   <li>{@link #GAS} – Internal combustion engine.</li>
 *   <li>{@link #ELECTRIC} – Battery electric vehicle.</li>
 *   <li>{@link #HYBRID} – Hybrid engine (gas + electric).</li>
 * </ul>
 */
public enum FuelType {
    GAS,
    ELECTRIC,
    HYBRID
}
