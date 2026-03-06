package com.hecookin.adastramekanized.common.blockentities;

import com.hecookin.adastramekanized.common.blockentities.base.TickableBlockEntity;
import com.hecookin.adastramekanized.common.blocks.SlidingDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for sliding door animation and state management.
 * Handles the animation progress for smooth door sliding.
 */
public class SlidingDoorBlockEntity extends BlockEntity implements TickableBlockEntity {

    private int slideTicks;
    private int lastSlideTicks;

    public SlidingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes.SLIDING_DOOR.get(), pos, state);
    }

    @Override
    public void tick(Level level, long time, BlockState state, BlockPos pos) {
        boolean isOpen = getBlockState().getValue(SlidingDoorBlock.OPEN) || getBlockState().getValue(SlidingDoorBlock.POWERED);
        lastSlideTicks = slideTicks;

        if (!level.isClientSide()) {
            if (!isOpen && slideTicks == 97) {
                level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.25f, 1);
            } else if (isOpen && slideTicks == 3) {
                level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.25f, 1);
            }
        }
        slideTicks = Mth.clamp(slideTicks + (isOpen ? 3 : -3), 0, 100);
    }

    public int slideTicks() {
        return slideTicks;
    }

    public int lastSlideTicks() {
        return lastSlideTicks;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        slideTicks = tag.getInt("SlideTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SlideTicks", slideTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(3);
    }
}