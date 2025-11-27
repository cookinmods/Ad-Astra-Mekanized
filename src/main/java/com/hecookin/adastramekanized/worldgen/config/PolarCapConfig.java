package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for polar ice caps on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class PolarCapConfig {
    private double northCapRadius;      // Radius of north polar cap
    private double southCapRadius;      // Radius of south polar cap
    private double thickness;           // Ice thickness
    private boolean layered;            // Show layered structure
    private String material;            // "ice", "dry_ice", "mixed"

    public PolarCapConfig() {
        // Default values
        this.northCapRadius = 300;
        this.southCapRadius = 300;
        this.thickness = 10;
        this.layered = false;
        this.material = "ice";
    }

    // Builder pattern methods
    public PolarCapConfig northCapRadius(double northCapRadius) {
        this.northCapRadius = northCapRadius;
        return this;
    }

    public PolarCapConfig southCapRadius(double southCapRadius) {
        this.southCapRadius = southCapRadius;
        return this;
    }

    public PolarCapConfig thickness(double thickness) {
        this.thickness = thickness;
        return this;
    }

    public PolarCapConfig layered(boolean layered) {
        this.layered = layered;
        return this;
    }

    public PolarCapConfig material(String material) {
        this.material = material;
        return this;
    }

    // Preset: Mars-style polar caps
    public static PolarCapConfig mars() {
        return new PolarCapConfig()
            .northCapRadius(500)
            .southCapRadius(600)
            .thickness(20)
            .layered(true)
            .material("mixed");
    }

    // Getters
    public double getNorthCapRadius() { return northCapRadius; }
    public double getSouthCapRadius() { return southCapRadius; }
    public double getThickness() { return thickness; }
    public boolean isLayered() { return layered; }
    public String getMaterial() { return material; }
}
