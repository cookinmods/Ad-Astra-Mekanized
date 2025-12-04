package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.GlobalOxygenManager;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.menus.OxygenDistributorMenu;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.ImprovedOxygenFloodFill;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.Direction;

/**
 * Improved oxygen distributor with dynamic expansion, ring-based claiming,
 * and proper boundary respect for other distributors.
 */
public class ImprovedOxygenDistributor extends BlockEntity implements MenuProvider {

    // Constants
    private static final int INITIAL_RADIUS = 3; // Start small
    private static final int EXPANSION_RATE = 10; // Expand radius by 1 every 10 ticks
    private static final int MAX_OXYGEN_BLOCKS = 4100; // Maximum blocks to oxygenate
    private static final float OXYGEN_PER_BLOCK = 0.01f; // Oxygen consumption per block (mB)
    private static final float ENERGY_PER_BLOCK = 0.10f; // Energy consumption per block (FE)

    private static final long OXYGEN_CAPACITY = 2000; // mB
    private static final int ENERGY_CAPACITY = 30000; // FE
    private static final int DISTRIBUTION_INTERVAL = 20; // Faster updates for better responsiveness

    // Components
    private final IChemicalTank oxygenTank;
    private final EnergyStorage energyStorage;
    private final ChemicalHandler chemicalHandler;

    // State
    private int tickCounter = 0;
    private int expansionTicks = 0; // Tracks ticks since activation for expansion
    private int currentRadius = INITIAL_RADIUS;
    private boolean isActive = false;
    private boolean manuallyDisabled = false;
    private boolean oxygenBlockVisibility = false;
    private int oxygenBlockColor = 8;  // Default to white (index 8 in color array)
    private long activationTime = 0;
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();
    private final int tickOffset;

    // Usage tracking for GUI display
    private float lastOxygenUsage = 0.0f; // mB per tick
    private float lastEnergyUsage = 0.0f; // FE per tick
    private int lastBlockCount = 0;

    // Animation
    public float yRot = 0.0f;
    private float lastYRot = 0.0f;

