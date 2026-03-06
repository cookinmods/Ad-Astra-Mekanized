package com.hecookin.adastramekanized.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Steel trapdoor that supports toggling between redstone-only and hand-openable modes.
 * Defaults to redstone-only (like iron trapdoors). Use the configurator to toggle.
 */
public class SteelTrapdoorBlock extends TrapDoorBlock {

    public static final BooleanProperty REQUIRES_REDSTONE = BooleanProperty.create("requires_redstone");

    public SteelTrapdoorBlock(BlockSetType blockSetType, Properties properties) {
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
        level.setBlock(pos, state, 2);
        if (state.getValue(OPEN)) {
            level.levelEvent(player, 1007, pos, 0);
        } else {
            level.levelEvent(player, 1013, pos, 0);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
