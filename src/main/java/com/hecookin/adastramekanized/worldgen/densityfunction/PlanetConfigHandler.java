package com.hecookin.adastramekanized.worldgen.densityfunction;

import com.hecookin.adastramekanized.AdAstraMekanized;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles configuration values for planet-specific worldgen parameters.
 * This allows runtime configuration of terrain generation without recompiling.
 * Based on Tectonic's ConfigHandler pattern.
 */
public class PlanetConfigHandler {
    private static final PlanetConfigHandler INSTANCE = new PlanetConfigHandler();

    // Planet ID -> (Config Key -> Value)
    private final Map<String, Map<String, Double>> planetConfigs = new ConcurrentHashMap<>();

    // Default values for common parameters
    private final Map<String, Double> defaultValues = new HashMap<>();

    private PlanetConfigHandler() {
        initializeDefaults();
    }

    public static PlanetConfigHandler getInstance() {
        return INSTANCE;
    }

    private void initializeDefaults() {
        // Tectonic-style terrain parameters
        defaultValues.put("continents", 1.0);
        defaultValues.put("continents_scale", 0.003);
        defaultValues.put("erosion", 1.0);
        defaultValues.put("erosion_scale", 0.0025);
        defaultValues.put("ridges", 1.0);
        defaultValues.put("ridges_scale", 0.004);

        // Mountain generation
        defaultValues.put("mountain_height", 1.0);
        defaultValues.put("mountain_sharpness", 0.5);
        defaultValues.put("jaggedness", 0.0);

        // Advanced features
        defaultValues.put("underground_rivers", 0.0);
        defaultValues.put("jungle_pillars", 0.0);
        defaultValues.put("desert_dunes", 0.0);
        defaultValues.put("lava_tunnels", 0.0);

        // Cave generation
        defaultValues.put("cheese_cave_additive", 0.27);
        defaultValues.put("noodle_cave_additive", -0.075);
        defaultValues.put("cave_depth_cutoff_start", 0.1);
        defaultValues.put("cave_depth_cutoff_size", 0.1);

        // Ocean control
        defaultValues.put("ocean_offset", -0.8);
        defaultValues.put("flat_terrain_skew", 0.0);

        // Vertical scaling
        defaultValues.put("vertical_terrain_scale", 1.0);
        defaultValues.put("snow_start_offset", 128.0);
    }

    /**
     * Configure a planet with specific values.
     */
    public void configurePlanet(String planetId, Map<String, Double> config) {
        planetConfigs.put(planetId, new HashMap<>(config));
        AdAstraMekanized.LOGGER.info("Configured planet {} with {} parameters",
            planetId, config.size());
    }

    /**
     * Set a specific configuration value for a planet.
     */
    public void setValue(String planetId, String key, double value) {
        planetConfigs.computeIfAbsent(planetId, k -> new HashMap<>())
                    .put(key, value);
    }

    /**
     * Get a configuration value for a planet, with fallback to defaults.
     */
    public double getValue(String planetId, String key) {
        // Try planet-specific config first
        Map<String, Double> planetConfig = planetConfigs.get(planetId);
        if (planetConfig != null && planetConfig.containsKey(key)) {
            return planetConfig.get(key);
        }

        // Fall back to default value
        Double defaultValue = defaultValues.get(key);
        if (defaultValue != null) {
            return defaultValue;
        }

        // If no default exists, return 0
        AdAstraMekanized.LOGGER.warn("No config value found for planet {} key {}, returning 0",
            planetId, key);
        return 0.0;
    }

    /**
     * Configure Moon with Tectonic parameters.
     */
    public void configureMoon() {
        Map<String, Double> moonConfig = new HashMap<>();

        // Lunar terrain - sharp maria boundaries, cratered highlands
        moonConfig.put("continents", 0.3);
        moonConfig.put("continents_scale", 0.005);  // Smaller scale for lunar maria
        moonConfig.put("erosion", 0.1);  // Minimal erosion on airless world
        moonConfig.put("erosion_scale", 0.001);
        moonConfig.put("ridges", 0.7);  // Crater rims
        moonConfig.put("ridges_scale", 0.01);

        // Sharp crater rims
        moonConfig.put("mountain_height", 0.8);
        moonConfig.put("mountain_sharpness", 0.9);
        moonConfig.put("jaggedness", 0.4);

        // Lunar features
        moonConfig.put("underground_rivers", 0.0);  // No rivers
        moonConfig.put("lava_tunnels", 0.3);  // Lunar lava tubes!

        // Reduced caves for airless world
        moonConfig.put("cheese_cave_additive", 0.05);
        moonConfig.put("noodle_cave_additive", -0.01);

        configurePlanet("moon", moonConfig);
    }

    /**
     * Configure Mars with Tectonic parameters.
     */
    public void configureMars() {
        Map<String, Double> marsConfig = new HashMap<>();

        // Martian terrain - dramatic features, canyons, Olympus Mons
        marsConfig.put("continents", 0.4);
        marsConfig.put("continents_scale", 0.004);
        marsConfig.put("erosion", 0.3);  // Some ancient erosion
        marsConfig.put("erosion_scale", 0.003);
        marsConfig.put("ridges", 0.6);
        marsConfig.put("ridges_scale", 0.008);

        // Olympus Mons scale mountains
        marsConfig.put("mountain_height", 2.0);  // Double height!
        marsConfig.put("mountain_sharpness", 0.7);
        marsConfig.put("jaggedness", 0.8);

        // Martian features
        marsConfig.put("underground_rivers", 0.1);  // Ancient riverbeds
        marsConfig.put("desert_dunes", 0.8);  // Martian dunes
        marsConfig.put("lava_tunnels", 0.1);  // Some ancient lava tubes

        // Moderate caves
        marsConfig.put("cheese_cave_additive", 0.15);
        marsConfig.put("noodle_cave_additive", -0.05);

        configurePlanet("mars", marsConfig);
    }

    /**
     * Initialize all planet configurations.
     * Called during mod initialization.
     */
    public void initializePlanets() {
        configureMoon();
        configureMars();
        // Add other planets as needed

        AdAstraMekanized.LOGGER.info("Initialized Tectonic configurations for {} planets",
            planetConfigs.size());
    }
}