package com.hecookin.adastramekanized.integration;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.IChemicalIntegration;
import com.hecookin.adastramekanized.api.IFuelIntegration;
import com.hecookin.adastramekanized.integration.immersiveengineering.ImmersiveEngineeringIntegration;
import com.hecookin.adastramekanized.integration.mekanism.MekanismIntegration;

/**
 * Central manager for all mod integrations
 *
 * This class provides a single access point for all mod integrations,
 * handles initialization, and provides fallback implementations when
 * integrated mods are not available.
 */
public class ModIntegrationManager {
    private static ModIntegrationManager instance;

    // Integration instances
    private final MekanismIntegration mekanismIntegration;
    private final ImmersiveEngineeringIntegration ieIntegration;

    // Fallback implementations
    private final IChemicalIntegration fallbackChemicalIntegration;
    private final IFuelIntegration fallbackFuelIntegration;

    private ModIntegrationManager() {
        AdAstraMekanized.LOGGER.info("Initializing mod integrations...");

        // Initialize all integrations
        this.mekanismIntegration = new MekanismIntegration();
        this.ieIntegration = new ImmersiveEngineeringIntegration();

        // Initialize fallback implementations
        this.fallbackChemicalIntegration = new FallbackChemicalIntegration();
        this.fallbackFuelIntegration = new FallbackFuelIntegration();

        // Initialize oxygen chemical if Mekanism is available
        if (mekanismIntegration.isChemicalSystemAvailable()) {
            mekanismIntegration.initializeOxygenChemical();
        }

        AdAstraMekanized.LOGGER.info("Mod integrations initialized successfully");
        logIntegrationStatus();
    }

    public static ModIntegrationManager getInstance() {
        if (instance == null) {
            instance = new ModIntegrationManager();
        }
        return instance;
    }

    // === Integration Access Methods ===

    /**
     * Get the chemical integration (Mekanism or fallback)
     * @return Active chemical integration implementation
     */
    public IChemicalIntegration getChemicalIntegration() {
        if (mekanismIntegration.isChemicalSystemAvailable()) {
            return mekanismIntegration;
        }
        return fallbackChemicalIntegration;
    }

    /**
     * Get the fuel integration (IE or fallback)
     * @return Active fuel integration implementation
     */
    public IFuelIntegration getFuelIntegration() {
        if (ieIntegration.isFuelSystemAvailable()) {
            return ieIntegration;
        }
        return fallbackFuelIntegration;
    }

    /**
     * Get the raw Mekanism integration
     * @return Mekanism integration instance
     */
    public MekanismIntegration getMekanismIntegration() {
        return mekanismIntegration;
    }

    /**
     * Get the raw IE integration
     * @return IE integration instance
     */
    public ImmersiveEngineeringIntegration getIEIntegration() {
        return ieIntegration;
    }

    // === Status Methods ===

    /**
     * Check if Mekanism is available
     * @return true if Mekanism integration is available
     */
    public boolean isMekanismAvailable() {
        return mekanismIntegration.isChemicalSystemAvailable();
    }

    /**
     * Check if Immersive Engineering is available
     * @return true if IE integration is available
     */
    public boolean isImmersiveEngineeringAvailable() {
        return ieIntegration.isFuelSystemAvailable();
    }

    /**
     * Check if any chemical system is available
     * @return true if chemical integration is available
     */
    public boolean hasChemicalIntegration() {
        return mekanismIntegration.isChemicalSystemAvailable();
    }

    /**
     * Check if any fuel system is available
     * @return true if fuel integration is available
     */
    public boolean hasFuelIntegration() {
        return ieIntegration.isFuelSystemAvailable();
    }

    /**
     * Get integration status summary
     * @return String describing current integration status
     */
    public String getIntegrationStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Mod Integrations: ");
        status.append("Mekanism=").append(hasChemicalIntegration());
        status.append(", IE=").append(hasFuelIntegration());
        return status.toString();
    }

    private void logIntegrationStatus() {
        AdAstraMekanized.LOGGER.info("=== Integration Status ===");
        AdAstraMekanized.LOGGER.info("Mekanism Chemical: {}", hasChemicalIntegration());
        AdAstraMekanized.LOGGER.info("Immersive Engineering Fuel: {}", hasFuelIntegration());
        AdAstraMekanized.LOGGER.info("========================");
    }

    // === Fallback Implementations ===

    private static class FallbackChemicalIntegration implements IChemicalIntegration {
        @Override
        public boolean isChemicalSystemAvailable() {
            return false;
        }

        @Override
        public void initializeOxygenChemical() {
            // No-op
        }
    }

    private static class FallbackFuelIntegration implements IFuelIntegration {
        @Override
        public boolean isFuelSystemAvailable() {
            return false;
        }

        @Override
        public boolean isValidRocketFuel(net.neoforged.neoforge.fluids.FluidStack fluid) {
            return false;
        }

        @Override
        public boolean isValidRocketFuel(net.minecraft.world.item.ItemStack item) {
            return false;
        }
    }
}