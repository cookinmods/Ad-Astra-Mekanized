package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for canyon generation on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class CanyonConfig {
    private double frequency;           // Canyon network density
    private double minDepth;            // Minimum canyon depth in blocks
    private double maxDepth;            // Maximum canyon depth in blocks
    private double width;               // Average canyon width in blocks
    private double sinuosity;           // How winding canyons are (0.0-1.0)
    private boolean branches;           // Allow canyon branches
    private String pattern;             // "radial", "dendritic", "parallel"

    public CanyonConfig() {
        // Default values
        this.frequency = 0.005;
        this.minDepth = 30;
        this.maxDepth = 80;
        this.width = 100;
        this.sinuosity = 0.2;
        this.branches = false;
        this.pattern = "dendritic";
    }

    // Builder pattern methods
    public CanyonConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public CanyonConfig minDepth(double minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    public CanyonConfig maxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public CanyonConfig width(double width) {
        this.width = width;
        return this;
    }

    public CanyonConfig sinuosity(double sinuosity) {
        this.sinuosity = sinuosity;
        return this;
    }

    public CanyonConfig branches(boolean branches) {
        this.branches = branches;
        return this;
    }

    public CanyonConfig pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    // Preset: Valles Marineris-style canyons
    public static CanyonConfig vallesMarineris() {
        return new CanyonConfig()
            .frequency(0.01)
            .minDepth(50)
            .maxDepth(150)
            .width(200)
            .sinuosity(0.3)
            .branches(true)
            .pattern("radial");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getMinDepth() { return minDepth; }
    public double getMaxDepth() { return maxDepth; }
    public double getWidth() { return width; }
    public double getSinuosity() { return sinuosity; }
    public boolean hasBranches() { return branches; }
    public String getPattern() { return pattern; }
}
