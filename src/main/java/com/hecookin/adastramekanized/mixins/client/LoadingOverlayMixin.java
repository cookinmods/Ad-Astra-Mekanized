package com.hecookin.adastramekanized.mixins.client;

import com.hecookin.adastramekanized.client.loading.StarfieldLoadingOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Mixin to replace the default Mojang loading overlay with our custom starfield overlay.
 */
@Mixin(Minecraft.class)
public abstract class LoadingOverlayMixin {

    @Shadow
    @Nullable
    private Overlay overlay;

    /**
     * Intercept the setOverlay call and replace LoadingOverlay with our StarfieldLoadingOverlay.
     */
    @Inject(method = "setOverlay", at = @At("HEAD"), cancellable = true)
    private void onSetOverlay(Overlay newOverlay, CallbackInfo ci) {
        if (newOverlay instanceof LoadingOverlay loadingOverlay && !(newOverlay instanceof StarfieldLoadingOverlay)) {
            // Replace with our custom starfield overlay
            try {
                // Access the fields from LoadingOverlay via reflection or accessor
                Minecraft minecraft = (Minecraft) (Object) this;

                // Get the reload instance and callback from the original overlay
                // We need to use an accessor mixin for this
                ReloadInstance reloadInstance = ((LoadingOverlayAccessor) loadingOverlay).getReload();
                Consumer<Optional<Throwable>> onFinish = ((LoadingOverlayAccessor) loadingOverlay).getOnFinish();
                boolean fadeIn = ((LoadingOverlayAccessor) loadingOverlay).getFadeIn();

                // Create our custom overlay
                StarfieldLoadingOverlay starfieldOverlay = new StarfieldLoadingOverlay(
                    minecraft, reloadInstance, onFinish, fadeIn
                );

                this.overlay = starfieldOverlay;
                ci.cancel();
            } catch (Exception e) {
                // If anything goes wrong, fall back to the default overlay
                // This ensures the game still loads even if our overlay fails
            }
        }
    }
}
