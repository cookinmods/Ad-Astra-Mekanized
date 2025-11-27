package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for basin features on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class BasinConfig {
    private double frequency;           // Basin density
    private double minRadius;           // Minimum basin radius
    private double maxRadius;           // Maximum basin radius
    private double depth;               // Basin depth

    public BasinConfig() {
        // Default values
        this.frequency = 0.005;
        this.minRadius = 100;
        this.maxRadius = 300;
        this.depth = 20;
    }

    // Builder pattern methods
    public BasinConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public BasinConfig minRadius(double minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    public BasinConfig maxRadius(double maxRadius) {
        this.maxRadius = maxRadius;
        return this;
    }

    public BasinConfig depth(double depth) {
        this.depth = depth;
        return this;
    }

    // Preset: Caloris-style basin (Mercury)
    public static BasinConfig caloris() {
        return new BasinConfig()
            .frequency(0.01)
            .minRadius(200)
            .maxRadius(600)
            .depth(30);
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getMinRadius() { return minRadius; }
    public double getMaxRadius() { return maxRadius; }
    public double getDepth() { return depth; }
}
