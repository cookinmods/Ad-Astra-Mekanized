package com.hecookin.adastramekanized.client.renderers.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Quaternionf;

public class GravityNormalizerBlockEntityRenderer implements BlockEntityRenderer<GravityNormalizerBlockEntity> {

    public static final ResourceLocation TOP_MODEL = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/gravity_normalizer_top");

    @Override
    public void render(GravityNormalizerBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();

        // Only spin if the entity is active (has power)
        float yRot = 0f;
        if (entity.isActive()) {
            // Get rotation for the spinning part with proper angle interpolation
            yRot = lerpAngle(partialTick, entity.lastYRot(), entity.yRot());
        }

        // Render the rotating top part
        renderTopPart(state, yRot, poseStack, buffer, packedLight, packedOverlay);
    }

    // Helper method to properly interpolate angles, handling wraparound
    private static float lerpAngle(float partialTick, float lastAngle, float currentAngle) {
        float diff = currentAngle - lastAngle;

        // Handle wraparound: if the difference is greater than 180, we crossed the 360/0 boundary
        if (diff > 180f) {
            diff -= 360f;
        } else if (diff < -180f) {
            diff += 360f;
        }

        return lastAngle + diff * partialTick;
    }

    private static void renderTopPart(BlockState state, float spinAngle, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel topModel = minecraft.getModelManager().getModel(ModelResourceLocation.standalone(TOP_MODEL));

        if (topModel == null) {
            return;
        }

        poseStack.pushPose();
        try {
            // Get orientation from block state
            AttachFace face = state.getValue(SidedMachineBlock.FACE);
            Direction facing = state.getValue(SidedMachineBlock.FACING);

            // Calculate blockstate-equivalent rotation angles
            float xRot = switch (face) {
                case FLOOR -> 0f;
                case WALL -> 90f;
                case CEILING -> 180f;
            };

            float yBlockRot = switch (facing) {
                case EAST -> 90f;
                case SOUTH -> 180f;
                case WEST -> 270f;
                default -> 0f; // NORTH
            };

            // Rotate around block center, matching Minecraft's blockstate rotation
            // then apply spin in local space so it spins around the correct axis
            poseStack.translate(0.5, 0.5, 0.5);

            // Apply orientation matching blockstate: rotateYXZ(-y, -x, 0)
            Quaternionf orientation = new Quaternionf().rotateYXZ(
                (float) Math.toRadians(-yBlockRot),
                (float) Math.toRadians(-xRot),
                0.0F
            );
            poseStack.mulPose(orientation);

            // Apply spin rotation in local space (around model's local Y axis)
            poseStack.mulPose(Axis.YP.rotationDegrees(-spinAngle));

            poseStack.translate(-0.5, -0.5, -0.5);

            // Render the top model
            minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.cutoutBlockSheet()),
                state,
                topModel,
                1.0f, 1.0f, 1.0f,
                packedLight, packedOverlay);
        } finally {
            poseStack.popPose();
        }
    }


    // Item renderer for inventory display
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {

        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            BlockState state = BuiltInRegistries.BLOCK.get(BuiltInRegistries.ITEM.getKey(stack.getItem())).defaultBlockState();

            Minecraft minecraft = Minecraft.getInstance();
            float yRot = 0f; // No rotation in inventory/hand - device is inactive

            poseStack.pushPose();
            try {
                // Apply standard block item rotation and scale for GUI display
                if (displayContext == ItemDisplayContext.GUI) {
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.scale(0.625f, 0.625f, 0.625f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(30));
                    poseStack.mulPose(Axis.YP.rotationDegrees(225));
                    poseStack.translate(-0.5, -0.5, -0.5);
                }

                // Render the base block model
                BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
                minecraft.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                    buffer.getBuffer(Sheets.cutoutBlockSheet()),
                    state,
                    model,
                    1.0f, 1.0f, 1.0f,
                    packedLight, packedOverlay);

                // Render the static top part (no rotation)
                renderTopPart(state, yRot, poseStack, buffer, packedLight, packedOverlay);
            } finally {
                poseStack.popPose();
            }
        }
    }
}
