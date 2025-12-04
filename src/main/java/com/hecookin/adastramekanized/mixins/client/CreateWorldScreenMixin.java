package com.hecookin.adastramekanized.mixins.client;

import com.hecookin.adastramekanized.client.rendering.StarfieldRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace the default Create World screen background with a starfield.
 */
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderStarfieldBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        StarfieldRenderer.getInstance().render(guiGraphics, screen.width, screen.height, 1.0f);
    }
}
