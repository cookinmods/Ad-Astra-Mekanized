package com.hecookin.adastramekanized.worldgen.builder;

import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;

import static com.hecookin.adastramekanized.worldgen.builder.DensityFunctionBuilder.*;

/**
 * Builder for complete noise router configurations.
 * Based on Tectonic's noise router patterns with all 15 required components.
 * Orchestrates terrain generation, caves, aquifers, and ore veins.
 */
public class NoiseRouterBuilder {
    private final String planetId;

    // Required noise router components
    private DensityFunctionBuilder barrier;
    private DensityFunctionBuilder continents;
    private DensityFunctionBuilder depth;
    private DensityFunctionBuilder erosion;
    private DensityFunctionBuilder finalDensity;
    private DensityFunctionBuilder fluidLevelFloodedness;
    private DensityFunctionBuilder fluidLevelSpread;
    private DensityFunctionBuilder initialDensityWithoutJaggedness;
    private DensityFunctionBuilder lava;
    private DensityFunctionBuilder ridges;
    private DensityFunctionBuilder temperature;
    private DensityFunctionBuilder vegetation;
    private DensityFunctionBuilder veinGap;
    private DensityFunctionBuilder veinRidged;
    private DensityFunctionBuilder veinToggle;

    public NoiseRouterBuilder(String planetId) {
        this.planetId = planetId;
        initializeDefaults();
    }

    /**
     * Initialize with vanilla defaults for components we don't customize.
     */
    private void initializeDefaults() {
        // Use vanilla defaults for complex systems
        barrier = reference("minecraft:overworld/noise_router/barrier");
        fluidLevelFloodedness = reference("minecraft:overworld/noise_router/fluid_level_floodedness");
        fluidLevelSpread = reference("minecraft:overworld/noise_router/fluid_level_spread");
        lava = reference("minecraft:overworld/noise_router/lava");
        veinToggle = reference("minecraft:overworld/noise_router/vein_toggle");
        veinRidged = reference("minecraft:overworld/noise_router/vein_ridged");
        veinGap = reference("minecraft:overworld/noise_router/vein_gap");

        // Basic temperature and vegetation (can be customized per planet)
        temperature = noise("adastramekanized:" + planetId + "_temperature", 0.25, 0.0).flatCache();
        vegetation = noise("adastramekanized:" + planetId + "_vegetation", 0.25, 0.0).flatCache();
    }

    /**
     * Build Tectonic-style continental system with splines.
     */
    public NoiseRouterBuilder withTectonicContinents(float scale, boolean enableIslands) {
        DensityFunctionBuilder rawContinents = noise("adastramekanized:" + planetId + "_continents", scale, 0.0);

        // Apply spline for sharp coastline transitions
        DensityFunctionBuilder continentSpline = rawContinents
            .spline(SplineBuilder.continentalTransition("adastramekanized:" + planetId + "/noise/raw_continents"))
            .cache2d();

        if (enableIslands) {
            // Add island layer
            DensityFunctionBuilder islands = noise("adastramekanized:" + planetId + "_islands", scale * 0.5, 0.0)
                .mul(0.3)
                .cache2d();

            this.continents = continentSpline.add(islands).flatCache();
        } else {
            this.continents = continentSpline.flatCache();
        }

        return this;
    }

    /**
     * Build Tectonic-style erosion system.
     */
    public NoiseRouterBuilder withTectonicErosion(float scale) {
        DensityFunctionBuilder rawErosion = noise("adastramekanized:" + planetId + "_erosion", scale, 0.0);

        this.erosion = rawErosion
            .spline(SplineBuilder.erosionFactor("adastramekanized:" + planetId + "/noise/raw_erosion"))
            .cache2d();

        return this;
    }

