package com.hecookin.adastramekanized.mixins.client;

import com.hecookin.adastramekanized.client.rendering.StarfieldRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace the default pause screen background with a starfield.
 */
@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    /**
     * Inject at the start of render to draw the starfield background.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void renderStarfieldBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        PauseScreen screen = (PauseScreen) (Object) this;
        int width = screen.width;
        int height = screen.height;

        // Render starfield background with slight transparency to see game behind
        StarfieldRenderer.getInstance().render(guiGraphics, width, height, 0.85f);
    }
}
