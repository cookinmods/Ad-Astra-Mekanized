package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.GravityZoneRenderer;
import com.hecookin.adastramekanized.client.renderers.OxygenZoneRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Client-side render event handlers
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        // Render oxygen and gravity zones after translucent blocks
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        OxygenZoneRenderer oxygenRenderer = OxygenZoneRenderer.getInstance();
        GravityZoneRenderer gravityRenderer = GravityZoneRenderer.getInstance();

        boolean oxygenEnabled = oxygenRenderer.isRenderingEnabled();
        boolean gravityEnabled = gravityRenderer.isRenderingEnabled();

        // Skip if neither renderer is active
        if (!oxygenEnabled && !gravityEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = camera.getPosition();

        // Get render buffers
        RenderBuffers renderBuffers = mc.renderBuffers();
        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();

        poseStack.pushPose();

        // Render oxygen zones
        if (oxygenEnabled) {
            oxygenRenderer.render(poseStack, bufferSource, cameraPos);
        }

        // Render gravity zones
        if (gravityEnabled) {
            gravityRenderer.render(poseStack, bufferSource, cameraPos);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }
}