    /**
     * Build Tectonic-style ridge/mountain system.
     */
    public NoiseRouterBuilder withTectonicRidges(float scale, float sharpness) {
        DensityFunctionBuilder rawRidges = noise("adastramekanized:" + planetId + "_ridges", scale, 0.0);

        this.ridges = rawRidges
            .abs()  // Create ridge pattern
            .mul(-1.0)  // Invert for peaks
            .add(constant(1.0))
            .mul(sharpness)
            .spline(SplineBuilder.ridgeFactor("adastramekanized:" + planetId + "/noise/raw_ridges"))
            .cache2d();

        return this;
    }

    /**
     * Build complete terrain shaping with depth calculation.
     */
    public NoiseRouterBuilder withTerrainShaping(int minY, int maxY, int seaLevel) {
        // Y-gradient from top to bottom (matching Tectonic's values)
        // More extreme to_value ensures upper atmosphere is definitely air
        DensityFunctionBuilder yGradient = constant(0)
            .yClampedGradient(minY, maxY, 1.5, -4.0);  // Changed from -1.5 to -4.0

        // Offset based on continents, erosion, ridges
        DensityFunctionBuilder offset = buildOffsetFunction();

        // Depth = Y-gradient + terrain offset
        this.depth = yGradient.add(offset).cache();

        return this;
    }

    /**
     * Build terrain offset from continents, erosion, and ridges.
     * Uses moderate multipliers to create varied terrain without extreme floating islands.
     */
    private DensityFunctionBuilder buildOffsetFunction() {
        // Use the spline values directly with moderate scaling
        // Continents: -0.22 to 0.06, Erosion: -0.08 to 0.21, Ridges: -0.01 to 0.62

        // Continents contribution (moderate multiplier for landmasses)
        DensityFunctionBuilder continentOffset = continents != null
            ? continents.mul(1.5)  // Creates gentle height variation
            : constant(0.0);

        // Erosion modulation (smaller multiplier)
        DensityFunctionBuilder erosionMod = erosion != null
            ? erosion.mul(0.8)  // Adds erosion patterns
            : constant(0.0);

        // Ridge peaks (larger multiplier for dramatic mountains)
        DensityFunctionBuilder ridgeMod = ridges != null
            ? ridges.mul(1.2)  // Creates mountain ridges
            : constant(0.0);

        // Total offset: sum of contributions, clamped to moderate range
        // This gives roughly -0.5 to +1.2 range, which is safer
        return continentOffset
            .add(erosionMod)
            .add(ridgeMod)
            .clamp(-0.8, 1.2);
    }

    /**
     * Build Tectonic-style final density with caves and features.
     */
    public NoiseRouterBuilder withTectonicFinalDensity(
        boolean cheeseCaves,
        boolean noodleCaves,
        boolean undergroundRivers,
        boolean lavaTunnels
    ) {
        // Start with sloped cheese (base terrain)
        DensityFunctionBuilder slopedCheese = buildSlopedCheese();

        // Add cheese caves
        if (cheeseCaves) {
            DensityFunctionBuilder cheeseCave = noise("adastramekanized:" + planetId + "_cave_cheese", 1.0, 0.67)
                .add(constant(0.27))  // Tectonic additive value
                .clamp(-1.0, 1.0);

            slopedCheese = slopedCheese.min(cheeseCave);
        }

        // Add noodle caves
        if (noodleCaves) {
            DensityFunctionBuilder noodleCave = reference("minecraft:overworld/caves/noodle")
                .add(constant(-0.075));  // Tectonic additive value

            slopedCheese = slopedCheese.min(noodleCave);
        }

        // Add underground rivers
        if (undergroundRivers) {
            DensityFunctionBuilder river = buildUndergroundRiver();
            slopedCheese = slopedCheese.min(river);
        }

        // Add lava tunnels
        if (lavaTunnels) {
            DensityFunctionBuilder tunnel = buildLavaTunnels();
            slopedCheese = slopedCheese.min(tunnel);
        }

        this.finalDensity = slopedCheese.cache();
        return this;
    }

