package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for crater generation on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class CraterConfig {
    private double frequency;           // How often craters appear (0.0-1.0)
    private double minSize;             // Minimum crater radius in blocks
    private double maxSize;             // Maximum crater radius in blocks
    private double depth;               // Depth multiplier (1.0 = normal depth)
    private double rimHeight;           // Rim height multiplier (1.0 = normal rim)
    private double rimWidth;            // Rim width as fraction of radius (0.0-1.0)
    private boolean varySize;           // Allow size variation
    private String distributionType;    // "uniform", "clustered", "sparse"

    public CraterConfig() {
        // Default values
        this.frequency = 0.01;
        this.minSize = 10;
        this.maxSize = 50;
        this.depth = 1.0;
        this.rimHeight = 0.1;
        this.rimWidth = 0.15;
        this.varySize = true;
        this.distributionType = "uniform";
    }

    // Builder pattern methods
    public CraterConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public CraterConfig minSize(double minSize) {
        this.minSize = minSize;
        return this;
    }

    public CraterConfig maxSize(double maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public CraterConfig depth(double depth) {
        this.depth = depth;
        return this;
    }

    public CraterConfig rimHeight(double rimHeight) {
        this.rimHeight = rimHeight;
        return this;
    }

    public CraterConfig rimWidth(double rimWidth) {
        this.rimWidth = rimWidth;
        return this;
    }

    public CraterConfig varySize(boolean varySize) {
        this.varySize = varySize;
        return this;
    }

    public CraterConfig distributionType(String distributionType) {
        this.distributionType = distributionType;
        return this;
    }

    // Presets
    public static CraterConfig realistic() {
        return new CraterConfig()
            .frequency(0.02)
            .minSize(10)
            .maxSize(100)
            .depth(1.0)
            .rimHeight(0.15)
            .rimWidth(0.2)
            .varySize(true)
            .distributionType("clustered");
    }

    public static CraterConfig dramatic() {
        return new CraterConfig()
            .frequency(0.05)
            .minSize(20)
            .maxSize(200)
            .depth(2.0)
            .rimHeight(0.3)
            .rimWidth(0.25)
            .varySize(true)
            .distributionType("uniform");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getMinSize() { return minSize; }
    public double getMaxSize() { return maxSize; }
    public double getDepth() { return depth; }
    public double getRimHeight() { return rimHeight; }
    public double getRimWidth() { return rimWidth; }
    public boolean isVarySize() { return varySize; }
    public String getDistributionType() { return distributionType; }
}
