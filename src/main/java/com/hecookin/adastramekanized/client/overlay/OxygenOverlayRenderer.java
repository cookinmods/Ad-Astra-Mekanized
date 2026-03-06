package com.hecookin.adastramekanized.client.overlay;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.items.armor.SpaceSuitItem;
import com.hecookin.adastramekanized.common.items.armor.base.ItemChemicalArmor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the oxygen tank HUD overlay when the player is wearing a full space suit.
 * Shows a vertical fill bar with percentage text, color-coded by atmosphere status.
 */
public class OxygenOverlayRenderer {

    private static final ResourceLocation OXYGEN_TANK_EMPTY = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "overlay/oxygen_tank_empty");
    private static final ResourceLocation OXYGEN_TANK = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/sprites/overlay/oxygen_tank.png");

    private static final int BAR_X = 5;
    private static final int BAR_Y = 25;

    public static void render(GuiGraphics graphics, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return;
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) return;
        if (!SpaceSuitItem.hasFullSet(player)) return;

        var chestStack = player.getInventory().getArmor(2);
        if (!(chestStack.getItem() instanceof ItemChemicalArmor chemArmor)) return;

        long amount = SpaceSuitItem.getOxygenAmount(player);
        long capacity = chemArmor.getCapacity();
        if (capacity <= 0) return;

        double ratio = (double) amount / capacity;
        int barHeight = (int) (ratio * 52);

        var font = Minecraft.getInstance().font;
        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        // Empty tank background
        graphics.blitSprite(OXYGEN_TANK_EMPTY, BAR_X, BAR_Y, 62, 52);
        // Filled portion (clips from bottom up)
        graphics.blit(OXYGEN_TANK, BAR_X, BAR_Y + 52 - barHeight, 0, 52 - barHeight, 62, barHeight, 62, 52);

        // Percentage text below the tank
        var text = String.format("%.1f%%", ratio * 100);
        int textWidth = font.width(text);
        int color = getTextColor(ratio);
        graphics.drawString(font, text, (int) (BAR_X + (62 - textWidth) / 2f), BAR_Y + 52 + 3, color);
        poseStack.popPose();
    }

    private static int getTextColor(double ratio) {
        if (ratio <= 0) return 0xDC143C; // Red: empty

        // Green if in a breathable atmosphere
        var player = Minecraft.getInstance().player;
        if (player != null) {
            ResourceLocation dimId = player.level().dimension().location();
            if (dimId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
                PlanetRegistry registry = PlanetRegistry.getInstance();
                Planet planet = registry.getPlanet(dimId);
                if (planet != null && planet.atmosphere() != null && planet.atmosphere().breathable()) {
                    return 0x55ff55; // Green: breathable
                }
            } else {
                // Non-mod dimensions (overworld, nether, end) are breathable
                return 0x55ff55;
            }
        }

        return 0xFFFFFF; // White: non-breathable
    }
}
