package com.hecookin.adastramekanized.client.rendering;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Custom dimension special effects for planets.
 *
 * Provides planet-specific atmospheric rendering including:
 * - Sky color based on atmosphere composition
 * - Fog rendering for atmospheric density
 * - Star visibility based on planet properties
 * - Celestial body rendering
 */
public class PlanetDimensionEffects extends DimensionSpecialEffects {

    private final Planet planet;

    public PlanetDimensionEffects(Planet planet) {
        super(
            // Cloud height - check if clouds are enabled in rendering config
            (planet != null && planet.rendering().weather().hasClouds()) ? 192.0f : Float.NaN,
            // Has precipitation - based on planet weather
            planet != null && planet.rendering().weather().hasRain(),
            // Sky type - always normal for planets
            SkyType.NORMAL,
            // Force bright lightmap - false for natural lighting
            false,
            // Force dark water - true for harsh environments
            planet == null || !planet.atmosphere().breathable()
        );
        this.planet = planet;

        // Debug cloud configuration
        if (planet != null) {
            boolean hasClouds = planet.rendering().weather().hasClouds();
            float cloudHeight = hasClouds ? 192.0f : Float.NaN;
            AdAstraMekanized.LOGGER.info("Planet {} clouds: {} (height: {})",
                planet.id(), hasClouds, cloudHeight);
        }
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        // Handle null planet case or no atmosphere
        if (planet == null || !planet.atmosphere().hasAtmosphere()) {
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
        // Handle null planet case or no atmosphere
        if (planet == null || !planet.atmosphere().hasAtmosphere()) {
            return null; // No sunrise colors in space
        }

        // Only show sunrise/sunset colors during appropriate times
        // timeOfDay: 0.0 = sunrise, 0.5 = noon, 0.75 = sunset, 1.0 = midnight
        if (timeOfDay > 0.23f && timeOfDay < 0.27f) {
            // Sunrise window (around 0.25 = 6am)
            float intensity = 1.0f - Math.abs(timeOfDay - 0.25f) / 0.02f;
            return getSunriseColorFromPlanet(intensity);
        } else if (timeOfDay > 0.73f && timeOfDay < 0.77f) {
            // Sunset window (around 0.75 = 6pm)
            float intensity = 1.0f - Math.abs(timeOfDay - 0.75f) / 0.02f;
            return getSunriseColorFromPlanet(intensity);
        }

        return null; // No sunrise/sunset color during day or night
    }

    private float[] getSunriseColorFromPlanet(float intensity) {
        // Get sunrise color from planet rendering data
        int sunriseColorInt = planet.rendering().sky().sunriseColor();
        float r = ((sunriseColorInt >> 16) & 0xFF) / 255.0f;
        float g = ((sunriseColorInt >> 8) & 0xFF) / 255.0f;
        float b = (sunriseColorInt & 0xFF) / 255.0f;

        // Return RGBA with intensity
        return new float[]{r * intensity, g * intensity, b * intensity, intensity};
    }

    /**
     * Get the planet associated with these effects
     */
    public Planet getPlanet() {
        return planet;
    }

    /**
     * Create effects for a specific planet type
     */
    public static PlanetDimensionEffects createForPlanet(Planet planet) {
        return new PlanetDimensionEffects(planet);
    }
}