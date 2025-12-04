package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.gravity.GlobalGravityManager;
import com.hecookin.adastramekanized.common.gravity.GravityManager;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.menus.GravityNormalizerMenu;
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
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Gravity Normalizer - A machine that creates a zone where gravity is normalized
 * to a configurable target value (default: Earth gravity 1.0).
 *
 * Consumes Argon gas and energy to maintain the gravity field.
 * Based on the oxygen distributor pattern but for gravity control.
 */
public class GravityNormalizerBlockEntity extends BlockEntity implements MenuProvider {

    // Constants - matched to oxygen distributor values
    private static final int INITIAL_RADIUS = 3;
    private static final int EXPANSION_RATE = 10; // Expand radius by 1 every 10 ticks
    private static final int MAX_GRAVITY_BLOCKS = 4100; // Same as oxygen distributor
    private static final float ARGON_PER_BLOCK = 0.005f; // Half of oxygen per block
    private static final float ENERGY_PER_BLOCK = 0.10f; // Same as oxygen distributor

    private static final long ARGON_CAPACITY = 2000; // mB (same as oxygen)
    private static final int ENERGY_CAPACITY = 30000; // FE (same as oxygen)
    private static final int DISTRIBUTION_INTERVAL = 20;

    // Gravity range
    public static final float MIN_GRAVITY = 0.0f;
    public static final float MAX_GRAVITY = 4.0f;
    public static final float DEFAULT_GRAVITY = 1.0f;

    // Components
    private final IChemicalTank argonTank;
    private final EnergyStorage energyStorage;
    private final ChemicalHandler chemicalHandler;

    // State
    private int tickCounter = 0;
    private int expansionTicks = 0;
    private int currentRadius = INITIAL_RADIUS;
    private boolean isActive = false;
    private boolean manuallyDisabled = false;
    private boolean zoneVisibility = false;
    private int zoneColor = 5; // Default to purple (index 5 in color array)
    private long activationTime = 0;
    private float targetGravity = DEFAULT_GRAVITY; // Configurable target gravity
    private final Set<BlockPos> normalizedBlocks = new HashSet<>();
    private final int tickOffset;

    // Usage tracking for GUI display
    private float lastArgonUsage = 0.0f;
    private float lastEnergyUsage = 0.0f;
    private int lastBlockCount = 0;

    // Animation
    public float yRot = 0.0f;
    private float lastYRot = 0.0f;

