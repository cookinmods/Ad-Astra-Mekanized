package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for dune fields on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class DuneConfig {
    private double frequency;           // Dune field density
    private double height;              // Average dune height
    private double wavelength;          // Distance between dune crests
    private String orientation;         // "north_south", "east_west", "variable"
    private String type;                // "barchan", "linear", "star"

    public DuneConfig() {
        // Default values
        this.frequency = 0.05;
        this.height = 10;
        this.wavelength = 50;
        this.orientation = "variable";
        this.type = "barchan";
    }

    // Builder pattern methods
    public DuneConfig frequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public DuneConfig height(double height) {
        this.height = height;
        return this;
    }

    public DuneConfig wavelength(double wavelength) {
        this.wavelength = wavelength;
        return this;
    }

    public DuneConfig orientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    public DuneConfig type(String type) {
        this.type = type;
        return this;
    }

    // Preset: Martian dune fields
    public static DuneConfig martian() {
        return new DuneConfig()
            .frequency(0.1)
            .height(15)
            .wavelength(80)
            .orientation("variable")
            .type("barchan");
    }

    // Getters
    public double getFrequency() { return frequency; }
    public double getHeight() { return height; }
    public double getWavelength() { return wavelength; }
    public String getOrientation() { return orientation; }
    public String getType() { return type; }
}
