package com.hecookin.adastramekanized.client.loading;

import com.hecookin.adastramekanized.client.rendering.StarfieldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Custom loading overlay that displays a starfield instead of the default Mojang orange screen.
 * Creates an immersive space-themed loading experience for Ad Astra Mekanized.
 */
public class StarfieldLoadingOverlay extends LoadingOverlay {

    // Store our own references since parent fields are private
    private final Minecraft mc;
    private final ReloadInstance reloadInstance;

    private final long startTime;
    private float fadeOutProgress = 0f;
    private boolean fadingOut = false;

    public StarfieldLoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance,
                                   Consumer<Optional<Throwable>> onFinish, boolean fadeIn) {
        super(minecraft, reloadInstance, onFinish, fadeIn);
        this.mc = minecraft;
        this.reloadInstance = reloadInstance;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int screenWidth = this.mc.getWindow().getGuiScaledWidth();
        int screenHeight = this.mc.getWindow().getGuiScaledHeight();

        long elapsed = System.currentTimeMillis() - startTime;

        // Check if we should fade out
        if (this.reloadInstance.isDone() && !fadingOut) {
            fadingOut = true;
        }

        if (fadingOut) {
            fadeOutProgress += partialTick * 0.05f;
            if (fadeOutProgress >= 1f) {
                fadeOutProgress = 1f;
            }
        }

        // Calculate alpha for fade in/out
        float alpha = 1f;
        if (elapsed < 500) {
            // Fade in during first 500ms
            alpha = elapsed / 500f;
        }
        if (fadingOut) {
            alpha = 1f - fadeOutProgress;
        }

        // Use the shared starfield renderer
        StarfieldRenderer.getInstance().render(guiGraphics, screenWidth, screenHeight, alpha);

        // Render loading progress bar
        renderProgressBar(guiGraphics, screenWidth, screenHeight, alpha);

        // Render mod name
        renderModName(guiGraphics, screenWidth, screenHeight, alpha);

        // Call super to handle the actual loading logic (but not rendering)
        // We need to trigger the completion callback
        if (fadeOutProgress >= 1f) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float alpha) {
        // Don't render if alpha is too low or font not available
        if (alpha < 0.1f || this.mc.font == null) {
            return;
        }

        float progress = this.reloadInstance.getActualProgress();

        int barWidth = 200;
        int barHeight = 4;
        int barX = (screenWidth - barWidth) / 2;
        int barY = screenHeight / 2 + 40;

        int bgAlpha = (int) (alpha * 100);
        int fgAlpha = (int) (alpha * 255);

        // Background
        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1,
            (bgAlpha << 24) | 0x333333);

        // Progress fill with gradient effect
        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            // Main progress bar (blue-purple gradient feel)
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight,
                (fgAlpha << 24) | 0x6688FF);

            // Highlight on top
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + 1,
                (fgAlpha << 24) | 0x99BBFF);
        }

        // Percentage text - use full opacity white, alpha controls visibility via threshold
        String percentText = String.format("%.0f%%", progress * 100);
        guiGraphics.drawCenteredString(this.mc.font, percentText,
            screenWidth / 2, barY + barHeight + 6, 0xFFFFFFFF);
    }

    private void renderModName(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float alpha) {
        // Don't render if alpha is too low or font not available
        if (alpha < 0.1f || this.mc.font == null) {
            return;
        }

        // Mod title - use full opacity, color indicates theme
        String title = "AD ASTRA MEKANIZED";
        guiGraphics.drawCenteredString(this.mc.font, title,
            screenWidth / 2, screenHeight / 2 - 20, 0xFF6688FF);

        // Tagline
        String tagline = "Explore the cosmos";
        guiGraphics.drawCenteredString(this.mc.font, tagline,
            screenWidth / 2, screenHeight / 2 - 5, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
