package com.hecookin.adastramekanized.mixins;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.sky.CelestialBodyConverter;
import com.hecookin.adastramekanized.client.sky.CelestialSkyRenderer;
import com.hecookin.adastramekanized.client.sky.SkyRenderable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    public ClientLevel level;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void replaceCustomSkyRendering(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick,
                                         Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {

        if (level == null) {
            return;
        }

        ResourceKey<Level> dimensionKey = level.dimension();
        ResourceLocation planetId = mapDimensionToPlanetId(dimensionKey.location());

        // Skip if no planet ID mapping exists (e.g., Nether, End)
        if (planetId == null) {
            return;
        }

        Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

        // Debug: Check if planet was found
        if (planet == null) {
            AdAstraMekanized.LOGGER.debug("Planet not found in registry for dimension: {} (registry has {} planets)",
                dimensionKey.location(), PlanetRegistry.getInstance().getPlanetCount());
        }

        // Only replace sky rendering for planets with custom celestial bodies
        if (planet != null && planet.rendering() != null && planet.rendering().celestialBodies() != null) {
            // Cancel vanilla sky rendering
            ci.cancel();

            // Set up fog
            setupFog.run();

            // Create a PoseStack for compatibility with our renderer
            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().mul(modelViewMatrix);

            // Render stars first (background layer)
            if (planet.rendering().sky() != null) {
                CelestialSkyRenderer.renderStars(
                    poseStack,
                    projectionMatrix,
                    partialTick,
                    level,
                    planet.rendering().sky()
                );
            }

            // Render our custom celestial bodies on top of stars
            List<SkyRenderable> celestialBodies = CelestialBodyConverter.convertCelestialBodies(
                planet.rendering().celestialBodies()
            );

            if (!celestialBodies.isEmpty()) {
                CelestialSkyRenderer.renderCelestialBodies(
                    poseStack,
                    projectionMatrix,
                    partialTick,
                    level,
                    camera,
                    celestialBodies
                );
            }
        }
    }

    private static ResourceLocation mapDimensionToPlanetId(ResourceLocation dimensionLocation) {
        // Simple mapping for our custom dimensions
        if (dimensionLocation.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return dimensionLocation; // Direct mapping for our mod dimensions
        }

        // Map vanilla dimensions to our planet IDs if we have planet data for them
        return switch (dimensionLocation.toString()) {
            case "minecraft:overworld" -> null; // Use vanilla sky rendering
            case "minecraft:the_nether" -> null; // No celestial bodies in Nether
            case "minecraft:the_end" -> null; // No celestial bodies in End
            default -> null;
        };
    }
}