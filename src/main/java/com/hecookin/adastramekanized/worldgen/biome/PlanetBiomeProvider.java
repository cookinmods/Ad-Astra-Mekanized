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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TerraBlender BiomeProvider for planet-specific biome distribution.
 * Creates a custom Region for each planet that controls biome placement
 * based on climate parameters.
 */
public class PlanetBiomeProvider extends Region {

    private final String planetId;
    private final Map<String, ResourceKey<Biome>> planetBiomes;

    /**
     * Create a planet-specific biome provider.
     *
     * @param planetId The planet identifier (e.g., "moon", "mars")
     * @param weight Region weight (higher = more likely to be selected)
     */
    public PlanetBiomeProvider(String planetId, int weight) {
        super(
            ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID,
                planetId + "_region"
            ),
            RegionType.OVERWORLD,
            weight
        );
        this.planetId = planetId;
        this.planetBiomes = PlanetBiomeRegistry.getPlanetBiomes(planetId);
    }

    /**
     * Add biomes to the climate parameter space.
     * This method is called by TerraBlender to populate the biome distribution.
     *
     * @param registry The biome registry
     * @param mapper Consumer to add climate->biome mappings
     */
    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // If no biomes registered for this planet, skip
        if (planetBiomes.isEmpty()) {
            AdAstraMekanized.LOGGER.warn("No biomes registered for planet: {}", planetId);
            return;
        }

        // Get list of biome keys
        List<ResourceKey<Biome>> biomeKeys = new ArrayList<>(planetBiomes.values());

        // Default simple distribution: spread biomes across parameter space
        // This will be enhanced in later phases with proper climate parameters
        int biomeCount = biomeKeys.size();

        if (biomeCount == 1) {
            // Single biome - cover entire parameter space
            ResourceKey<Biome> biome = biomeKeys.get(0);
            addSingleBiome(mapper, biome);
        } else if (biomeCount == 2) {
            // Two biomes - split by temperature
            addTwoBiomes(mapper, biomeKeys);
        } else if (biomeCount <= 5) {
            // 3-5 biomes - distribute by temperature and humidity
            addMultipleBiomes(mapper, biomeKeys);
        } else {
            // 6+ biomes - full multi-parameter distribution
            addFullDistribution(mapper, biomeKeys);
        }

        AdAstraMekanized.LOGGER.debug("Added {} biomes for planet: {}", biomeCount, planetId);
    }

    /**
     * Add a single biome that covers the entire parameter space.
     */
    private void addSingleBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper, ResourceKey<Biome> biome) {
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-1.0f, 1.0f), // temperature: full range
                Climate.Parameter.span(-1.0f, 1.0f), // humidity: full range
                Climate.Parameter.span(-1.0f, 1.0f), // continentalness: full range
                Climate.Parameter.span(-1.0f, 1.0f), // erosion: full range
                Climate.Parameter.span(0.0f, 1.0f),  // depth: surface level
                Climate.Parameter.span(-1.0f, 1.0f), // weirdness: full range
                0.0f                                  // offset
            ),
            biome
        ));
    }

    /**
     * Add two biomes split by temperature (cold/hot).
     */
    private void addTwoBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper, List<ResourceKey<Biome>> biomes) {
        // Cold biome (temperature < 0)
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(-1.0f, 0.0f), // temperature: cold
                Climate.Parameter.span(-1.0f, 1.0f), // humidity: full range
                Climate.Parameter.span(-1.0f, 1.0f), // continentalness: full range
                Climate.Parameter.span(-1.0f, 1.0f), // erosion: full range
                Climate.Parameter.span(0.0f, 1.0f),  // depth: surface level
                Climate.Parameter.span(-1.0f, 1.0f), // weirdness: full range
                0.0f
            ),
            biomes.get(0)
        ));

        // Hot biome (temperature >= 0)
        mapper.accept(Pair.of(
            Climate.parameters(
                Climate.Parameter.span(0.0f, 1.0f),  // temperature: hot
                Climate.Parameter.span(-1.0f, 1.0f), // humidity: full range
                Climate.Parameter.span(-1.0f, 1.0f), // continentalness: full range
                Climate.Parameter.span(-1.0f, 1.0f), // erosion: full range
                Climate.Parameter.span(0.0f, 1.0f),  // depth: surface level
                Climate.Parameter.span(-1.0f, 1.0f), // weirdness: full range
                0.0f
            ),
            biomes.get(1)
        ));
    }

    /**
     * Add 3-5 biomes distributed by temperature and humidity.
     */
    private void addMultipleBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper, List<ResourceKey<Biome>> biomes) {
        int biomeCount = biomes.size();
        float step = 2.0f / biomeCount;

        for (int i = 0; i < biomeCount; i++) {
            float tempMin = -1.0f + (i * step);
            float tempMax = -1.0f + ((i + 1) * step);

            mapper.accept(Pair.of(
                Climate.parameters(
                    Climate.Parameter.span(tempMin, tempMax),
                    Climate.Parameter.span(-1.0f, 1.0f), // humidity: full range
                    Climate.Parameter.span(-1.0f, 1.0f), // continentalness: full range
                    Climate.Parameter.span(-1.0f, 1.0f), // erosion: full range
                    Climate.Parameter.span(0.0f, 1.0f),  // depth: surface level
                    Climate.Parameter.span(-1.0f, 1.0f), // weirdness: full range
                    0.0f
                ),
                biomes.get(i)
            ));
        }
    }

    /**
     * Add 6+ biomes with full multi-parameter distribution.
     * Creates a grid distribution across temperature and humidity.
     */
    private void addFullDistribution(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper, List<ResourceKey<Biome>> biomes) {
        int biomeCount = biomes.size();
        int gridSize = (int) Math.ceil(Math.sqrt(biomeCount));

        int biomeIndex = 0;
        for (int tempIdx = 0; tempIdx < gridSize && biomeIndex < biomeCount; tempIdx++) {
            for (int humIdx = 0; humIdx < gridSize && biomeIndex < biomeCount; humIdx++) {
                float tempMin = -1.0f + (tempIdx * 2.0f / gridSize);
                float tempMax = -1.0f + ((tempIdx + 1) * 2.0f / gridSize);
                float humMin = -1.0f + (humIdx * 2.0f / gridSize);
                float humMax = -1.0f + ((humIdx + 1) * 2.0f / gridSize);

                mapper.accept(Pair.of(
                    Climate.parameters(
                        Climate.Parameter.span(tempMin, tempMax),
                        Climate.Parameter.span(humMin, humMax),
                        Climate.Parameter.span(-1.0f, 1.0f), // continentalness: full range
                        Climate.Parameter.span(-1.0f, 1.0f), // erosion: full range
                        Climate.Parameter.span(0.0f, 1.0f),  // depth: surface level
                        Climate.Parameter.span(-1.0f, 1.0f), // weirdness: full range
                        0.0f
                    ),
                    biomes.get(biomeIndex)
                ));

                biomeIndex++;
            }
        }
    }

    /**
     * Get the planet ID for this provider.
     *
     * @return The planet identifier
     */
    public String getPlanetId() {
        return planetId;
    }

    /**
     * Create and register a biome provider for a specific planet.
     *
     * @param planetId The planet identifier
     * @param weight Region weight (higher = more likely)
     * @return The created PlanetBiomeProvider
     */
    public static PlanetBiomeProvider createForPlanet(String planetId, int weight) {
        return new PlanetBiomeProvider(planetId, weight);
    }
}
