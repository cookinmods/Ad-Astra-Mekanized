package com.hecookin.adastramekanized.worldgen.biome;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * Moon-specific biome provider with realistic lunar biome distribution.
 *
 * Biome Distribution Strategy:
 * - Highlands (60%): Default biome, high continentalness
 * - Maria (30%): Low continentalness (basins), flat erosion
 * - Crater Rim (5%): High weirdness + high erosion
 * - Crater Floor (3%): Low depth + moderate weirdness
 * - Polar (2%): Extreme temperature values (poles)
 */
public class MoonBiomeProvider extends Region {

    public MoonBiomeProvider() {
        super(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_biomes"),
            RegionType.OVERWORLD,
            100  // High weight to ensure Moon biomes are used
        );
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        AdAstraMekanized.LOGGER.info("Adding Moon biomes to climate parameter space");

        // Register all 5 Moon biomes with specific climate parameters

        // 1. POLAR BIOMES (2% coverage) - Extreme temperature zones
        addPolarBiomes(mapper);

        // 2. CRATER RIM BIOMES (5% coverage) - High elevation, high weirdness
        addCraterRimBiomes(mapper);

        // 3. CRATER FLOOR BIOMES (3% coverage) - Low depth, moderate weirdness
        addCraterFloorBiomes(mapper);

        // 4. MARIA BIOMES (30% coverage) - Low continentalness (basins)
        addMariaBiomes(mapper);

        // 5. HIGHLANDS BIOMES (60% coverage) - Default/remainder
        addHighlandsBiomes(mapper);

        AdAstraMekanized.LOGGER.info("Moon biomes added to parameter space successfully");
    }

    /**
     * Polar biomes - Extreme cold at poles (temperature extremes)
     * Coverage: ~2%
     */
    private void addPolarBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // North pole (very cold temperature)
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-1.0f, -0.75f),  // Very cold temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(-1.0f, 1.0f),    // Any continentalness
                Climate.Parameter.span(-1.0f, 1.0f),    // Any erosion
                Climate.Parameter.span(0.0f, 1.0f),     // Surface level
                Climate.Parameter.span(-1.0f, 1.0f),    // Any weirdness
                0.0f
            ),
            MoonBiomes.POLAR
        ));

        // South pole (very cold temperature)
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(0.75f, 1.0f),    // Very hot (inverted for south pole)
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(-1.0f, 1.0f),    // Any continentalness
                Climate.Parameter.span(-1.0f, 1.0f),    // Any erosion
                Climate.Parameter.span(0.0f, 1.0f),     // Surface level
                Climate.Parameter.span(-1.0f, 1.0f),    // Any weirdness
                0.0f
            ),
            MoonBiomes.POLAR
        ));
    }

    /**
     * Crater rim biomes - High elevation and high weirdness
     * Coverage: ~5%
     */
    private void addCraterRimBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-0.75f, 0.75f),  // Normal temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(0.2f, 1.0f),     // High continentalness (elevated)
                Climate.Parameter.span(0.5f, 1.0f),     // High erosion (exposed)
                Climate.Parameter.span(0.0f, 1.0f),     // Surface level
                Climate.Parameter.span(0.6f, 1.0f),     // High weirdness (unusual terrain)
                0.0f
            ),
            MoonBiomes.CRATER_RIM
        ));
    }

    /**
     * Crater floor biomes - Low elevation with moderate weirdness
     * Coverage: ~3%
     */
    private void addCraterFloorBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-0.75f, 0.75f),  // Normal temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(-1.0f, 0.0f),    // Low continentalness (depressions)
                Climate.Parameter.span(-1.0f, 0.0f),    // Low erosion (filled)
                Climate.Parameter.span(0.0f, 0.5f),     // Below surface (crater depression)
                Climate.Parameter.span(0.3f, 0.7f),     // Moderate weirdness
                0.0f
            ),
            MoonBiomes.CRATER_FLOOR
        ));
    }

    /**
     * Maria biomes - Flat, low-lying basins
     * Coverage: ~30%
     */
    private void addMariaBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-0.75f, 0.75f),  // Normal temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(-0.8f, -0.2f),   // Low continentalness (basins)
                Climate.Parameter.span(-0.5f, 0.3f),    // Low to moderate erosion (flat)
                Climate.Parameter.span(0.0f, 0.8f),     // Near surface level
                Climate.Parameter.span(-0.6f, 0.3f),    // Low to moderate weirdness (flat)
                0.0f
            ),
            MoonBiomes.MARIA
        ));
    }

    /**
     * Highlands biomes - Default lunar surface
     * Coverage: ~60% (covers remaining parameter space)
     */
    private void addHighlandsBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Main highlands - normal continentalness and erosion
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-0.75f, 0.75f),  // Normal temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(-0.2f, 0.6f),    // Moderate to high continentalness
                Climate.Parameter.span(-0.5f, 0.5f),    // Moderate erosion
                Climate.Parameter.span(0.0f, 1.0f),     // Surface level
                Climate.Parameter.span(-0.6f, 0.6f),    // Normal weirdness
                0.0f
            ),
            MoonBiomes.HIGHLANDS
        ));

        // Additional highlands coverage (fallback for uncovered space)
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-0.75f, 0.75f),  // Normal temperature
                Climate.Parameter.span(-1.0f, 1.0f),    // Any humidity
                Climate.Parameter.span(0.0f, 1.0f),     // Any continentalness
                Climate.Parameter.span(-1.0f, 1.0f),    // Any erosion
                Climate.Parameter.span(0.5f, 1.0f),     // Above surface
                Climate.Parameter.span(-1.0f, -0.6f),   // Low weirdness (normal terrain)
                0.0f
            ),
            MoonBiomes.HIGHLANDS
        ));
    }
}
