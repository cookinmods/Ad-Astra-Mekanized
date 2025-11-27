package com.hecookin.adastramekanized.worldgen.biome;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.worldgen.config.BiomeConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Optional;

/**
 * Handles dynamic biome modifications for planet-specific biomes.
 * This includes adding features, spawn settings, and other runtime modifications.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BiomeModificationHandler {

    /**
     * Apply planet-specific biome modifications.
     * Called when biomes are loaded to apply custom features and settings.
     *
     * @param event The biome loading event
     */
    @SubscribeEvent
    public static void onBiomeLoading(LevelEvent.Load event) {
        // This event is called when a level loads
        // Biome modifications are now handled through BiomeModifier system in NeoForge
        AdAstraMekanized.LOGGER.debug("Level loaded, biome modifications active");
    }

    /**
     * Add configured features to a planet biome.
     *
     * @param biomeKey The biome to modify
     * @param config The biome configuration containing features to add
     */
    public static void addBiomeFeatures(ResourceKey<Biome> biomeKey, BiomeConfig config) {
        // Feature addition is handled through BiomeModifier JSON files in NeoForge
        // This method serves as a future hook for programmatic feature addition

        if (config.getFeatures().isEmpty()) {
            return;
        }

        AdAstraMekanized.LOGGER.debug(
            "Biome {} has {} configured features (handled via BiomeModifier JSON)",
            biomeKey.location(),
            config.getFeatures().size()
        );
    }

    /**
     * Apply surface rules to a planet biome based on configuration.
     *
     * @param biomeKey The biome to modify
     * @param config The biome configuration with surface block settings
     */
    public static void applySurfaceRules(ResourceKey<Biome> biomeKey, BiomeConfig config) {
        // Surface rules are handled through SurfaceRuleData in NeoForge
        // This method serves as a future hook for programmatic surface rule application

        String surfaceBlock = config.getSurfaceBlock();
        String subsurfaceBlock = config.getSubsurfaceBlock();
        String undergroundBlock = config.getUndergroundBlock();

        AdAstraMekanized.LOGGER.debug(
            "Biome {} surface configuration - Surface: {}, Subsurface: {}, Underground: {}",
            biomeKey.location(),
            surfaceBlock,
            subsurfaceBlock,
            undergroundBlock
        );
    }

    /**
     * Register all planet biome modifications.
     * Call this during mod initialization after biomes are registered.
     */
    public static void registerBiomeModifications() {
        int totalBiomes = PlanetBiomeRegistry.getTotalBiomeCount();

        if (totalBiomes == 0) {
            AdAstraMekanized.LOGGER.warn("No planet biomes registered for modification");
            return;
        }

        AdAstraMekanized.LOGGER.info(
            "Registered biome modifications for {} planet biomes",
            totalBiomes
        );

        // Apply modifications to all registered biomes
        PlanetBiomeRegistry.getPlanetBiomes("moon").forEach((name, biomeKey) -> {
            Optional<BiomeConfig> configOpt = PlanetBiomeRegistry.getBiomeConfig(biomeKey);
            configOpt.ifPresent(config -> {
                addBiomeFeatures(biomeKey, config);
                applySurfaceRules(biomeKey, config);
            });
        });

        PlanetBiomeRegistry.getPlanetBiomes("mars").forEach((name, biomeKey) -> {
            Optional<BiomeConfig> configOpt = PlanetBiomeRegistry.getBiomeConfig(biomeKey);
            configOpt.ifPresent(config -> {
                addBiomeFeatures(biomeKey, config);
                applySurfaceRules(biomeKey, config);
            });
        });

        // Additional planets will be added in future phases
    }

    /**
     * Add ore generation features to a biome.
     *
     * @param biomeKey The biome to add ores to
     * @param oreType The type of ore (e.g., "iron", "copper")
     * @param generationStep The generation step (usually UNDERGROUND_ORES)
     */
    public static void addOreGeneration(
        ResourceKey<Biome> biomeKey,
        String oreType,
        GenerationStep.Decoration generationStep
    ) {
        // Ore generation is handled through PlacedFeature JSON files
        // This serves as a future hook for programmatic ore addition
        AdAstraMekanized.LOGGER.debug(
            "Biome {} ore generation: {} at step {}",
            biomeKey.location(),
            oreType,
            generationStep
        );
    }

    /**
     * Add vegetation features to a biome.
     *
     * @param biomeKey The biome to add vegetation to
     * @param vegetationType The type of vegetation (e.g., "trees", "grass")
     */
    public static void addVegetation(ResourceKey<Biome> biomeKey, String vegetationType) {
        // Vegetation is handled through PlacedFeature and ConfiguredFeature JSON files
        // This serves as a future hook for programmatic vegetation addition
        AdAstraMekanized.LOGGER.debug(
            "Biome {} vegetation: {}",
            biomeKey.location(),
            vegetationType
        );
    }

    /**
     * Add structure generation to a biome.
     *
     * @param biomeKey The biome to add structures to
     * @param structureType The type of structure (e.g., "ruins", "village")
     */
    public static void addStructures(ResourceKey<Biome> biomeKey, String structureType) {
        // Structure generation is handled through structure JSON files
        // This serves as a future hook for programmatic structure addition
        AdAstraMekanized.LOGGER.debug(
            "Biome {} structure: {}",
            biomeKey.location(),
            structureType
        );
    }

    /**
     * Configure mob spawning for a biome.
     *
     * @param biomeKey The biome to configure spawning for
     * @param mobType The type of mob to spawn
     * @param weight Spawn weight (higher = more common)
     * @param minGroup Minimum group size
     * @param maxGroup Maximum group size
     */
    public static void configureMobSpawning(
        ResourceKey<Biome> biomeKey,
        String mobType,
        int weight,
        int minGroup,
        int maxGroup
    ) {
        // Mob spawning is configured in biome JSON files
        // This serves as a future hook for programmatic spawn configuration
        AdAstraMekanized.LOGGER.debug(
            "Biome {} mob spawning: {} (weight: {}, group: {}-{})",
            biomeKey.location(),
            mobType,
            weight,
            minGroup,
            maxGroup
        );
    }
}