    public GravityNormalizerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.GRAVITY_NORMALIZER.get(), pos, blockState);

        // Calculate tick offset for staggered updates
        this.tickOffset = Math.abs((pos.getX() * 73 + pos.getY() * 179 + pos.getZ() * 283) % DISTRIBUTION_INTERVAL);

        // Initialize argon tank - accepts argon gas
        this.argonTank = BasicChemicalTank.inputModern(
            ARGON_CAPACITY,
            stack -> isArgon(stack.getChemical()),
            this::setChanged
        );

        // Initialize energy storage
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
            AdAstraMekanized.LOGGER.debug("GravityNormalizer at {}: Energy={}/{}, Argon={}/{}, Active={}, Radius={}, Blocks={}, TargetGravity={}",
                worldPosition, energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored(),
                argonTank.getStored(), argonTank.getCapacity(), isActive, currentRadius, normalizedBlocks.size(), targetGravity);
        }

        // Calculate minimum energy needed
        int minEnergyNeeded = (int) Math.ceil(ENERGY_PER_BLOCK);
        boolean hasResources = energyStorage.getEnergyStored() >= minEnergyNeeded && !argonTank.isEmpty();
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
                currentRadius++;
                AdAstraMekanized.LOGGER.debug("Gravity normalizer expanding radius to {} at {}", currentRadius, worldPosition);
            }

            // Distribute gravity at intervals
            int adjustedInterval = DISTRIBUTION_INTERVAL + (tickOffset % DISTRIBUTION_INTERVAL);
            if (tickCounter >= adjustedInterval) {
                tickCounter = 0;
                distributeGravity();
            }

            // Send visualization updates
            if (zoneVisibility && !normalizedBlocks.isEmpty() && tickCounter % 20 == 0) {
                sendVisualizationUpdate(true);
            }
        } else if (isActive && !hasResources) {
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
        currentRadius = INITIAL_RADIUS;
        AdAstraMekanized.LOGGER.debug("Activating gravity normalizer at {} with initial radius {}, target gravity {}",
            worldPosition, currentRadius, targetGravity);
        setChanged();
    }

    private void deactivate() {
        isActive = false;
        activationTime = 0;
        expansionTicks = 0;
        currentRadius = INITIAL_RADIUS;
        lastBlockCount = 0;
        lastArgonUsage = 0;
        lastEnergyUsage = 0;
        clearNormalizedBlocks();
        sendVisualizationRemoval();
        // Note: notifyNearbyNormalizersForUpdate() already called by clearNormalizedBlocks()
        AdAstraMekanized.LOGGER.debug("Deactivating gravity normalizer at {}", worldPosition);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected void distributeGravity() {
        var dimension = level.dimension();

        AdAstraMekanized.LOGGER.debug("Distributing gravity field from {} with radius {} (tick {})",
            worldPosition, currentRadius, level.getGameTime());

        // Use gravity-specific flood fill that uses GlobalGravityManager
        Set<BlockPos> newNormalizedBlocks = ImprovedOxygenFloodFill.findGravityNormalizableArea(
            level,
            worldPosition,
            currentRadius,
            MAX_GRAVITY_BLOCKS,
            level.getGameTime()
        );

        // Try to claim the blocks atomically
        if (!newNormalizedBlocks.isEmpty() && argonTank.getStored() > 0) {
            Set<BlockPos> claimedBlocks = GlobalGravityManager.getInstance().claimGravityBlocks(
                dimension, worldPosition, newNormalizedBlocks
            );

            if (!claimedBlocks.isEmpty()) {
                // Calculate resource consumption
                int blockCount = claimedBlocks.size();
                long argonToConsume = Math.max(1, Math.round(blockCount * ARGON_PER_BLOCK));
                int energyToConsume = Math.max(1, (int) Math.ceil(blockCount * ENERGY_PER_BLOCK));

                // Check argon level and reduce blocks if below 10% capacity
                if (argonTank.getStored() < (argonTank.getCapacity() * 0.1)) {
                    int targetBlockCount = Math.max(1, (int)(blockCount * argonTank.getStored() / (argonTank.getCapacity() / 10)));
                    if (targetBlockCount < blockCount) {
                        Set<BlockPos> toKeep = removeEdgeBlocks(claimedBlocks, targetBlockCount);
                        Set<BlockPos> toRelease = new HashSet<>(claimedBlocks);
                        toRelease.removeAll(toKeep);

                        GlobalGravityManager.getInstance().releaseGravityBlocks(dimension, worldPosition, toRelease);
                        claimedBlocks = toKeep;
                        blockCount = toKeep.size();

                        argonToConsume = Math.max(1, Math.round(blockCount * ARGON_PER_BLOCK));
                        energyToConsume = Math.max(1, (int) Math.ceil(blockCount * ENERGY_PER_BLOCK));
                    }
                }

                // Check if we have enough resources
                if (argonTank.getStored() >= argonToConsume &&
                    energyStorage.getEnergyStored() >= energyToConsume) {

                    // Update gravity zones
                    updateGravityZones(claimedBlocks);

                    // Consume resources
                    long argonBefore = argonTank.getStored();
                    int energyBefore = energyStorage.getEnergyStored();

                    argonTank.shrinkStack(argonToConsume, Action.EXECUTE);
                    energyStorage.extractEnergy(energyToConsume, false);

                    long argonAfter = argonTank.getStored();
                    int energyAfter = energyStorage.getEnergyStored();
                    long actualArgonConsumed = argonBefore - argonAfter;
                    int actualEnergyConsumed = energyBefore - energyAfter;

                    // Update usage tracking
                    lastBlockCount = blockCount;
                    lastArgonUsage = (float) actualArgonConsumed / DISTRIBUTION_INTERVAL;
                    lastEnergyUsage = (float) actualEnergyConsumed / DISTRIBUTION_INTERVAL;

                    AdAstraMekanized.LOGGER.info("Gravity distribution: {} blocks | Argon: requested={} mB, actual={} mB ({}mB/t) | Energy: requested={} FE, actual={} FE ({}FE/t)",
                        blockCount, argonToConsume, actualArgonConsumed, lastArgonUsage,
                        energyToConsume, actualEnergyConsumed, lastEnergyUsage);

                    if (zoneVisibility) {
                        sendVisualizationUpdate(true);
                    }
                } else {
                    GlobalGravityManager.getInstance().releaseGravityBlocks(dimension, worldPosition, claimedBlocks);
                    lastBlockCount = 0;
                    lastArgonUsage = 0;
                    lastEnergyUsage = 0;
                }
            } else {
                lastBlockCount = 0;
                lastArgonUsage = 0;
                lastEnergyUsage = 0;
            }
        }
    }

    private void updateGravityZones(Set<BlockPos> claimedBlocks) {
        // Release blocks we no longer claim
        Set<BlockPos> toRelease = new HashSet<>(normalizedBlocks);
        toRelease.removeAll(claimedBlocks);
        if (!toRelease.isEmpty()) {
            GlobalGravityManager.getInstance().releaseGravityBlocks(level.dimension(), worldPosition, toRelease);
            GravityManager.getInstance().removeGravity(level, toRelease);
        }

        // Add/update new blocks with our target gravity
        Set<BlockPos> toAdd = new HashSet<>(claimedBlocks);
        toAdd.removeAll(normalizedBlocks);
        if (!toAdd.isEmpty()) {
            GravityManager.getInstance().setGravity(level, toAdd, targetGravity);
        }

        // Also update existing blocks in case target gravity changed
        Set<BlockPos> existing = new HashSet<>(claimedBlocks);
        existing.retainAll(normalizedBlocks);
        if (!existing.isEmpty()) {
            GravityManager.getInstance().setGravity(level, existing, targetGravity);
        }

        normalizedBlocks.clear();
        normalizedBlocks.addAll(claimedBlocks);
    }

    protected void clearNormalizedBlocks() {
        if (!normalizedBlocks.isEmpty() && level != null) {
            AdAstraMekanized.LOGGER.debug("Clearing {} gravity blocks at {}", normalizedBlocks.size(), worldPosition);
            GlobalGravityManager.getInstance().releaseGravityBlocks(level.dimension(), worldPosition, normalizedBlocks);
            GravityManager.getInstance().removeGravity(level, normalizedBlocks);
            normalizedBlocks.clear();
            notifyNearbyNormalizersForUpdate();
        }
    }

    /**
     * Notify nearby normalizers that blocks may be available.
     * Note: Normalizers naturally re-expand every DISTRIBUTION_INTERVAL ticks (20 ticks),
     * so explicit notification is only needed for immediate response.
     * Removed expensive 274K position scan - normalizers will reclaim blocks naturally.
     */
    protected void notifyNearbyNormalizersForUpdate() {
        // Intentionally empty - removed O(n^3) position scan that caused freezes.
        // Nearby normalizers will reclaim released blocks during their next distribution cycle.
        // This happens every 20 ticks anyway, so the delay is negligible.
    }

    private boolean isArgon(Chemical chemical) {
        var registryName = chemical.getRegistryName();
        // Accept argon from various sources (Mekanism, ChemLib, etc.)
        String path = registryName.getPath().toLowerCase();
        return path.equals("argon") || path.contains("argon");
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

    public float getTargetGravity() {
        return targetGravity;
    }

    public void setTargetGravity(float gravity) {
        this.targetGravity = Math.max(MIN_GRAVITY, Math.min(MAX_GRAVITY, gravity));
        setChanged();

        // Update existing zones with new gravity value
        if (level != null && !level.isClientSide && !normalizedBlocks.isEmpty()) {
            GravityManager.getInstance().setGravity(level, normalizedBlocks, targetGravity);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setZoneVisibility(boolean visible) {
        this.zoneVisibility = visible;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            sendVisualizationUpdate(visible && !normalizedBlocks.isEmpty());
        }
    }

    public boolean getZoneVisibility() {
        return this.zoneVisibility;
    }

    public void setZoneColor(int colorIndex) {
        this.zoneColor = colorIndex;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (zoneVisibility) {
                sendVisualizationUpdate(true);
            }
        }
    }

    public int getZoneColor() {
        return this.zoneColor;
    }

    public int getMachineState() {
        if (manuallyDisabled) return 0; // INACTIVE
        if (isActive) return 2; // ACTIVE
        return 1; // STANDBY
    }

    public int getMaxGravityBlocks() {
        return MAX_GRAVITY_BLOCKS;
    }

    public int getCurrentRadius() {
        return currentRadius;
    }

    public static int getMaxBlocks() {
        return MAX_GRAVITY_BLOCKS;
    }

    public static float getArgonPerBlockConstant() {
        return ARGON_PER_BLOCK;
    }

    public static float getEnergyPerBlockConstant() {
        return ENERGY_PER_BLOCK;
    }

    public static int getDistributionInterval() {
        return DISTRIBUTION_INTERVAL;
    }

    public int getEnergyPerTick() {
        return (int) Math.ceil(lastEnergyUsage);
    }

    public float getArgonPerTick() {
        return isActive ? getNormalizedBlockCount() * ARGON_PER_BLOCK : 0;
    }

    public float getEfficiency() {
        if (MAX_GRAVITY_BLOCKS == 0) return 0;
        return (float) getNormalizedBlockCount() / MAX_GRAVITY_BLOCKS * 100f;
    }

    private Set<BlockPos> removeEdgeBlocks(Set<BlockPos> blocks, int targetCount) {
        if (blocks.size() <= targetCount) {
            return new HashSet<>(blocks);
        }

        Map<BlockPos, Integer> neighborCounts = new HashMap<>();
        for (BlockPos pos : blocks) {
            int neighbors = 0;
            for (Direction dir : Direction.values()) {
                if (blocks.contains(pos.relative(dir))) {
                    neighbors++;
                }
            }
            neighborCounts.put(pos, neighbors);
        }

        List<BlockPos> sortedBlocks = new ArrayList<>(blocks);
        sortedBlocks.sort((a, b) -> {
            int neighborDiff = neighborCounts.get(b) - neighborCounts.get(a);
            if (neighborDiff != 0) return neighborDiff;

            double distA = a.distSqr(worldPosition);
            double distB = b.distSqr(worldPosition);
            return Double.compare(distA, distB);
        });

        Set<BlockPos> toKeep = new HashSet<>();
        for (int i = 0; i < targetCount && i < sortedBlocks.size(); i++) {
            toKeep.add(sortedBlocks.get(i));
        }

        return toKeep;
    }

    public int getNormalizedBlockCount() {
        return lastBlockCount > 0 ? lastBlockCount : normalizedBlocks.size();
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
    public float getArgonUsage() {
        return lastArgonUsage;
    }

    public float getEnergyUsage() {
        return lastEnergyUsage;
    }

    public IChemicalTank getArgonTank() {
        return argonTank;
    }

    // Visualization packets

    protected void sendVisualizationUpdate(boolean visible) {
        if (level == null || level.isClientSide) return;

        Set<BlockPos> zones = new HashSet<>(normalizedBlocks);
        var packet = new com.hecookin.adastramekanized.common.network.GravityVisualizationPacket(
            worldPosition, zones, visible, zoneColor, targetGravity);

        level.players().stream()
            .filter(player -> player instanceof net.minecraft.server.level.ServerPlayer)
            .map(player -> (net.minecraft.server.level.ServerPlayer) player)
            .filter(player -> player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64 * 64)
            .forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet));
    }

    protected void sendVisualizationRemoval() {
        if (level == null || level.isClientSide) return;

        var packet = new com.hecookin.adastramekanized.common.network.GravityVisualizationPacket(
            worldPosition, new HashSet<>(), false, zoneColor, targetGravity);

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
        tag.put("argonTank", argonTank.serializeNBT(provider));
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("manuallyDisabled", manuallyDisabled);
        tag.putBoolean("zoneVisibility", zoneVisibility);
        tag.putInt("zoneColor", zoneColor);
        tag.putInt("currentRadius", currentRadius);
        tag.putInt("expansionTicks", expansionTicks);
        tag.putFloat("targetGravity", targetGravity);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.energy = tag.getInt("energy");
        argonTank.deserializeNBT(provider, tag.getCompound("argonTank"));
        isActive = tag.getBoolean("isActive");
        manuallyDisabled = tag.getBoolean("manuallyDisabled");
        zoneVisibility = tag.getBoolean("zoneVisibility");
        zoneColor = tag.getInt("zoneColor");
        currentRadius = tag.getInt("currentRadius");
        expansionTicks = tag.getInt("expansionTicks");
        targetGravity = tag.contains("targetGravity") ? tag.getFloat("targetGravity") : DEFAULT_GRAVITY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("energy", energyStorage.energy);
        tag.putLong("argon", argonTank.getStored());
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("manuallyDisabled", manuallyDisabled);
        tag.putBoolean("zoneVisibility", zoneVisibility);
        tag.putInt("zoneColor", zoneColor);
        tag.putInt("currentRadius", currentRadius);
        tag.putFloat("targetGravity", targetGravity);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        energyStorage.energy = tag.getInt("energy");

        if (tag.getLong("argon") > 0) {
            try {
                java.lang.reflect.Field storedField = argonTank.getClass().getDeclaredField("stored");
                storedField.setAccessible(true);
                storedField.set(argonTank, tag.getLong("argon"));
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Could not set client-side argon amount: {}", e.getMessage());
            }
        }

        isActive = tag.getBoolean("isActive");
        manuallyDisabled = tag.getBoolean("manuallyDisabled");
        zoneVisibility = tag.getBoolean("zoneVisibility");
        zoneColor = tag.getInt("zoneColor");
        currentRadius = tag.getInt("currentRadius");
        targetGravity = tag.contains("targetGravity") ? tag.getFloat("targetGravity") : DEFAULT_GRAVITY;
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
        if (level != null && !normalizedBlocks.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("Gravity normalizer at {} being removed - releasing {} blocks",
                worldPosition, normalizedBlocks.size());

            GlobalGravityManager.getInstance().releaseGravityBlocks(level.dimension(), worldPosition, normalizedBlocks);
            GravityManager.getInstance().removeGravity(level, normalizedBlocks);
            normalizedBlocks.clear();

            if (!level.isClientSide) {
                sendVisualizationRemoval();
            }
        }
        super.setRemoved();
    }

    // MenuProvider implementation

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.gravity_normalizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GravityNormalizerMenu(containerId, playerInventory, this);
    }

    // Inner classes for handlers

    private class ChemicalHandler implements IChemicalHandler {
        @Override
        public int getChemicalTanks() {
            return 1;
        }

        @Override
        public @NotNull ChemicalStack getChemicalInTank(int tank) {
            return tank == 0 ? argonTank.getStack() : ChemicalStack.EMPTY;
        }

        @Override
        public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
            if (tank == 0) {
                argonTank.setStack(stack);
            }
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return tank == 0 ? argonTank.getCapacity() : 0;
        }

        @Override
        public boolean isValid(int tank, @NotNull ChemicalStack stack) {
            return tank == 0 && argonTank.isValid(stack);
        }

        @Override
        public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
            if (tank != 0 || !isValid(tank, stack)) {
                return stack;
            }
            ChemicalStack remainder = argonTank.insert(stack, action, AutomationType.EXTERNAL);
            if (action.execute() && remainder.getAmount() < stack.getAmount()) {
                AdAstraMekanized.LOGGER.debug("Inserted {} mB of argon, tank now has {} mB",
                    stack.getAmount() - remainder.getAmount(), argonTank.getStored());
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
