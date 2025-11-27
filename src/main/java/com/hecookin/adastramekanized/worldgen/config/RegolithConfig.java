package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for regolith depth variation on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class RegolithConfig {
    private double minDepth;            // Minimum regolith depth
    private double maxDepth;            // Maximum regolith depth
    private double variation;           // Depth variation (0.0-1.0)

    public RegolithConfig() {
        // Default values
        this.minDepth = 1;
        this.maxDepth = 5;
        this.variation = 0.5;
    }

    // Builder pattern methods
    public RegolithConfig minDepth(double minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    public RegolithConfig maxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public RegolithConfig variation(double variation) {
        this.variation = variation;
        return this;
    }

    // Preset: Lunar regolith
    public static RegolithConfig lunar() {
        return new RegolithConfig()
            .minDepth(2)
            .maxDepth(8)
            .variation(0.7);
    }

    // Getters
    public double getMinDepth() { return minDepth; }
    public double getMaxDepth() { return maxDepth; }
    public double getVariation() { return variation; }
}
