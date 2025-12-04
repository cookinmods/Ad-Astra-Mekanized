package com.hecookin.adastramekanized.client.rendering;

import com.google.gson.JsonElement;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.rendering.planet.MoonDimensionEffects;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import java.io.BufferedReader;
import java.util.Map;

/**
 * Handles registration of custom dimension special effects for planets.
 *
 * This class registers planet-specific visual effects that control:
 * - Atmospheric rendering (fog, sky color)
 * - Celestial body appearance
 * - Weather and particle effects
 * - Star visibility and rendering
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DimensionEffectsHandler {

    /**
     * Register custom dimension effects for all planets.
     * Called during client initialization to set up planet-specific visual effects.
     */
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        AdAstraMekanized.LOGGER.info("=== DIMENSION EFFECTS REGISTRATION START ===");

        try {
            // Get all loaded planets from the planet registry
            PlanetRegistry registry = PlanetRegistry.getInstance();
            int planetCount = registry != null && registry.getAllPlanets() != null ? registry.getAllPlanets().size() : 0;
            AdAstraMekanized.LOGGER.info("PlanetRegistry status: instance={}, planets={}",
                registry != null ? "present" : "null", planetCount);

            if (registry != null && registry.getAllPlanets() != null && !registry.getAllPlanets().isEmpty()) {
                // If registry is ready, use it
                AdAstraMekanized.LOGGER.info("Using PRIMARY path: PlanetRegistry with {} planets", planetCount);
                registerFromPlanetRegistry(event, registry);
            } else {
                // Fallback: Register known planets using JSON data
                AdAstraMekanized.LOGGER.info("Using FALLBACK path: PlanetRegistry not ready");
                registerKnownPlanetEffects(event);
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register dimension effects, using fallback", e);
            registerKnownPlanetEffects(event);
        }

        AdAstraMekanized.LOGGER.info("=== DIMENSION EFFECTS REGISTRATION COMPLETE ===");
    }

    private static void registerFromPlanetRegistry(RegisterDimensionSpecialEffectsEvent event, PlanetRegistry registry) {
        int registeredCount = 0;

        // Register effects for each planet
        for (Planet planet : registry.getAllPlanets()) {
            try {
                // Create the dimension type resource location
                ResourceLocation dimensionType = planet.id();

                // Create custom effects for this planet
                PlanetDimensionEffects effects = PlanetDimensionEffects.createForPlanet(planet);

                // Register the effects
                event.register(dimensionType, effects);

                AdAstraMekanized.LOGGER.debug("Registered dimension effects for planet: {}", planet.displayName());
                registeredCount++;

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to register dimension effects for planet: {}",
                    planet.displayName(), e);
            }
        }

        AdAstraMekanized.LOGGER.info("Successfully registered dimension effects for {} planets", registeredCount);
    }

    /**
     * Fallback method to register effects for specific known planets using JSON data
     */
    public static void registerKnownPlanetEffects(RegisterDimensionSpecialEffectsEvent event) {
        AdAstraMekanized.LOGGER.info("FALLBACK Step 1: Attempting to load planet JSON data...");

        // Load planet data directly from JSON files during client startup
        try {
            net.minecraft.server.packs.resources.ResourceManager resourceManager =
                net.minecraft.client.Minecraft.getInstance().getResourceManager();

            AdAstraMekanized.LOGGER.info("FALLBACK Step 2: ResourceManager={}", resourceManager != null ? "present" : "null");

            if (resourceManager != null) {
                loadPlanetsFromResourceManager(resourceManager);

                // Now try to register from the loaded data
                PlanetRegistry registry = PlanetRegistry.getInstance();
                int loadedCount = registry != null ? registry.getAllPlanets().size() : 0;
                AdAstraMekanized.LOGGER.info("FALLBACK Step 3: After JSON load, registry has {} planets", loadedCount);

                if (registry != null && !registry.getAllPlanets().isEmpty()) {
                    AdAstraMekanized.LOGGER.info("FALLBACK: Successfully loaded JSON, using registry path");
                    registerFromPlanetRegistry(event, registry);
                    return;
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("FALLBACK: Failed to load planets from JSON", e);
        }

        // Last resort: Auto-generate from planet registry (if JSON loading fails completely)
        AdAstraMekanized.LOGGER.info("FALLBACK Step 4: Using PlanetBuilder auto-generation (last resort)...");

        // Get all planet builders from PlanetGenerationRunner
        var planetBuilders = com.hecookin.adastramekanized.common.planets.PlanetGenerationRunner.getAllPlanetBuilders();

        if (planetBuilders.isEmpty()) {
            AdAstraMekanized.LOGGER.warn("No planet builders found! Dimension effects will not be registered.");
            return;
        }

        // Auto-generate dimension effects for each planet
        for (var entry : planetBuilders.entrySet()) {
            String planetId = entry.getKey();
            var builder = entry.getValue();

            try {
                DimensionSpecialEffects effects = createEffectsFromBuilder(builder);
                registerPlanetEffect(event, planetId, effects);
                AdAstraMekanized.LOGGER.debug("Auto-registered dimension effects for: {}", planetId);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to auto-register dimension effects for {}: {}",
                    planetId, e.getMessage());
            }
        }

        AdAstraMekanized.LOGGER.info("Auto-registered dimension effects for {} planets", planetBuilders.size());
    }

    /**
     * Create dimension effects from a planet builder.
     * Used for fallback when planet JSON isn't loaded yet.
     */
    private static DimensionSpecialEffects createEffectsFromBuilder(
            com.hecookin.adastramekanized.common.planets.PlanetMaker.PlanetBuilder builder) {

        // For moon, use special MoonDimensionEffects
        if ("moon".equals(builder.getName())) {
            AdAstraMekanized.LOGGER.info("Creating MoonDimensionEffects for: {}", builder.getName());
            return new MoonDimensionEffects();
        }

        // Extract properties from builder
        boolean hasClouds = builder.hasClouds();
        boolean hasRain = builder.hasRain();
        boolean hasAtmosphere = builder.hasAtmosphere();
        boolean breathable = builder.isAtmosphereBreathable();

        AdAstraMekanized.LOGGER.info("Creating BuilderDimensionEffects for {} - clouds={}, rain={}, atmosphere={}, breathable={}",
            builder.getName(), hasClouds, hasRain, hasAtmosphere, breathable);

        // Create a dimension effects instance that uses builder values directly
        // instead of relying on a Planet object
        return new BuilderDimensionEffects(hasClouds, hasRain, hasAtmosphere, breathable);
    }

    /**
     * A DimensionSpecialEffects implementation that uses builder values directly.
     * This fixes the issue where the anonymous class with planet=null couldn't set cloud height/rain.
     */
    private static class BuilderDimensionEffects extends DimensionSpecialEffects {
        private final boolean hasAtmosphere;
        private final boolean breathable;

        public BuilderDimensionEffects(boolean hasClouds, boolean hasRain, boolean hasAtmosphere, boolean breathable) {
            super(
                hasClouds ? 192.0f : Float.NaN,  // Cloud height
                hasRain,                          // Has precipitation
                SkyType.NORMAL,                   // Sky type
                false,                            // Force bright lightmap
                !breathable                       // Force dark water (true for harsh environments)
            );
            this.hasAtmosphere = hasAtmosphere;
            this.breathable = breathable;
        }

        @Override
        public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
            if (!hasAtmosphere) {
                // No atmosphere = black space (like End but darker)
                return fogColor.scale(0.05);
            }

            // Follow vanilla Overworld pattern: multiply input fog color by brightness factors
            // This preserves the biome's fog color while adjusting for day/night cycle
            return fogColor.multiply(
                brightness * 0.94F + 0.06F,
                brightness * 0.94F + 0.06F,
                brightness * 0.91F + 0.09F
            );
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            // Follow vanilla Overworld: NOT foggy (return false)
            // Dense fog is controlled by biome effects, not dimension effects
            return false;
        }

        @Override
        public float[] getSunriseColor(float timeOfDay, float partialTicks) {
            if (!hasAtmosphere) {
                return null; // No sunrise colors in space
            }

            // Show sunrise/sunset colors during appropriate times
            if (timeOfDay > 0.23f && timeOfDay < 0.27f) {
                // Sunrise window
                float intensity = 1.0f - Math.abs(timeOfDay - 0.25f) / 0.02f;
                return new float[]{intensity * 0.85f, intensity * 0.5f, intensity * 0.4f, intensity};
            } else if (timeOfDay > 0.73f && timeOfDay < 0.77f) {
                // Sunset window
                float intensity = 1.0f - Math.abs(timeOfDay - 0.75f) / 0.02f;
                return new float[]{intensity * 0.85f, intensity * 0.5f, intensity * 0.4f, intensity};
            }

            return null;
        }
    }

    /**
     * Load planet data from resource manager during client startup
     */
    private static void loadPlanetsFromResourceManager(ResourceManager resourceManager) {
        AdAstraMekanized.LOGGER.info("Loading planet JSON files from resource manager...");

        PlanetRegistry registry = PlanetRegistry.getInstance();
        int loadedCount = 0;

        try {
            // Find all planet JSON files in data packs
            Map<ResourceLocation, Resource> planetResources = resourceManager.listResources(
                "planets", location -> location.getPath().endsWith(".json"));

            for (Map.Entry<ResourceLocation, Resource> entry : planetResources.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey();
                Resource resource = entry.getValue();

                try {
                    // Extract planet ID from resource location
                    ResourceLocation planetId = extractPlanetId(resourceLocation);

                    if (planetId == null) {
                        AdAstraMekanized.LOGGER.warn("Could not extract planet ID from: {}", resourceLocation);
                        continue;
                    }

                    // Load and parse planet JSON
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement jsonElement = GsonHelper.parse(reader);

                        var result = Planet.CODEC.parse(JsonOps.INSTANCE, jsonElement);

                        if (result.error().isPresent()) {
                            AdAstraMekanized.LOGGER.error("Failed to parse planet {}: {}",
                                planetId, result.error().get().message());
                            continue;
                        }

                        Planet planet = result.result().orElse(null);
                        if (planet != null && registry.registerPlanet(planet)) {
                            loadedCount++;
                            AdAstraMekanized.LOGGER.debug("Loaded planet: {}", planetId);
                        }
                    }
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Error loading planet from {}: {}",
                        resourceLocation, e.getMessage());
                }
            }

            AdAstraMekanized.LOGGER.info("Loaded {} planets from JSON files", loadedCount);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to load planets from resource manager", e);
        }
    }

    /**
     * Extract planet ID from resource location
     * data/namespace/planets/planet_name.json -> namespace:planet_name
     */
    private static ResourceLocation extractPlanetId(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();

        // Remove "planets/" prefix and ".json" suffix
        if (!path.startsWith("planets/") || !path.endsWith(".json")) {
            return null;
        }

        String planetName = path.substring(8, path.length() - 5); // "planets/".length() = 8

        if (planetName.isEmpty() || !ResourceLocation.isValidPath(planetName)) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), planetName);
    }

    private static void registerPlanetEffect(RegisterDimensionSpecialEffectsEvent event, String planetId, DimensionSpecialEffects effects) {
        ResourceLocation dimensionType = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetId);
        event.register(dimensionType, effects);
        AdAstraMekanized.LOGGER.debug("Registered dimension effects for: {}", planetId);
    }

}