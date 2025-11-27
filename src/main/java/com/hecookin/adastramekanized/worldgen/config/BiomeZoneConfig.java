package com.hecookin.adastramekanized.worldgen.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for biome zone distribution on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class BiomeZoneConfig {
    private Map<String, ZoneDefinition> zones;

    public BiomeZoneConfig() {
        this.zones = new HashMap<>();
    }

    /**
     * Define a zone based on altitude
     */
    public BiomeZoneConfig altitudeZone(String biomeName, int minY, int maxY) {
        zones.put(biomeName, new ZoneDefinition()
            .type("altitude")
            .minY(minY)
            .maxY(maxY));
        return this;
    }

    /**
     * Define a zone based on noise value
     */
    public BiomeZoneConfig noiseZone(String biomeName, String noiseKey,
                                     double minValue, double maxValue) {
        zones.put(biomeName, new ZoneDefinition()
            .type("noise")
            .noiseKey(noiseKey)
            .minValue(minValue)
            .maxValue(maxValue));
        return this;
    }

    /**
     * Define a zone based on feature (e.g., inside craters)
     */
    public BiomeZoneConfig featureZone(String biomeName, String featureType) {
        zones.put(biomeName, new ZoneDefinition()
            .type("feature")
            .featureType(featureType));
        return this;
    }

    // Getter
    public Map<String, ZoneDefinition> getZones() {
        return zones;
    }

    /**
     * Zone definition class for biome distribution
     */
    public static class ZoneDefinition {
        private String type;
        private int minY, maxY;
        private String noiseKey;
        private double minValue, maxValue;
        private String featureType;

        public ZoneDefinition() {
            // Default values
            this.type = "noise";
            this.minY = -64;
            this.maxY = 320;
            this.noiseKey = "continents";
            this.minValue = -1.0;
            this.maxValue = 1.0;
            this.featureType = "none";
        }

        // Builder methods
        public ZoneDefinition type(String type) {
            this.type = type;
            return this;
        }

        public ZoneDefinition minY(int minY) {
            this.minY = minY;
            return this;
        }

        public ZoneDefinition maxY(int maxY) {
            this.maxY = maxY;
            return this;
        }

        public ZoneDefinition noiseKey(String noiseKey) {
            this.noiseKey = noiseKey;
            return this;
        }

        public ZoneDefinition minValue(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public ZoneDefinition maxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public ZoneDefinition featureType(String featureType) {
            this.featureType = featureType;
            return this;
        }

        // Getters
        public String getType() { return type; }
        public int getMinY() { return minY; }
        public int getMaxY() { return maxY; }
        public String getNoiseKey() { return noiseKey; }
        public double getMinValue() { return minValue; }
        public double getMaxValue() { return maxValue; }
        public String getFeatureType() { return featureType; }
    }
}
