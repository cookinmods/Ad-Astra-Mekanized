package com.hecookin.adastramekanized.client.overlay;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.constants.RocketConstants;
import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Locale;

/**
 * Renders HUD overlays for vehicles (rockets and landers).
 * Includes launch countdown, altitude indicator, and landing warnings.
 */
public class VehicleOverlayRenderer {

    private static final ResourceLocation ROCKET_BAR = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "overlay/rocket_bar");
    private static final ResourceLocation ROCKET_ICON = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "overlay/rocket");

    /**
     * Main render method called from client render events
     */
    public static void render(GuiGraphics graphics, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return;
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) return;

        var vehicle = player.getVehicle();
        if (vehicle == null) return;

        if (vehicle instanceof Rocket rocket) {
            renderRocketOverlay(graphics, rocket);
        } else if (vehicle instanceof Lander lander) {
            renderLanderOverlay(graphics, lander, partialTick);
        }
    }

    /**
     * Render rocket-specific overlays (launch countdown, altitude)
     */
    private static void renderRocketOverlay(GuiGraphics graphics, Rocket rocket) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        PoseStack poseStack = graphics.pose();

        // Launch countdown display (large centered numbers)
        if (rocket.isLaunching()) {
            int countdown = Mth.ceil(rocket.launchTicks() / 20f);
            poseStack.pushPose();
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(4, 4, 4);
            graphics.drawCenteredString(font, String.valueOf(countdown), 0, -10, 0xe53253);
            poseStack.popPose();
        }

        // Altitude indicator bar (left side of screen with padding)
        int barPadding = 4;
        graphics.blitSprite(ROCKET_BAR, barPadding, height / 2, 16, 128);

        // Rocket icon moves up the bar from y=100 to y=600 (matches Ad Astra formula)
        poseStack.pushPose();
        double y = Mth.clamp(rocket.getY(), 100, 600);
        poseStack.translate(0.3f, (600 - y - 500) / 4.5, 0);
        graphics.blitSprite(ROCKET_ICON, barPadding + 3, height / 2 + 113, 8, 11);
        poseStack.popPose();
    }

    /**
     * Render lander-specific overlays (distance to ground, thruster hint)
     */
    private static void renderLanderOverlay(GuiGraphics graphics, Lander lander, float partialTick) {
        var level = lander.level();
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        PoseStack poseStack = graphics.pose();

        // Only show landing UI when not yet grounded
        if (level.getBlockState(lander.getOnPos().below(2)).isAir()) {
            // Calculate distance to ground
            int ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, lander.blockPosition()).getY();
            int distance = Math.max(0, lander.blockPosition().getY() - ground);

            poseStack.pushPose();
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(1.4f, 1.4f, 1.4f);

            // Thruster hint with fade based on descent speed
            float alpha = Mth.clamp(0.1f - (float) (lander.getDeltaMovement().y() + 0.5), 0, 1);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, alpha);

            String spaceKey = minecraft.options.keyJump.getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT);
            graphics.drawCenteredString(font,
                "HOLD " + spaceKey + " TO SLOW DESCENT",
                0, 60, 0xe53253);

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            // Distance meter with color coding
            int distanceColor = getDistanceColor(distance);
            graphics.drawCenteredString(font,
                distance + "m",
                0, 30, distanceColor);

            poseStack.popPose();
        }
    }

    /**
     * Get color for distance display based on safety zones
     * Green (>300m) -> Yellow (100-300m) -> Red (<100m)
     */
    private static int getDistanceColor(int distance) {
        if (distance < 100) {
            return 0xff5555; // Red - danger zone
        } else if (distance < 300) {
            return 0xffff55; // Yellow - caution zone
        } else {
            return 0x55ff55; // Green - safe zone
        }
    }
}
