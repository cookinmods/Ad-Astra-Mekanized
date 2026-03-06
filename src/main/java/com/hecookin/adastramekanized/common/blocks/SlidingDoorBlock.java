package com.hecookin.adastramekanized.common.blocks;

import com.hecookin.adastramekanized.common.blocks.base.BasicEntityBlock;
import com.hecookin.adastramekanized.common.blocks.properties.SlidingDoorPartProperty;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 3x3 multiblock sliding door with animation support.
 * Controller block (BOTTOM) handles animation through SlidingDoorBlockEntity.
 */
@SuppressWarnings("deprecation")
public class SlidingDoorBlock extends BasicEntityBlock {

    public static final MapCodec<SlidingDoorBlock> CODEC = simpleCodec(SlidingDoorBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<SlidingDoorPartProperty> PART = EnumProperty.create("part", SlidingDoorPartProperty.class);
    public static final BooleanProperty REQUIRES_REDSTONE = BooleanProperty.create("requires_redstone");

    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 1, 16, 16, 4);
    private static final VoxelShape EAST_SHAPE = Block.box(12, 0, 0, 15, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 12, 16, 16, 15);
    private static final VoxelShape WEST_SHAPE = Block.box(1, 0, 0, 4, 16, 16);

    public SlidingDoorBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.BLOCK), true,
              () -> com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes.SLIDING_DOOR.get());
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(POWERED, false)
            .setValue(PART, SlidingDoorPartProperty.BOTTOM)
            .setValue(REQUIRES_REDSTONE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, PART, REQUIRES_REDSTONE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var controllerState = level.getBlockState(getController(state, pos));
        if (!controllerState.getValues().containsKey(PART)) {
            return super.getCollisionShape(state, level, pos, context);
        }
        return controllerState.getValue(OPEN) || controllerState.getValue(POWERED) ?
            Shapes.empty() :
            super.getCollisionShape(state, level, pos, context);
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN) || state.getValue(POWERED);
            case WATER -> false;
        };
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        // All parts invisible - entire door rendered by block entity renderer
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Face toward the player when placed (opposite of where they're looking)
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Only the controller (BOTTOM) part has a block entity
        return state.getValue(PART).isController() ? super.newBlockEntity(pos, state) : null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return onUse(state, level, pos, player);
    }

    private InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        var controllerPos = getController(state, pos);
        var controllerState = level.getBlockState(controllerPos);
        if (controllerState.isAir()) return InteractionResult.PASS;

        // If requires redstone, don't allow hand-opening
        if (controllerState.getValue(REQUIRES_REDSTONE)) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Toggle open state for all parts
        var direction = state.getValue(FACING).getClockWise();
        boolean newOpen = !controllerState.getValue(OPEN);

        for (var part : SlidingDoorPartProperty.values()) {
            var partPos = controllerPos.relative(direction, part.xOffset()).above(part.yOffset());
            var partState = level.getBlockState(partPos);
            if (partState.getBlock() == this) {
                level.setBlockAndUpdate(partPos, partState.setValue(OPEN, newOpen));
            }
        }

        // Play door sound
        level.playSound(null, pos, newOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var direction = state.getValue(FACING).getClockWise();
        for (var part : SlidingDoorPartProperty.values()) {
            if (part != SlidingDoorPartProperty.BOTTOM) { // Don't replace the controller block
                var partPos = pos.relative(direction, part.xOffset()).above(part.yOffset());
                level.setBlock(partPos, state.setValue(PART, part), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockPos controllerPos = getController(state, pos);
            BlockState controllerState = level.getBlockState(controllerPos);
            if (controllerState.getBlock() instanceof SlidingDoorBlock) {
                boolean powered = level.hasNeighborSignal(pos);
                if (controllerState.getValue(POWERED) != powered) {
                    // Update all parts with powered state
                    var direction = state.getValue(FACING).getClockWise();
                    for (var part : SlidingDoorPartProperty.values()) {
                        var partPos = controllerPos.relative(direction, part.xOffset()).above(part.yOffset());
                        var partState = level.getBlockState(partPos);
                        if (partState.getBlock() == this) {
                            level.setBlock(partPos, partState.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
                        }
                    }

                    // Play sound if powered state changed
                    if (powered) {
                        level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!Block.canSupportRigidBlock(level, pos.below())) return false;
        Direction direction = state.getValue(FACING).getClockWise();
        for (var part : SlidingDoorPartProperty.values()) {
            if (part != SlidingDoorPartProperty.BOTTOM) { // Don't check the controller position
                BlockPos offset = pos.relative(direction, part.xOffset()).above(part.yOffset());
                if (!level.getBlockState(offset).canBeReplaced()) return false;
            }
        }
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()) {
            destroy(level, pos, state, true);
        } else {
            destroy(level, pos, state, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            for (var direction : Direction.values()) {
                BlockPos offset = pos.relative(direction);
                BlockState state = level.getBlockState(offset);
                if (state.getBlock().equals(this)) {
                    destroy(level, offset, state, true);
                    break;
                }
            }
        }
        super.wasExploded(level, pos, explosion);
    }

    private void destroy(Level level, BlockPos pos, BlockState state, boolean dropItems) {
        var direction = state.getValue(FACING).getClockWise();
        var controllerPos = getController(state, pos);

        // Only drop items from the controller block
        boolean isController = state.getValue(PART).isController();

        for (var part : SlidingDoorPartProperty.values()) {
            var partPos = controllerPos.relative(direction, part.xOffset()).above(part.yOffset());
            if (level.getBlockState(partPos).getBlock() == this) {
                level.destroyBlock(partPos, isController && dropItems && part.isController());
            }
        }
    }

    private BlockPos getController(BlockState state, BlockPos pos) {
        var part = state.getValue(PART);
        var direction = state.getValue(FACING).getClockWise();
        return pos.relative(direction, -part.xOffset()).below(part.yOffset());
    }
}