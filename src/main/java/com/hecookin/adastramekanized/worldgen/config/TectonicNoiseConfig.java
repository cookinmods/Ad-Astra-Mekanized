package com.hecookin.adastramekanized.worldgen.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Tectonic noise integration.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class TectonicNoiseConfig {
    private boolean useTectonicContinents;
    private boolean useTectonicErosion;
    private boolean useTectonicRidges;
    private Map<String, String> customNoiseOverrides;

    public TectonicNoiseConfig() {
        this.useTectonicContinents = false;
        this.useTectonicErosion = false;
        this.useTectonicRidges = false;
        this.customNoiseOverrides = new HashMap<>();
    }

    // Builder pattern methods
    public TectonicNoiseConfig useContinents(boolean use) {
        this.useTectonicContinents = use;
        return this;
    }

    public TectonicNoiseConfig useErosion(boolean use) {
        this.useTectonicErosion = use;
        return this;
    }

    public TectonicNoiseConfig useRidges(boolean use) {
        this.useTectonicRidges = use;
        return this;
    }

    public TectonicNoiseConfig overrideNoise(String noiseKey, String customFunction) {
        this.customNoiseOverrides.put(noiseKey, customFunction);
        return this;
    }

    // Presets
    public static TectonicNoiseConfig fullIntegration() {
        return new TectonicNoiseConfig()
            .useContinents(true)
            .useErosion(true)
            .useRidges(true);
    }

    public static TectonicNoiseConfig minimal() {
        return new TectonicNoiseConfig()
            .useContinents(false)
            .useErosion(false)
            .useRidges(false);
    }

    // Getters
    public boolean isUseTectonicContinents() { return useTectonicContinents; }
    public boolean isUseTectonicErosion() { return useTectonicErosion; }
    public boolean isUseTectonicRidges() { return useTectonicRidges; }
    public Map<String, String> getCustomNoiseOverrides() { return customNoiseOverrides; }
}
