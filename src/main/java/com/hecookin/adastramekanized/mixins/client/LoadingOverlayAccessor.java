package com.hecookin.adastramekanized.mixins.client;

import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Accessor mixin to access private fields of LoadingOverlay.
 */
@Mixin(LoadingOverlay.class)
public interface LoadingOverlayAccessor {

    @Accessor("reload")
    ReloadInstance getReload();

    @Accessor("onFinish")
    Consumer<Optional<Throwable>> getOnFinish();

    @Accessor("fadeIn")
    boolean getFadeIn();
}
