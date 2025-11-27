package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for scarps (cliff faces) on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class ScarpConfig {
    private double frequency;           // Scarp density
    private double height;              // Average scarp height
    private double length;              // Average scarp length
    private String orientation;         // Preferred orientation

    public ScarpConfig() {
        // Default values
        this.frequency = 0.01;
        this.height = 30;
        this.length = 300;
        this.orientation = "random";
    }

    // Builder pattern methods
    public ScarpConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public ScarpConfig height(double height) {
        this.height = height;
        return this;
    }

    public ScarpConfig length(double length) {
        this.length = length;
        return this;
    }

    public ScarpConfig orientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    // Preset: Mercury-style scarps
    public static ScarpConfig mercury() {
        return new ScarpConfig()
            .frequency(0.03)
            .height(40)
            .length(500)
            .orientation("random");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getHeight() { return height; }
    public double getLength() { return length; }
    public String getOrientation() { return orientation; }
}
