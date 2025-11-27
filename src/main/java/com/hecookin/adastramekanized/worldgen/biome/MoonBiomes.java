package com.hecookin.adastramekanized.worldgen.biome;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.config.BiomeConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Moon biome definitions and registration.
 *
 * Moon biomes:
 * - highlands: Bright, heavily cratered uplands (60% coverage)
 * - maria: Dark, flat volcanic plains (30% coverage)
 * - crater_rim: Elevated crater edges (5% coverage)
 * - crater_floor: Deep crater interiors (3% coverage)
 * - polar: Ice-bearing polar regions (2% coverage)
 */
public class MoonBiomes {

    // Biome ResourceKeys
    public static ResourceKey<Biome> HIGHLANDS;
    public static ResourceKey<Biome> MARIA;
    public static ResourceKey<Biome> CRATER_RIM;
    public static ResourceKey<Biome> CRATER_FLOOR;
    public static ResourceKey<Biome> POLAR;

    /**
     * Register all Moon biomes with the biome registry.
     * Call this during mod initialization.
     */
    public static void register() {
        HIGHLANDS = PlanetBiomeRegistry.registerPlanetBiome(
            "moon",
            "highlands",
            createHighlandsBiome()
        );

        MARIA = PlanetBiomeRegistry.registerPlanetBiome(
            "moon",
            "maria",
            createMariaBiome()
        );

        CRATER_RIM = PlanetBiomeRegistry.registerPlanetBiome(
            "moon",
            "crater_rim",
            createCraterRimBiome()
        );

        CRATER_FLOOR = PlanetBiomeRegistry.registerPlanetBiome(
            "moon",
            "crater_floor",
            createCraterFloorBiome()
        );

        POLAR = PlanetBiomeRegistry.registerPlanetBiome(
            "moon",
            "polar",
            createPolarBiome()
        );

        AdAstraMekanized.LOGGER.info("Registered {} Moon biomes", 5);
    }

    /**
     * Moon Highlands - Bright, heavily cratered uplands.
     * Temperature: Very cold (0.0)
     * Coverage: 60% of Moon surface
     */
    private static BiomeConfig createHighlandsBiome() {
        return new BiomeConfig("highlands")
            .temperature(0.0f)              // Very cold
            .humidity(0.0f)                 // No atmosphere
            .skyColor(0x000000)             // Black space sky
            .fogColor(0x000000)             // No atmosphere fog
            .waterColor(0x3F76E4)           // Not used (no water)
            .grassColor(0xC8C8C8)           // Light gray (lunar regolith)
            .foliageColor(0xC8C8C8)         // Light gray
            .hasPrecipitation(false)        // No atmosphere
            .surfaceBlock("adastramekanized:moon_stone")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .undergroundBlock("adastramekanized:moon_stone");
    }

    /**
     * Moon Maria - Dark, flat volcanic plains.
     * Temperature: Very cold (0.0)
     * Coverage: 30% of Moon surface
     */
    private static BiomeConfig createMariaBiome() {
        return new BiomeConfig("maria")
            .temperature(0.0f)              // Very cold
            .humidity(0.0f)                 // No atmosphere
            .skyColor(0x000000)             // Black space sky
            .fogColor(0x000000)             // No atmosphere fog
            .waterColor(0x3F76E4)           // Not used
            .grassColor(0x3C3C3C)           // Dark gray (basaltic plains)
            .foliageColor(0x3C3C3C)         // Dark gray
            .hasPrecipitation(false)        // No atmosphere
            .surfaceBlock("adastramekanized:moon_deepslate")
            .subsurfaceBlock("adastramekanized:moon_deepslate")
            .undergroundBlock("adastramekanized:moon_stone");
    }

    /**
     * Moon Crater Rim - Elevated crater edges.
     * Temperature: Very cold (0.0)
     * Coverage: 5% of Moon surface
     */
    private static BiomeConfig createCraterRimBiome() {
        return new BiomeConfig("crater_rim")
            .temperature(0.0f)              // Very cold
            .humidity(0.0f)                 // No atmosphere
            .skyColor(0x000000)             // Black space sky
            .fogColor(0x000000)             // No atmosphere fog
            .waterColor(0x3F76E4)           // Not used
            .grassColor(0xE8E8E8)           // Bright gray (exposed bedrock)
            .foliageColor(0xE8E8E8)         // Bright gray
            .hasPrecipitation(false)        // No atmosphere
            .surfaceBlock("adastramekanized:moon_stone")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .undergroundBlock("adastramekanized:moon_stone");
    }

    /**
     * Moon Crater Floor - Deep crater interiors.
     * Temperature: Very cold (0.0)
     * Coverage: 3% of Moon surface
     */
    private static BiomeConfig createCraterFloorBiome() {
        return new BiomeConfig("crater_floor")
            .temperature(0.0f)              // Very cold
            .humidity(0.0f)                 // No atmosphere
            .skyColor(0x000000)             // Black space sky
            .fogColor(0x000000)             // No atmosphere fog
            .waterColor(0x3F76E4)           // Not used
            .grassColor(0x646464)           // Medium gray (deep regolith)
            .foliageColor(0x646464)         // Medium gray
            .hasPrecipitation(false)        // No atmosphere
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_sand")
            .undergroundBlock("adastramekanized:moon_stone");
    }

    /**
     * Moon Polar - Ice-bearing polar regions.
     * Temperature: Extremely cold (-0.5)
     * Coverage: 2% of Moon surface (polar caps)
     */
    private static BiomeConfig createPolarBiome() {
        return new BiomeConfig("polar")
            .temperature(-0.5f)             // Extremely cold
            .humidity(0.0f)                 // No atmosphere
            .skyColor(0x000000)             // Black space sky
            .fogColor(0x000000)             // No atmosphere fog
            .waterColor(0x3F76E4)           // Not used
            .grassColor(0xF0F0F0)           // Nearly white (ice deposits)
            .foliageColor(0xF0F0F0)         // Nearly white
            .hasPrecipitation(false)        // No atmosphere
            .surfaceBlock("minecraft:ice")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .undergroundBlock("adastramekanized:moon_stone");
    }

    /**
     * Get the total number of Moon biomes.
     */
    public static int getBiomeCount() {
        return 5;
    }
}