    /**
     * Build sloped cheese (base terrain density).
     */
    private DensityFunctionBuilder buildSlopedCheese() {
        // Use depth directly for proper terrain shaping
        // Positive density = solid (underground), Negative density = air (sky)
        DensityFunctionBuilder quarterDepth = depth
            .mul(0.25);  // Removed negative - was causing inverted terrain

        // Jaggedness for mountain peaks
        DensityFunctionBuilder jaggedness = buildJaggedness();

        // Base terrain = 4 * (quarter_depth) + jaggedness = depth + jaggedness
        return quarterDepth
            .mul(4.0)
            .add(jaggedness)
            .interpolated();  // Smooth interpolation between chunks
    }

    /**
     * Build jaggedness (mountain peak sharpness).
     */
    private DensityFunctionBuilder buildJaggedness() {
        if (ridges == null) {
            return constant(0.0);
        }

        // Jaggedness noise
        DensityFunctionBuilder jaggedNoise = noise("adastramekanized:" + planetId + "_jagged", 1500.0, 0.0)
            .mul(0.3);

        // Apply only in ridge areas
        return ridges
            .mul(jaggedNoise)
            .clamp(0.0, 0.4);
    }

    /**
     * Build underground river system.
     */
    private DensityFunctionBuilder buildUndergroundRiver() {
        // River noise at specific Y levels
        DensityFunctionBuilder riverNoise = noise("adastramekanized:" + planetId + "_underground_river", 0.002, 0.001)
            .abs()
            .add(constant(-0.02))  // Threshold for river
            .clamp(-1.0, 0.0)
            .mul(50.0);  // Amplify for carving

        return riverNoise;
    }

    /**
     * Build lava tunnel system.
     */
    private DensityFunctionBuilder buildLavaTunnels() {
        // Tunnel noise at low Y levels
        DensityFunctionBuilder tunnelNoise = noise("adastramekanized:" + planetId + "_lava_tunnel", 0.004, 0.002)
            .abs()
            .add(constant(-0.01))  // Threshold for tunnel
            .clamp(-1.0, 0.0)
            .mul(30.0);  // Amplify for carving

        return tunnelNoise;
    }

    /**
     * Add desert dune features (modifies final density).
     */
    public NoiseRouterBuilder withDesertDunes(float height, float wavelength) {
        // Dunes are added to the offset function
        DensityFunctionBuilder dunePattern = noise("adastramekanized:" + planetId + "_dunes", 1.0 / wavelength, 0.0)
            .mul(height)
            .spline(SplineBuilder.desertDunes("adastramekanized:" + planetId + "_vegetation"));

        // Add to terrain offset
        if (finalDensity != null) {
            finalDensity = finalDensity.add(dunePattern);
        }

        return this;
    }

    /**
     * Add jungle pillar features.
     */
    public NoiseRouterBuilder withJunglePillars(float height) {
        DensityFunctionBuilder pillarPattern = noise("adastramekanized:" + planetId + "_pillars", 0.01, 0.01)
            .spline(SplineBuilder.junglePillars("adastramekanized:" + planetId + "_vegetation", height));

        if (finalDensity != null) {
            finalDensity = finalDensity.add(pillarPattern);
        }

        return this;
    }

    /**
     * Set initial density without jaggedness (for biome placement).
     */
    public NoiseRouterBuilder withInitialDensity() {
        // Similar to final density but without jaggedness
        // Positive density = solid, Negative density = air
        DensityFunctionBuilder quarterDepth = depth.mul(0.25);  // Removed negative

        this.initialDensityWithoutJaggedness = quarterDepth
            .mul(4.0)
            .interpolated()
            .cache();

        return this;
    }

