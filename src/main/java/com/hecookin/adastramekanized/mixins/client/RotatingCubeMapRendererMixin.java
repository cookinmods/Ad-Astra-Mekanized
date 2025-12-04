package com.hecookin.adastramekanized.mixins.client;

import com.hecookin.adastramekanized.client.rendering.StarfieldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace the rotating panorama background with a starfield.
 * This affects all screens that use the panorama (title screen, loading screens, etc.)
 */
@Mixin(PanoramaRenderer.class)
public class RotatingCubeMapRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderStarfieldInstead(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        // Render our starfield instead of the panorama
        StarfieldRenderer.getInstance().render(guiGraphics, width, height, fade);

        // Cancel the original panorama rendering
        ci.cancel();
    }
}
