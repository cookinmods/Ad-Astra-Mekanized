package com.hecookin.adastramekanized.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Steel door that supports toggling between redstone-only and hand-openable modes.
 * Defaults to redstone-only (like iron doors). Use the configurator to toggle.
 */
public class SteelDoorBlock extends DoorBlock {

    public static final BooleanProperty REQUIRES_REDSTONE = BooleanProperty.create("requires_redstone");

    public SteelDoorBlock(BlockSetType blockSetType, Properties properties) {
        super(blockSetType, properties);
        registerDefaultState(defaultBlockState().setValue(REQUIRES_REDSTONE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(REQUIRES_REDSTONE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(REQUIRES_REDSTONE)) {
            return InteractionResult.PASS;
        }
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 10);
        level.playSound(player, pos,
            state.getValue(OPEN) ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
            SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
