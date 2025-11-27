package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for maria (flat plains) on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class MariaConfig {
    private double frequency;           // How much of planet is maria
    private double flatness;            // How flat (0.0-1.0, 1.0 = perfectly flat)
    private double fillLevel;           // Y-level where maria fill to
    private String material;            // "basalt", "dust", "ice"

    public MariaConfig() {
        // Default values
        this.frequency = 0.2;
        this.flatness = 0.8;
        this.fillLevel = 64;
        this.material = "basalt";
    }

    // Builder pattern methods
    public MariaConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public MariaConfig flatness(double flatness) {
        this.flatness = flatness;
        return this;
    }

    public MariaConfig fillLevel(double fillLevel) {
        this.fillLevel = fillLevel;
        return this;
    }

    public MariaConfig material(String material) {
        this.material = material;
        return this;
    }

    // Preset: Lunar maria (basalt plains)
    public static MariaConfig lunar() {
        return new MariaConfig()
            .frequency(0.3)
            .flatness(0.9)
            .fillLevel(64)
            .material("basalt");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getFlatness() { return flatness; }
    public double getFillLevel() { return fillLevel; }
    public String getMaterial() { return material; }
}
