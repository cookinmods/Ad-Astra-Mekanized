package com.hecookin.adastramekanized.worldgen.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for individual biomes on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class BiomeConfig {
    private String name;                // Biome identifier
    private float temperature;          // Temperature (0.0 to 2.0, affects gameplay)
    private float humidity;             // Humidity/downfall (0.0 to 1.0)
    private int skyColor;               // Sky color (RGB hex)
    private int fogColor;               // Fog color (RGB hex)
    private int waterColor;             // Water color (RGB hex, if applicable)
    private int grassColor;             // Grass color override (-1 for default)
    private int foliageColor;           // Foliage color override (-1 for default)
    private boolean hasPrecipitation;   // Whether biome has rain/snow
    private String surfaceBlock;        // Primary surface block
    private String subsurfaceBlock;     // Block beneath surface
    private String undergroundBlock;    // Deep underground block
    private List<String> features;      // Configured features to add

    public BiomeConfig(String name) {
        this.name = name;
        this.temperature = 0.5f;        // Default moderate temperature
        this.humidity = 0.5f;           // Default moderate humidity
        this.skyColor = 0x78A7FF;       // Default sky blue
        this.fogColor = 0xC0D8FF;       // Default fog blue
        this.waterColor = 0x3F76E4;     // Default water blue
        this.grassColor = -1;           // No override by default
        this.foliageColor = -1;         // No override by default
        this.hasPrecipitation = false;  // No precipitation by default (space)
        this.surfaceBlock = "minecraft:stone";
        this.subsurfaceBlock = "minecraft:stone";
        this.undergroundBlock = "minecraft:stone";
        this.features = new ArrayList<>();
    }

    // Builder pattern methods
    public BiomeConfig temperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    public BiomeConfig humidity(float humidity) {
        this.humidity = humidity;
        return this;
    }

    public BiomeConfig skyColor(int skyColor) {
        this.skyColor = skyColor;
        return this;
    }

    public BiomeConfig fogColor(int fogColor) {
        this.fogColor = fogColor;
        return this;
    }

    public BiomeConfig waterColor(int waterColor) {
        this.waterColor = waterColor;
        return this;
    }

    public BiomeConfig grassColor(int grassColor) {
        this.grassColor = grassColor;
        return this;
    }

    public BiomeConfig foliageColor(int foliageColor) {
        this.foliageColor = foliageColor;
        return this;
    }

    public BiomeConfig hasPrecipitation(boolean hasPrecipitation) {
        this.hasPrecipitation = hasPrecipitation;
        return this;
    }

    public BiomeConfig surfaceBlock(String surfaceBlock) {
        this.surfaceBlock = surfaceBlock;
        return this;
    }

    public BiomeConfig subsurfaceBlock(String subsurfaceBlock) {
        this.subsurfaceBlock = subsurfaceBlock;
        return this;
    }

    public BiomeConfig undergroundBlock(String undergroundBlock) {
        this.undergroundBlock = undergroundBlock;
        return this;
    }

    public BiomeConfig addFeature(String feature) {
        this.features.add(feature);
        return this;
    }

    // Getters
    public String getName() { return name; }
    public float getTemperature() { return temperature; }
    public float getHumidity() { return humidity; }
    public int getSkyColor() { return skyColor; }
    public int getFogColor() { return fogColor; }
    public int getWaterColor() { return waterColor; }
    public int getGrassColor() { return grassColor; }
    public int getFoliageColor() { return foliageColor; }
    public boolean hasPrecipitation() { return hasPrecipitation; }
    public String getSurfaceBlock() { return surfaceBlock; }
    public String getSubsurfaceBlock() { return subsurfaceBlock; }
    public String getUndergroundBlock() { return undergroundBlock; }
    public List<String> getFeatures() { return features; }
}
