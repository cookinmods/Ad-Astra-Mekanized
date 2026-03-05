package com.hecookin.adastramekanized.api;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Interface for fuel system integration (primarily with Immersive Engineering)
 *
 * This allows our mod to work with or without IE's fuel system,
 * providing graceful fallbacks when the integration is not available.
 */
public interface IFuelIntegration {

    /**
     * Check if the fuel integration system is available and loaded
     * @return true if fuel APIs are available
     */
    boolean isFuelSystemAvailable();

    /**
     * Check if a fluid can be used as rocket fuel
     * @param fluid The fluid to check
     * @return true if this fluid is valid rocket fuel
     */
    boolean isValidRocketFuel(FluidStack fluid);

    /**
     * Check if an item can be used as rocket fuel
     * @param item The item to check
     * @return true if this item is valid rocket fuel
     */
    boolean isValidRocketFuel(ItemStack item);

}