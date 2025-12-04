package com.hecookin.adastramekanized.mixins.client;

import com.hecookin.adastramekanized.client.rendering.StarfieldRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace the default Minecraft title screen background with a starfield.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    /**
     * Inject at the start of render to draw the starfield background.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void renderStarfieldBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int width = screen.width;
        int height = screen.height;

        // Render starfield background
        StarfieldRenderer.getInstance().render(guiGraphics, width, height, 1.0f);
    }
}