    public ImprovedOxygenDistributor(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), pos, blockState);

        // Calculate tick offset for staggered updates
        this.tickOffset = Math.abs((pos.getX() * 73 + pos.getY() * 179 + pos.getZ() * 283) % DISTRIBUTION_INTERVAL);

        // Initialize oxygen tank
        this.oxygenTank = BasicChemicalTank.inputModern(
            OXYGEN_CAPACITY,
            stack -> isOxygen(stack.getChemical()),
            this::setChanged
        );

        // Initialize energy storage (max extract = capacity for internal use)
        this.energyStorage = new EnergyStorage(ENERGY_CAPACITY, 1000, ENERGY_CAPACITY);

        // Initialize chemical handler
        this.chemicalHandler = new ChemicalHandler();
    }

    public void tick() {
        if (level == null) return;

        // Client-side animation
        if (level.isClientSide) {
            if (isActive) {
                lastYRot = yRot;
                yRot = (yRot + 3.6f) % 360.0f;
            }
            return;
        }

        // Server-side logic
        tickCounter++;

        // Debug logging
        if (tickCounter % 20 == 0) {
            AdAstraMekanized.LOGGER.debug("ImprovedOxygenDistributor at {}: Energy={}/{}, Oxygen={}/{}, Active={}, Radius={}, Blocks={}",
                worldPosition, energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored(),
                oxygenTank.getStored(), oxygenTank.getCapacity(), isActive, currentRadius, oxygenatedBlocks.size());
        }

        // Calculate minimum energy needed (at least 1 block worth)
        int minEnergyNeeded = (int) Math.ceil(ENERGY_PER_BLOCK);
        boolean hasResources = energyStorage.getEnergyStored() >= minEnergyNeeded && !oxygenTank.isEmpty();
        boolean wasActive = isActive;

        // Handle manual disable
        if (manuallyDisabled) {
            if (isActive) {
                deactivate();
            }
            return;
        }

        // Auto-activate when resources available
        if (!isActive && hasResources) {
            activate();
        }

        // Active operation
        if (isActive && hasResources) {
            // Dynamic radius expansion
            expansionTicks++;
            if (expansionTicks % EXPANSION_RATE == 0) {
                // No max radius limit, only max blocks
                currentRadius++;
                AdAstraMekanized.LOGGER.debug("Expanding radius to {} at {}", currentRadius, worldPosition);
            }

            // Distribute oxygen at intervals
            int adjustedInterval = DISTRIBUTION_INTERVAL + (tickOffset % DISTRIBUTION_INTERVAL);
            if (tickCounter >= adjustedInterval) {
                tickCounter = 0;
                distributeOxygen();
            }

            // Send visualization updates
            if (oxygenBlockVisibility && !oxygenatedBlocks.isEmpty() && tickCounter % 20 == 0) {
                sendVisualizationUpdate(true);
            }
        } else if (isActive && !hasResources) {
            // Enter standby
            deactivate();
        }

        // Sync state changes
        if (wasActive != isActive) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void activate() {
        isActive = true;
        activationTime = System.currentTimeMillis();
        expansionTicks = 0;
        currentRadius = INITIAL_RADIUS; // Reset to initial radius
        AdAstraMekanized.LOGGER.debug("Activating oxygen distributor at {} with initial radius {}", worldPosition, currentRadius);
        setChanged();
    }

    private void deactivate() {
        isActive = false;
        activationTime = 0;
        expansionTicks = 0;
        currentRadius = INITIAL_RADIUS; // Reset radius for next activation
        lastBlockCount = 0;
        lastOxygenUsage = 0;
        lastEnergyUsage = 0;
        clearOxygenatedBlocks();
        sendVisualizationRemoval();
        // Note: notifyNearbyDistributorsForUpdate() already called by clearOxygenatedBlocks()
        AdAstraMekanized.LOGGER.debug("Deactivating oxygen distributor at {}", worldPosition);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected void distributeOxygen() {
        var dimension = level.dimension();

        AdAstraMekanized.LOGGER.debug("Distributing oxygen from {} with radius {} (tick {})",
            worldPosition, currentRadius, level.getGameTime());

        // Use the improved flood fill algorithm
        Set<BlockPos> newOxygenatedBlocks = ImprovedOxygenFloodFill.findOxygenatableArea(
            level,
            worldPosition,
            currentRadius,
            MAX_OXYGEN_BLOCKS,
            level.getGameTime()
        );

        // Try to claim the blocks atomically
        if (!newOxygenatedBlocks.isEmpty() && oxygenTank.getStored() > 0) {
            Set<BlockPos> claimedBlocks = GlobalOxygenManager.getInstance().claimOxygenBlocks(
                dimension, worldPosition, newOxygenatedBlocks
            );

            if (!claimedBlocks.isEmpty()) {
                // Calculate resource consumption
                int blockCount = claimedBlocks.size();
                // Ensure minimum consumption of 1 mB to prevent staying active with small amounts
                long oxygenToConsume = Math.max(1, Math.round(blockCount * OXYGEN_PER_BLOCK));
                int energyToConsume = Math.max(1, (int) Math.ceil(blockCount * ENERGY_PER_BLOCK));

                // Check oxygen level and reduce blocks if below 10% capacity
                if (oxygenTank.getStored() < (oxygenTank.getCapacity() * 0.1)) {
                    // Below 10% capacity - start reducing oxygen blocks from outer edges
                    int targetBlockCount = Math.max(1, (int)(blockCount * oxygenTank.getStored() / (oxygenTank.getCapacity() / 10)));
                    if (targetBlockCount < blockCount) {
                        // Remove blocks starting from edges (blocks with fewer neighbors)
                        Set<BlockPos> toKeep = removeEdgeBlocks(claimedBlocks, targetBlockCount);
                        Set<BlockPos> toRelease = new HashSet<>(claimedBlocks);
                        toRelease.removeAll(toKeep);

                        GlobalOxygenManager.getInstance().releaseOxygenBlocks(dimension, worldPosition, toRelease);
                        claimedBlocks = toKeep;
                        blockCount = toKeep.size();

                        // Recalculate consumption for reduced blocks
                        oxygenToConsume = Math.max(1, Math.round(blockCount * OXYGEN_PER_BLOCK));
                        energyToConsume = Math.max(1, (int) Math.ceil(blockCount * ENERGY_PER_BLOCK));
                    }
                }

                // Check if we have enough resources
                if (oxygenTank.getStored() >= oxygenToConsume &&
                    energyStorage.getEnergyStored() >= energyToConsume) {

                    // Update oxygen zones
                    updateOxygenZones(claimedBlocks);

                    // Consume resources
                    long oxygenBefore = oxygenTank.getStored();
                    int energyBefore = energyStorage.getEnergyStored();

                    oxygenTank.shrinkStack(oxygenToConsume, Action.EXECUTE);
                    energyStorage.extractEnergy(energyToConsume, false);

                    long oxygenAfter = oxygenTank.getStored();
                    int energyAfter = energyStorage.getEnergyStored();
                    long actualOxygenConsumed = oxygenBefore - oxygenAfter;
                    int actualEnergyConsumed = energyBefore - energyAfter;

                    // Update usage tracking (per tick, so divide by distribution interval)
                    lastBlockCount = blockCount;
                    lastOxygenUsage = (float) actualOxygenConsumed / DISTRIBUTION_INTERVAL;
                    lastEnergyUsage = (float) actualEnergyConsumed / DISTRIBUTION_INTERVAL;

                    AdAstraMekanized.LOGGER.info("Distribution: {} blocks | Oxygen: requested={} mB, actual={} mB ({}mB/t) | Energy: requested={} FE, actual={} FE ({}FE/t)",
                        blockCount, oxygenToConsume, actualOxygenConsumed, lastOxygenUsage,
                        energyToConsume, actualEnergyConsumed, lastEnergyUsage);

                    if (oxygenBlockVisibility) {
                        sendVisualizationUpdate(true);
                    }
                } else {
                    // Not enough resources, release blocks
                    GlobalOxygenManager.getInstance().releaseOxygenBlocks(dimension, worldPosition, claimedBlocks);
                    lastBlockCount = 0;
                    lastOxygenUsage = 0;
                    lastEnergyUsage = 0;
                }
            } else {
                // No blocks claimed
                lastBlockCount = 0;
                lastOxygenUsage = 0;
                lastEnergyUsage = 0;
            }
        }
    }

    private void updateOxygenZones(Set<BlockPos> claimedBlocks) {
        // Release blocks we no longer claim
        Set<BlockPos> toRelease = new HashSet<>(oxygenatedBlocks);
        toRelease.removeAll(claimedBlocks);
        if (!toRelease.isEmpty()) {
            GlobalOxygenManager.getInstance().releaseOxygenBlocks(level.dimension(), worldPosition, toRelease);
            OxygenManager.getInstance().setOxygen(level, toRelease, false);
        }

        // Add new blocks
        Set<BlockPos> toAdd = new HashSet<>(claimedBlocks);
        toAdd.removeAll(oxygenatedBlocks);
        if (!toAdd.isEmpty()) {
            OxygenManager.getInstance().setOxygen(level, toAdd, true);
        }

        oxygenatedBlocks.clear();
        oxygenatedBlocks.addAll(claimedBlocks);
    }

    protected void clearOxygenatedBlocks() {
        if (!oxygenatedBlocks.isEmpty() && level != null) {
            AdAstraMekanized.LOGGER.debug("Clearing {} oxygen blocks at {}", oxygenatedBlocks.size(), worldPosition);
            GlobalOxygenManager.getInstance().releaseOxygenBlocks(level.dimension(), worldPosition, oxygenatedBlocks);
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, false);
            oxygenatedBlocks.clear();
            notifyNearbyDistributorsForUpdate();
        }
    }

    /**
     * Notify nearby distributors that blocks may be available.
     * Note: Distributors naturally re-expand every DISTRIBUTION_INTERVAL ticks,
     * so explicit notification is only needed for immediate response.
     * Removed expensive 274K position scan - distributors will reclaim blocks naturally.
     */
    protected void notifyNearbyDistributorsForUpdate() {
        // Intentionally empty - removed O(n^3) position scan that caused freezes.
        // Nearby distributors will reclaim released blocks during their next distribution cycle.
        // This happens every ~20 ticks anyway, so the delay is negligible.
    }

    private BlockPos getStartPosition(BlockPos distributorPos) {
        BlockState state = level.getBlockState(distributorPos);

        if (state.hasProperty(SidedMachineBlock.FACE)) {
            AttachFace face = state.getValue(SidedMachineBlock.FACE);
            return switch (face) {
                case FLOOR -> distributorPos.above();
                case CEILING -> distributorPos.below();
                case WALL -> {
                    if (state.hasProperty(SidedMachineBlock.FACING)) {
                        Direction facing = state.getValue(SidedMachineBlock.FACING);
                        yield distributorPos.relative(facing);
                    }
                    yield distributorPos.above();
                }
            };
        }

        return distributorPos.above();
    }

    private boolean isOxygen(Chemical chemical) {
        var registryName = chemical.getRegistryName();
        return registryName.getNamespace().equals("mekanism") && registryName.getPath().equals("oxygen");
    }

    // Public API methods

    public boolean isActive() {
        return isActive;
    }

    public boolean isManuallyDisabled() {
        return manuallyDisabled;
    }

    public void setManuallyDisabled(boolean disabled) {
        this.manuallyDisabled = disabled;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            if (disabled && isActive) {
                deactivate();
            }
        }
    }

    public void setOxygenBlockVisibility(boolean visible) {
        this.oxygenBlockVisibility = visible;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            sendVisualizationUpdate(visible && !oxygenatedBlocks.isEmpty());
        }
    }

    public boolean getOxygenBlockVisibility() {
        return this.oxygenBlockVisibility;
    }

    public void setOxygenBlockColor(int colorIndex) {
        this.oxygenBlockColor = colorIndex;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (oxygenBlockVisibility) {
                sendVisualizationUpdate(true);
            }
        }
    }

    public int getOxygenBlockColor() {
        return this.oxygenBlockColor;
    }

    public int getMachineState() {
        if (manuallyDisabled) return 0; // INACTIVE
        if (isActive) return 2; // ACTIVE
        return 1; // STANDBY
    }

    public int getMaxOxygenBlocks() {
        return MAX_OXYGEN_BLOCKS;
    }

    public int getCurrentRadius() {
        return currentRadius;
    }

    // Static getters for GUI to access constants dynamically
    public static int getMaxBlocks() {
        return MAX_OXYGEN_BLOCKS;
    }

    public static float getOxygenPerBlockConstant() {
        return OXYGEN_PER_BLOCK;
    }

    public static float getEnergyPerBlockConstant() {
        return ENERGY_PER_BLOCK;
    }

    public static int getDistributionInterval() {
        return DISTRIBUTION_INTERVAL;
    }

    public int getEnergyPerTick() {
        // Return the tracked energy usage rate
        return (int) Math.ceil(lastEnergyUsage);
    }

    public float getOxygenPerTick() {
        return isActive ? getOxygenatedBlockCount() * OXYGEN_PER_BLOCK : 0;
    }

    public float getEfficiency() {
        if (MAX_OXYGEN_BLOCKS == 0) return 0;
        return (float) getOxygenatedBlockCount() / MAX_OXYGEN_BLOCKS * 100f;
    }

    /**
     * Remove blocks from the outer edges first, keeping blocks closer to the center.
     * Prioritizes removing blocks with fewer neighbors.
     * @param blocks The set of blocks to reduce
     * @param targetCount The target number of blocks to keep
     * @return Set of blocks to keep (most connected/central blocks)
     */
    private Set<BlockPos> removeEdgeBlocks(Set<BlockPos> blocks, int targetCount) {
        if (blocks.size() <= targetCount) {
            return new HashSet<>(blocks);
        }

        // Calculate neighbor count for each block
        Map<BlockPos, Integer> neighborCounts = new HashMap<>();
        for (BlockPos pos : blocks) {
            int neighbors = 0;
            // Check all 6 adjacent blocks
            for (Direction dir : Direction.values()) {
                if (blocks.contains(pos.relative(dir))) {
                    neighbors++;
                }
            }
            neighborCounts.put(pos, neighbors);
        }

        // Sort blocks by distance from distributor (keep closer ones) and neighbor count (keep more connected ones)
        List<BlockPos> sortedBlocks = new ArrayList<>(blocks);
        sortedBlocks.sort((a, b) -> {
            // First priority: number of neighbors (more neighbors = more central/connected)
            int neighborDiff = neighborCounts.get(b) - neighborCounts.get(a);
            if (neighborDiff != 0) return neighborDiff;

            // Second priority: distance from distributor (closer is better)
            double distA = a.distSqr(worldPosition);
            double distB = b.distSqr(worldPosition);
            return Double.compare(distA, distB);
        });

        // Keep the most connected/central blocks
        Set<BlockPos> toKeep = new HashSet<>();
        for (int i = 0; i < targetCount && i < sortedBlocks.size(); i++) {
            toKeep.add(sortedBlocks.get(i));
        }

        return toKeep;
    }

    public int getOxygenatedBlockCount() {
        // Use tracked value for consistent display
        return lastBlockCount > 0 ? lastBlockCount : oxygenatedBlocks.size();
    }

    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }

    // Capability handling

    public <T> T getCapability(BlockCapability<T, Direction> cap, @Nullable Direction side) {
        if (cap == Capabilities.EnergyStorage.BLOCK) {
            return (T) energyStorage;
        }

        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);
            java.lang.reflect.Method blockMethod = chemicalCapObject.getClass().getMethod("block");
            Object blockCap = blockMethod.invoke(chemicalCapObject);

            if (cap.equals(blockCap)) {
                return (T) chemicalHandler;
            }
        } catch (Exception e) {
            // Mekanism not available
        }

        return null;
    }

    public IChemicalHandler getChemicalHandler() {
        return chemicalHandler;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IStrictEnergyHandler getStrictEnergyHandler() {
        return energyStorage;
    }

    // Getters for usage tracking
    public float getOxygenUsage() {
        return lastOxygenUsage;
    }

    public float getEnergyUsage() {
        return lastEnergyUsage;
    }

    // Note: getOxygenatedBlockCount() and getCurrentRadius() already exist above

    public IChemicalTank getOxygenTank() {
        return oxygenTank;
    }

    // Visualization packets

    protected void sendVisualizationUpdate(boolean visible) {
        if (level == null || level.isClientSide) return;

        Set<BlockPos> zones = new HashSet<>(oxygenatedBlocks);
        var packet = new com.hecookin.adastramekanized.common.network.OxygenVisualizationPacket(
            worldPosition, zones, visible, oxygenBlockColor);

        level.players().stream()
            .filter(player -> player instanceof net.minecraft.server.level.ServerPlayer)
            .map(player -> (net.minecraft.server.level.ServerPlayer) player)
            .filter(player -> player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64 * 64)
            .forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet));
    }

    protected void sendVisualizationRemoval() {
        if (level == null || level.isClientSide) return;

        var packet = new com.hecookin.adastramekanized.common.network.OxygenVisualizationPacket(
            worldPosition, new HashSet<>(), false, oxygenBlockColor);

        level.players().stream()
            .filter(player -> player instanceof net.minecraft.server.level.ServerPlayer)
            .map(player -> (net.minecraft.server.level.ServerPlayer) player)
            .filter(player -> player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64 * 64)
            .forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet));
    }

    // NBT serialization

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.energy);
        tag.put("oxygenTank", oxygenTank.serializeNBT(provider));
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("manuallyDisabled", manuallyDisabled);
        tag.putBoolean("oxygenBlockVisibility", oxygenBlockVisibility);
        tag.putInt("oxygenBlockColor", oxygenBlockColor);
        tag.putInt("currentRadius", currentRadius);
        tag.putInt("expansionTicks", expansionTicks);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.energy = tag.getInt("energy");
        oxygenTank.deserializeNBT(provider, tag.getCompound("oxygenTank"));
        isActive = tag.getBoolean("isActive");
        manuallyDisabled = tag.getBoolean("manuallyDisabled");
        oxygenBlockVisibility = tag.getBoolean("oxygenBlockVisibility");
        oxygenBlockColor = tag.getInt("oxygenBlockColor");
        currentRadius = tag.getInt("currentRadius");
        expansionTicks = tag.getInt("expansionTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("energy", energyStorage.energy);
        tag.putLong("oxygen", oxygenTank.getStored());
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("manuallyDisabled", manuallyDisabled);
        tag.putBoolean("oxygenBlockVisibility", oxygenBlockVisibility);
        tag.putInt("oxygenBlockColor", oxygenBlockColor);
        tag.putInt("currentRadius", currentRadius);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        energyStorage.energy = tag.getInt("energy");

        if (tag.getLong("oxygen") > 0) {
            try {
                java.lang.reflect.Field storedField = oxygenTank.getClass().getDeclaredField("stored");
                storedField.setAccessible(true);
                storedField.set(oxygenTank, tag.getLong("oxygen"));
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Could not set client-side oxygen amount: {}", e.getMessage());
            }
        }

        isActive = tag.getBoolean("isActive");
        manuallyDisabled = tag.getBoolean("manuallyDisabled");
        oxygenBlockVisibility = tag.getBoolean("oxygenBlockVisibility");
        oxygenBlockColor = tag.getInt("oxygenBlockColor");
        currentRadius = tag.getInt("currentRadius");
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        handleUpdateTag(pkt.getTag(), provider);
    }

    @Override
    public void setRemoved() {
        if (level != null && !oxygenatedBlocks.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("Distributor at {} being removed - releasing {} oxygen blocks",
                worldPosition, oxygenatedBlocks.size());

            GlobalOxygenManager.getInstance().releaseOxygenBlocks(level.dimension(), worldPosition, oxygenatedBlocks);
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, false);
            oxygenatedBlocks.clear();

            if (!level.isClientSide) {
                sendVisualizationRemoval();
            }
        }
        super.setRemoved();
    }

    // MenuProvider implementation

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.oxygen_distributor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OxygenDistributorMenu(containerId, playerInventory, this);
    }

    // Inner classes for handlers

    private class ChemicalHandler implements IChemicalHandler {
        @Override
        public int getChemicalTanks() {
            return 1;
        }

        @Override
        public @NotNull ChemicalStack getChemicalInTank(int tank) {
            return tank == 0 ? oxygenTank.getStack() : ChemicalStack.EMPTY;
        }

        @Override
        public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
            if (tank == 0) {
                oxygenTank.setStack(stack);
            }
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return tank == 0 ? oxygenTank.getCapacity() : 0;
        }

        @Override
        public boolean isValid(int tank, @NotNull ChemicalStack stack) {
            return tank == 0 && oxygenTank.isValid(stack);
        }

        @Override
        public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
            if (tank != 0 || !isValid(tank, stack)) {
                return stack;
            }
            ChemicalStack remainder = oxygenTank.insert(stack, action, AutomationType.EXTERNAL);
            if (action.execute() && remainder.getAmount() < stack.getAmount()) {
                AdAstraMekanized.LOGGER.debug("Inserted {} mB of oxygen, tank now has {} mB",
                    stack.getAmount() - remainder.getAmount(), oxygenTank.getStored());
            }
            return remainder;
        }

        @Override
        public @NotNull ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
            return ChemicalStack.EMPTY; // Don't allow extraction
        }
    }

    private class EnergyStorage implements IEnergyStorage, IStrictEnergyHandler {
        private int energy;
        private final int capacity;
        private final int maxReceive;
        private final int maxExtract;

        public EnergyStorage(int capacity, int maxReceive, int maxExtract) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.maxExtract = maxExtract;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = Math.min(toReceive, Math.min(maxReceive, capacity - energy));
            if (!simulate) {
                energy += received;
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = Math.min(toExtract, Math.min(maxExtract, energy));
            if (!simulate) {
                energy -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return maxExtract > 0;
        }

        @Override
        public boolean canReceive() {
            return maxReceive > 0;
        }

        // Mekanism IStrictEnergyHandler implementation
        @Override
        public int getEnergyContainerCount() {
            return 1;
        }

        @Override
        public long getEnergy(int container) {
            return container == 0 ? energy : 0;
        }

        @Override
        public void setEnergy(int container, long energy) {
            if (container == 0) {
                this.energy = (int) Math.min(energy, Integer.MAX_VALUE);
                setChanged();
            }
        }

        @Override
        public long getMaxEnergy(int container) {
            return container == 0 ? capacity : 0;
        }

        @Override
        public long getNeededEnergy(int container) {
            return container == 0 ? capacity - energy : 0;
        }

        @Override
        public long insertEnergy(int container, long amount, @NotNull Action action) {
            if (container != 0) return 0;
            int toReceive = (int) Math.min(amount, Math.min(maxReceive, capacity - energy));
            if (action.execute()) {
                energy += toReceive;
                setChanged();
            }
            return toReceive;
        }

        @Override
        public long extractEnergy(int container, long amount, @NotNull Action action) {
            if (container != 0) return 0;
            int toExtract = (int) Math.min(amount, Math.min(maxExtract, energy));
            if (action.execute()) {
                energy -= toExtract;
                setChanged();
            }
            return toExtract;
        }
    }
}