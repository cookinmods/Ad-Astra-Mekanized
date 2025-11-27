package com.hecookin.adastramekanized.worldgen.biome;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.config.BiomeConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central biome registration system for planet-specific biomes.
 * Handles registration with NeoForge and TerraBlender integration.
 */
public class PlanetBiomeRegistry {

    private static final DeferredRegister<Biome> BIOMES =
        DeferredRegister.create(Registries.BIOME, AdAstraMekanized.MOD_ID);

    // Registry of planet ID -> biome names -> ResourceKeys
    private static final Map<String, Map<String, ResourceKey<Biome>>> PLANET_BIOMES = new HashMap<>();

    // Registry of biome ResourceKeys -> BiomeConfig definitions
    private static final Map<ResourceKey<Biome>, BiomeConfig> BIOME_CONFIGS = new HashMap<>();

    /**
     * Register a planet-specific biome with configuration.
     *
     * @param planetId The planet identifier (e.g., "moon", "mars")
     * @param biomeName The biome name within the planet (e.g., "highlands", "maria")
     * @param config The biome configuration
     * @return ResourceKey for the registered biome
     */
    public static ResourceKey<Biome> registerPlanetBiome(String planetId, String biomeName, BiomeConfig config) {
        // Create resource key with format: adastramekanized:planet_biome (e.g., moon_highlands)
        String biomeId = planetId + "_" + biomeName;
        ResourceKey<Biome> biomeKey = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, biomeId)
        );

        // Register with DeferredRegister
        BIOMES.register(biomeId, () -> createBiomeFromConfig(config));

        // Store in planet registry
        PLANET_BIOMES.computeIfAbsent(planetId, k -> new HashMap<>()).put(biomeName, biomeKey);

        // Store configuration
        BIOME_CONFIGS.put(biomeKey, config);

        AdAstraMekanized.LOGGER.info("Registered planet biome: {} for planet: {}", biomeId, planetId);

        return biomeKey;
    }

    /**
     * Get all biomes registered for a specific planet.
     *
     * @param planetId The planet identifier
     * @return Map of biome name -> ResourceKey
     */
    public static Map<String, ResourceKey<Biome>> getPlanetBiomes(String planetId) {
        return PLANET_BIOMES.getOrDefault(planetId, new HashMap<>());
    }

    /**
     * Get a specific biome ResourceKey for a planet.
     *
     * @param planetId The planet identifier
     * @param biomeName The biome name within the planet
     * @return Optional containing the ResourceKey if found
     */
    public static Optional<ResourceKey<Biome>> getBiome(String planetId, String biomeName) {
        Map<String, ResourceKey<Biome>> planetBiomes = PLANET_BIOMES.get(planetId);
        if (planetBiomes != null) {
            return Optional.ofNullable(planetBiomes.get(biomeName));
        }
        return Optional.empty();
    }

    /**
     * Get the BiomeConfig for a specific ResourceKey.
     *
     * @param biomeKey The biome ResourceKey
     * @return Optional containing the BiomeConfig if found
     */
    public static Optional<BiomeConfig> getBiomeConfig(ResourceKey<Biome> biomeKey) {
        return Optional.ofNullable(BIOME_CONFIGS.get(biomeKey));
    }

    /**
     * Create a Biome instance from a BiomeConfig.
     * Note: BiomeGenerationSettings are handled separately via JSON datapack files.
     *
     * @param config The biome configuration
     * @return Constructed Biome instance
     */
    private static Biome createBiomeFromConfig(BiomeConfig config) {
        // Create special effects (sky color, fog color, water color, etc.)
        BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder()
            .skyColor(config.getSkyColor())
            .fogColor(config.getFogColor())
            .waterColor(config.getWaterColor())
            .waterFogColor(config.getWaterColor()); // Use same as water color for consistency

        // Apply grass color if set
        if (config.getGrassColor() != -1) {
            effectsBuilder.grassColorOverride(config.getGrassColor());
        }

        // Apply foliage color if set
        if (config.getFoliageColor() != -1) {
            effectsBuilder.foliageColorOverride(config.getFoliageColor());
        }

        BiomeSpecialEffects effects = effectsBuilder.build();

        // Create mob spawn settings (empty for now, can be customized per planet)
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        MobSpawnSettings spawnSettings = spawnBuilder.build();

        // Create empty generation settings
        // In Minecraft 1.21.1+, generation settings require HolderGetter parameters
        // which are only available during worldgen context or data generation.
        // For programmatic biome registration, we use PlainBuilder with empty settings.
        BiomeGenerationSettings generationSettings = BiomeGenerationSettings.EMPTY;

        // Build and return the biome
        return new Biome.BiomeBuilder()
            .hasPrecipitation(config.hasPrecipitation())
            .temperature(config.getTemperature())
            .downfall(config.getHumidity())
            .specialEffects(effects)
            .mobSpawnSettings(spawnSettings)
            .generationSettings(generationSettings)
            .build();
    }

    /**
     * Register the biome deferred register with the mod event bus.
     * Call this from the mod constructor.
     *
     * @param modEventBus The mod event bus
     */
    public static void register(IEventBus modEventBus) {
        BIOMES.register(modEventBus);
        AdAstraMekanized.LOGGER.info("Planet biome registry initialized");
    }

    /**
     * Get the total number of registered planet biomes.
     *
     * @return Total biome count
     */
    public static int getTotalBiomeCount() {
        return PLANET_BIOMES.values().stream()
            .mapToInt(Map::size)
            .sum();
    }

    /**
     * Check if a planet has any registered biomes.
     *
     * @param planetId The planet identifier
     * @return true if the planet has biomes registered
     */
    public static boolean hasPlanetBiomes(String planetId) {
        Map<String, ResourceKey<Biome>> biomes = PLANET_BIOMES.get(planetId);
        return biomes != null && !biomes.isEmpty();
    }
}
