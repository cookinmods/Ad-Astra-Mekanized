package com.hecookin.adastramekanized.client.renderers.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.SlidingDoorBlockEntity;
import com.hecookin.adastramekanized.common.blocks.SlidingDoorBlock;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renderer for sliding door animations.
 * Uses Ad Astra's exact coordinate system and rendering approach.
 */
public class SlidingDoorBlockEntityRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity> {

    public SlidingDoorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Context not needed for this renderer
    }

    @Override
    public void render(SlidingDoorBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Get sliding progress (matching Ad Astra's animation)
        float slide = Mth.lerp(partialTick, entity.lastSlideTicks(), entity.slideTicks()) / 81.0f;
        var state = entity.getBlockState();
        var direction = state.getValue(SlidingDoorBlock.FACING);
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getBlockRenderer().getBlockModel(state);

        // Check if this is a reinforced door (needs special handling)
        boolean isReinforcedDoor = state.is(ModBlocks.REINFORCED_DOOR.get());

        poseStack.pushPose();

        // The block entity is at BOTTOM (center-bottom of the 3x3)
        // Parts extend clockwise from facing, so for NORTH, they go EAST
        // We need to translate to the center of the 3x3 area

        // First, apply the facing rotation
        poseStack.translate(0.5f, 1, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(direction.toYRot()));
        poseStack.translate(-0.5f, 0, -0.5f);

        // Now in local space, the door extends from -1 to +1 on the X axis
        // The block entity is at X=0, so no additional offset needed

        // Apply base Z offset and first door panel position
        poseStack.translate(slide, 0, 0.0625f);

        // Z-axis adjustment for proper door depth
        if (direction.getAxis() == Direction.Axis.Z) {
            poseStack.translate(0, 0, 0.6875f);
            // Special adjustment for reinforced door
            if (isReinforcedDoor) {
                poseStack.translate(0, 0, -0.3125f);
            }
        }

        // Render first door panel
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
            poseStack.last(),
            buffer.getBuffer(Sheets.cutoutBlockSheet()),
            state,
            model,
            1f, 1f, 1f,
            packedLight, packedOverlay);

        // Move to second door position
        poseStack.translate(-slide - slide, 0, 0);

        if (!isReinforcedDoor) {
            // For regular doors, flip 180 degrees
            poseStack.translate(0.5f, 0, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(-0.5f, 0, -0.5f);
            poseStack.translate(0, 0, 0.8125f);

            // Render second door panel
            minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.cutoutBlockSheet()),
                state,
                model,
                1f, 1f, 1f,
                packedLight, packedOverlay);
        } else {
            // For reinforced door, load and render the flipped model
            poseStack.translate(-1.25f, 0, 0);

            // Load the flipped model (now registered in ClientModEvents)
            ModelResourceLocation flippedModelLocation = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(
                    AdAstraMekanized.MOD_ID,
                    "block/reinforced_door_flipped"
                )
            );
            BakedModel flippedModel = minecraft.getModelManager().getModel(flippedModelLocation);

            // Render the flipped model
            minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.cutoutBlockSheet()),
                state,
                flippedModel,
                1f, 1f, 1f,
                packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

}