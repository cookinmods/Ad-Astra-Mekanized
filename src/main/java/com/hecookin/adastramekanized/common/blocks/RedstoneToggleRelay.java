package com.hecookin.adastramekanized.common.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.data.ButtonControllerManager;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * A relay block that toggles linked oxygen and gravity distributors when it receives a redstone signal.
 * Can be paired with an Oxygen Network Controller via right-click.
 */
public class RedstoneToggleRelay extends DirectionalBlock {

    private static final MapCodec<RedstoneToggleRelay> CODEC = simpleCodec(RedstoneToggleRelay::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneToggleRelay(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(POWERED, powered);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Check binding status
        ButtonControllerManager.ControllerBinding binding = ButtonControllerManager.getBinding(level, pos);
        if (binding == null) {
            player.displayClientMessage(
                Component.literal("No controller linked. Sneak+right-click with controller to pair.")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
        } else {
            // Show current status
            DistributorLinkData linkData = new DistributorLinkData();
            if (binding.controllerData.contains("LinkData")) {
                linkData.fromNbt(binding.controllerData.getCompound("LinkData"));
            }

            int enabledCount = 0;
            for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
                if (link.isEnabled()) enabledCount++;
            }

            player.displayClientMessage(
                Component.literal("Controller linked with ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(linkData.getLinkCount() + " distributors")
                        .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" (" + enabledCount + " active)")
                        .withStyle(ChatFormatting.GRAY)),
                true
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);

            if (hasSignal != wasPowered) {
                // Update block state
                level.setBlock(pos, state.setValue(POWERED, hasSignal), 2);

                // Toggle distributors when signal changes
                if (hasSignal) {
                    toggleDistributors(level, pos, null);
                }
            }
        }
    }

    private void toggleDistributors(Level level, BlockPos relayPos, Player player) {
        // Check if we have a controller bound
        ButtonControllerManager.ControllerBinding binding = ButtonControllerManager.getBinding(level, relayPos);
        if (binding == null) {
            if (player != null) {
                player.displayClientMessage(
                    Component.literal("No controller linked to this relay")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        // Recreate link data from saved controller data
        DistributorLinkData linkData = new DistributorLinkData();
        if (binding.controllerData.contains("LinkData")) {
            linkData.fromNbt(binding.controllerData.getCompound("LinkData"));
        }

        // Count current state
        int enabledCount = 0;
        int totalCount = linkData.getLinkCount();

        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            if (link.isEnabled()) {
                enabledCount++;
            }
        }

        // Determine new state (if any are on, turn all off; if all are off, turn all on)
        boolean newState = enabledCount == 0;
        int successCount = 0;

        // Apply new state to all distributors
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            link.setEnabled(newState);

            BlockPos distributorPos = link.getPos();
            if (level.isLoaded(distributorPos)) {
                BlockEntity be = level.getBlockEntity(distributorPos);
                if (be instanceof ImprovedOxygenDistributor distributor) {
                    distributor.setManuallyDisabled(!newState);
                    successCount++;
                    AdAstraMekanized.LOGGER.info("Relay at {} toggled oxygen distributor at {} to {}",
                        relayPos, distributorPos, newState ? "ON" : "OFF");
                } else if (be instanceof GravityNormalizerBlockEntity gravityNormalizer) {
                    gravityNormalizer.setManuallyDisabled(!newState);
                    successCount++;
                    AdAstraMekanized.LOGGER.info("Relay at {} toggled gravity normalizer at {} to {}",
                        relayPos, distributorPos, newState ? "ON" : "OFF");
                }
            }
        }

        // Update the stored controller data with new states
        binding.controllerData.put("LinkData", linkData.toNbt());
        ButtonControllerManager.updateBinding(level, relayPos, binding);

        // Play sound effect
        level.playSound(null, relayPos,
            newState ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT,
            SoundSource.BLOCKS, 0.5F, 1.0F);

        // Send feedback to nearby players if triggered by redstone
        if (player == null) {
            String action = newState ? "Activated" : "Deactivated";
            for (Player nearbyPlayer : level.players()) {
                if (nearbyPlayer.position().distanceToSqr(relayPos.getX() + 0.5, relayPos.getY() + 0.5, relayPos.getZ() + 0.5) < 64) {
                    nearbyPlayer.displayClientMessage(
                        Component.literal("Relay " + action.toLowerCase() + " ")
                            .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                            .append(Component.literal(successCount + "/" + totalCount + " distributors")
                                .withStyle(ChatFormatting.WHITE)),
                        true
                    );
                }
            }
        } else {
            // Direct player interaction feedback
            String action = newState ? "Enabled" : "Disabled";
            player.displayClientMessage(
                Component.literal(action + " ")
                    .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(Component.literal(successCount + "/" + totalCount + " distributors")
                        .withStyle(ChatFormatting.WHITE)),
                true
            );
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Remove controller binding when relay is broken
            if (!level.isClientSide) {
                ButtonControllerManager.removeBinding(level, pos);
                AdAstraMekanized.LOGGER.info("Removed controller binding from relay at {}", pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Toggles linked oxygen & gravity distributors")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak+right-click with controller to pair")
            .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Apply redstone signal to toggle")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}