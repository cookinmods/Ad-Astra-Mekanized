package com.hecookin.adastramekanized.client.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.overlay.OxygenOverlayRenderer;
import com.hecookin.adastramekanized.client.overlay.VehicleOverlayRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Handles GUI overlay rendering events
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class RenderOverlayEvent {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        VehicleOverlayRenderer.render(event.getGuiGraphics(), partialTick);
        OxygenOverlayRenderer.render(event.getGuiGraphics(), partialTick);
    }
}
