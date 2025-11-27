package com.hecookin.adastramekanized.worldgen.config;

/**
 * Configuration for atmospheric effects on terrain.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class AtmosphereConfig {
    private double pressureMultiplier;  // Atmospheric pressure effect
    private double erosionEffect;       // How atmosphere affects terrain
    private boolean weathering;         // Enable weathering effects

    public AtmosphereConfig() {
        // Default values
        this.pressureMultiplier = 1.0;
        this.erosionEffect = 0.5;
        this.weathering = true;
    }

    // Builder pattern methods
    public AtmosphereConfig pressureMultiplier(double pressureMultiplier) {
        this.pressureMultiplier = pressureMultiplier;
        return this;
    }

    public AtmosphereConfig erosionEffect(double erosionEffect) {
        this.erosionEffect = erosionEffect;
        return this;
    }

    public AtmosphereConfig weathering(boolean weathering) {
        this.weathering = weathering;
        return this;
    }

    // Preset: Venus-style thick atmosphere
    public static AtmosphereConfig venus() {
        return new AtmosphereConfig()
            .pressureMultiplier(2.5)
            .erosionEffect(0.8)
            .weathering(true);
    }

    // Getters
    public double getPressureMultiplier() { return pressureMultiplier; }
    public double getErosionEffect() { return erosionEffect; }
    public boolean hasWeathering() { return weathering; }
}
