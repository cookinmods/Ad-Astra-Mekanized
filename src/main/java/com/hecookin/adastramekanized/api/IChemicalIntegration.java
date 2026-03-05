package com.hecookin.adastramekanized.api;

/**
 * Interface for chemical system integration (primarily with Mekanism)
 *
 * This allows our mod to work with or without Mekanism's chemical system,
 * providing graceful fallbacks when the integration is not available.
 */
public interface IChemicalIntegration {

    /**
     * Check if the chemical integration system is available and loaded
     * @return true if chemical APIs are available
     */
    boolean isChemicalSystemAvailable();

    /**
     * Initialize oxygen chemical registration if possible
     * This should be called during mod setup
     */
    void initializeOxygenChemical();
}