    /**
     * Build the complete noise router JSON.
     */
    public JsonObject build() {
        JsonObject router = new JsonObject();

        router.add("barrier", barrier.build());
        router.add("continents", continents != null ? continents.build() : constant(0.0).build());
        router.add("depth", depth != null ? depth.build() : constant(0.0).build());
        router.add("erosion", erosion != null ? erosion.build() : constant(0.0).build());
        router.add("final_density", finalDensity != null ? finalDensity.build() : constant(0.0).build());
        router.add("fluid_level_floodedness", fluidLevelFloodedness.build());
        router.add("fluid_level_spread", fluidLevelSpread.build());
        router.add("initial_density_without_jaggedness",
            initialDensityWithoutJaggedness != null ? initialDensityWithoutJaggedness.build() : constant(0.0).build());
        router.add("lava", lava.build());
        router.add("ridges", ridges != null ? ridges.build() : constant(0.0).build());
        router.add("temperature", temperature.build());
        router.add("vegetation", vegetation.build());
        router.add("vein_gap", veinGap.build());
        router.add("vein_ridged", veinRidged.build());
        router.add("vein_toggle", veinToggle.build());

        return router;
    }

    /**
     * Create a complete Tectonic terrain configuration.
     */
    public static NoiseRouterBuilder createTectonicTerrain(
        String planetId,
        int minY,
        int maxY,
        int seaLevel,
        TectonicConfig config
    ) {
        NoiseRouterBuilder builder = new NoiseRouterBuilder(planetId);

        // Build continental system
        builder.withTectonicContinents(config.continentScale, config.enableIslands);

        // Build erosion system
        builder.withTectonicErosion(config.erosionScale);

        // Build ridge system
        builder.withTectonicRidges(config.ridgeScale, config.mountainSharpness);

        // Build terrain shaping
        builder.withTerrainShaping(minY, maxY, seaLevel);

        // Build initial density
        builder.withInitialDensity();

        // Build final density with features
        builder.withTectonicFinalDensity(
            config.cheeseCaves,
            config.noodleCaves,
            config.undergroundRivers,
            config.lavaTunnels
        );

        // Add optional features
        if (config.desertDunes) {
            builder.withDesertDunes(config.duneHeight, config.duneWavelength);
        }

        if (config.junglePillars) {
            builder.withJunglePillars(config.pillarHeight);
        }

        AdAstraMekanized.LOGGER.info("Built Tectonic noise router for planet: {}", planetId);

        return builder;
    }

    /**
     * Configuration class for Tectonic terrain.
     */
    public static class TectonicConfig {
        public float continentScale = 0.003f;
        public float erosionScale = 0.0025f;
        public float ridgeScale = 0.004f;
        public float mountainSharpness = 1.0f;
        public boolean enableIslands = false;
        public boolean cheeseCaves = true;
        public boolean noodleCaves = true;
        public boolean undergroundRivers = false;
        public boolean lavaTunnels = false;
        public boolean desertDunes = false;
        public float duneHeight = 10.0f;
        public float duneWavelength = 200.0f;
        public boolean junglePillars = false;
        public float pillarHeight = 30.0f;

        // Preset: Moon (cratered, airless)
        public static TectonicConfig moon() {
            TectonicConfig config = new TectonicConfig();
            config.continentScale = 0.005f;  // Smaller maria
            config.erosionScale = 0.001f;    // Minimal erosion
            config.ridgeScale = 0.01f;       // Sharp crater rims
            config.mountainSharpness = 0.9f;
            config.cheeseCaves = false;      // Minimal caves
            config.noodleCaves = false;
            config.lavaTunnels = true;       // Lunar lava tubes
            return config;
        }

        // Preset: Mars (dramatic, ancient)
        public static TectonicConfig mars() {
            TectonicConfig config = new TectonicConfig();
            config.continentScale = 0.004f;
            config.erosionScale = 0.003f;    // Ancient erosion
            config.ridgeScale = 0.008f;
            config.mountainSharpness = 1.5f; // Olympus Mons!
            config.cheeseCaves = true;
            config.noodleCaves = true;
            config.undergroundRivers = true;  // Ancient riverbeds
            config.desertDunes = true;        // Martian dunes
            config.duneHeight = 15.0f;
            return config;
        }

        // Preset: Earth-like
        public static TectonicConfig earthLike() {
            TectonicConfig config = new TectonicConfig();
            config.enableIslands = true;
            config.cheeseCaves = true;
            config.noodleCaves = true;
            config.undergroundRivers = false;
            return config;
        }
    }
}
