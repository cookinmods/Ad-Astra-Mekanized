package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for volcanic features on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class VolcanoConfig {
    private double frequency;           // Volcano density
    private double minHeight;           // Minimum volcano height
    private double maxHeight;           // Maximum volcano height
    private double slopeAngle;          // Slope steepness (degrees)
    private double calderaSize;         // Caldera radius as fraction of base
    private boolean lavaFlows;          // Generate lava flows
    private String type;                // "shield", "stratovolcano", "cinder_cone"

    public VolcanoConfig() {
        // Default values
        this.frequency = 0.002;
        this.minHeight = 50;
        this.maxHeight = 120;
        this.slopeAngle = 15.0;
        this.calderaSize = 0.1;
        this.lavaFlows = false;
        this.type = "stratovolcano";
    }

    // Builder pattern methods
    public VolcanoConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public VolcanoConfig minHeight(double minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    public VolcanoConfig maxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public VolcanoConfig slopeAngle(double slopeAngle) {
        this.slopeAngle = slopeAngle;
        return this;
    }

    public VolcanoConfig calderaSize(double calderaSize) {
        this.calderaSize = calderaSize;
        return this;
    }

    public VolcanoConfig lavaFlows(boolean lavaFlows) {
        this.lavaFlows = lavaFlows;
        return this;
    }

    public VolcanoConfig type(String type) {
        this.type = type;
        return this;
    }

    // Preset: Olympus Mons-style shield volcano
    public static VolcanoConfig olympusMons() {
        return new VolcanoConfig()
            .frequency(0.005)
            .minHeight(100)
            .maxHeight(250)
            .slopeAngle(5.0)
            .calderaSize(0.15)
            .lavaFlows(true)
            .type("shield");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getMinHeight() { return minHeight; }
    public double getMaxHeight() { return maxHeight; }
    public double getSlopeAngle() { return slopeAngle; }
    public double getCalderaSize() { return calderaSize; }
    public boolean hasLavaFlows() { return lavaFlows; }
    public String getType() { return type; }
}
