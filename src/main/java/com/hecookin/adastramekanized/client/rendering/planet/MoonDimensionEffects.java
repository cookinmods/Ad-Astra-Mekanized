package com.hecookin.adastramekanized.client.rendering.planet;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Moon-specific dimension effects based on actual Moon planetary data.
 *
 * Moon Properties (from moon.json):
 * - Gravity: 0.165 (16.5% of Earth)
 * - Temperature: -173°C (extremely cold)
 * - Atmosphere: None (0.0 pressure, not breathable)
 * - Sky: Black space (color 0), no sunrise
 * - Fog: Very minimal gray (1447444), density 0.05
 * - Weather: No weather at all
 * - Stars: Constant visibility, 25000 stars, brightness 2.0
 */
public class MoonDimensionEffects extends DimensionSpecialEffects {

    // Moon atmospheric colors from JSON
    private static final int SKY_COLOR = 0;            // Black space
    private static final int FOG_COLOR = 1447444;     // Very dark gray
    private static final int SUNRISE_COLOR = 0;       // No sunrise

    public MoonDimensionEffects() {
        super(
            // Cloud height - Moon has no atmosphere, no clouds
            Float.NaN,
            // Has precipitation - Moon has no weather
            false,
            // Sky type - normal sky but black
            SkyType.NORMAL,
            // Force bright lightmap - false for natural lighting
            false,
            // Force dark water - true due to lack of atmosphere
            true
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 skyColor, float celestialAngle) {
        // Moon has no atmosphere - fog color is constant black space, no sunrise/sunset
        // Return pure black to prevent any color transitions that look like sunrises
        return new Vec3(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Moon has no atmosphere, so no fog except very minimal surface effects
        // Only very rarely foggy due to dust kicked up by impacts
        return (x * z) % 1000 == 0; // Extremely rare "fog" from dust
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // No sunrise/sunset colors on the Moon - space has no atmosphere to scatter light
        return null;
    }

    /**
     * Get Moon sky color (black space)
     */
    public static Vec3 getMoonSkyColor() {
        return new Vec3(0.0, 0.0, 0.0); // Pure black space
    }

    /**
     * Get Moon sunrise color (none)
     */
    public static Vec3 getMoonSunriseColor() {
        return new Vec3(0.0, 0.0, 0.0); // No sunrise color
    }

    /**
     * Check if stars should be visible (always on Moon)
     */
    public static boolean shouldShowStars() {
        return true; // Moon always shows stars due to no atmosphere
    }

    /**
     * Get star brightness for Moon
     */
    public static float getStarBrightness() {
        return 2.0f; // Very bright stars due to no atmospheric interference
    }
}