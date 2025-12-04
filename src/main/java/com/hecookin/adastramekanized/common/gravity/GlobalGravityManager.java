package com.hecookin.adastramekanized.common.gravity;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager for gravity-normalized blocks to prevent overlap between normalizers
 * and allow them to work together efficiently.
 *
 * Modeled after GlobalOxygenManager but tracks gravity zones instead.
 */
public class GlobalGravityManager {
    private static final GlobalGravityManager INSTANCE = new GlobalGravityManager();

    // Track per-dimension to prevent cross-dimensional conflicts
    // Dimension -> BlockPos -> NormalizerPos
    private final Map<ResourceKey<Level>, Map<BlockPos, BlockPos>> dimensionBlockOwnership = new ConcurrentHashMap<>();
    // Dimension -> Set of occupied blocks
    private final Map<ResourceKey<Level>, Set<BlockPos>> dimensionOccupiedBlocks = new ConcurrentHashMap<>();

    private GlobalGravityManager() {}

    public static GlobalGravityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Check if a block position is available for gravity normalization in a specific dimension
     */
    public boolean isBlockAvailable(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied == null || !occupied.contains(pos);
    }

    /**
     * Try to claim gravity blocks for a normalizer in a specific dimension
     * Returns the set of blocks that were successfully claimed
     * Uses atomic batch claiming to prevent race conditions
     */
    public synchronized Set<BlockPos> claimGravityBlocks(ResourceKey<Level> dimension, BlockPos normalizerPos, Set<BlockPos> requestedBlocks) {
        AdAstraMekanized.LOGGER.debug("GRAVITY CLAIM REQUEST: Normalizer at {} requesting {} blocks",
            normalizerPos, requestedBlocks.size());

        Set<BlockPos> claimedBlocks = ConcurrentHashMap.newKeySet();

        // Get or create dimension-specific maps
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());

        // First pass: check which blocks we can claim
        Set<BlockPos> availableBlocks = new HashSet<>();
        int alreadyOwned = 0;
        int ownedByOthers = 0;

        for (BlockPos pos : requestedBlocks) {
            BlockPos currentOwner = blockOwnership.get(pos);
            if (currentOwner == null) {
                // Block is unclaimed
                availableBlocks.add(pos);
            } else if (currentOwner.equals(normalizerPos)) {
                // We already own this block
                claimedBlocks.add(pos);
                alreadyOwned++;
            } else {
                // Another normalizer owns it - skip
                ownedByOthers++;
                AdAstraMekanized.LOGGER.debug("  Gravity block {} already owned by {}", pos, currentOwner);
            }
        }

        // Second pass: claim all available blocks atomically
        int newlyClaimed = 0;
        for (BlockPos pos : availableBlocks) {
            // Double-check in case of concurrent modification
            if (!occupiedBlocks.contains(pos)) {
                occupiedBlocks.add(pos);
                blockOwnership.put(pos, normalizerPos);
                claimedBlocks.add(pos);
                newlyClaimed++;
            }
        }

        AdAstraMekanized.LOGGER.debug("GRAVITY CLAIM COMPLETE: Normalizer {} claimed {}/{} blocks (new={}, alreadyOwned={}, blocked={})",
            normalizerPos, claimedBlocks.size(), requestedBlocks.size(), newlyClaimed, alreadyOwned, ownedByOthers);

        return claimedBlocks;
    }

    /**
     * Release gravity blocks owned by a normalizer in a specific dimension
     */
    public synchronized void releaseGravityBlocks(ResourceKey<Level> dimension, BlockPos normalizerPos, Set<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("releaseGravityBlocks called with EMPTY blocks for normalizer at {}", normalizerPos);
            return;
        }

        AdAstraMekanized.LOGGER.debug("GRAVITY RELEASE REQUEST: Normalizer at {} requesting release of {} blocks",
            normalizerPos, blocks.size());

        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.get(dimension);
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.get(dimension);

        if (blockOwnership == null || occupiedBlocks == null) {
            AdAstraMekanized.LOGGER.warn("GRAVITY RELEASE FAILED: No ownership maps for dimension {}", dimension.location());
            return;
        }

        int released = 0;
        int notOwned = 0;
        int ownedByOthers = 0;

        for (BlockPos pos : blocks) {
            BlockPos owner = blockOwnership.get(pos);
            if (owner == null) {
                notOwned++;
                AdAstraMekanized.LOGGER.debug("  Gravity block {} was not owned by anyone", pos);
            } else if (normalizerPos.equals(owner)) {
                blockOwnership.remove(pos);
                occupiedBlocks.remove(pos);
                released++;
                AdAstraMekanized.LOGGER.debug("  Released gravity block {} from normalizer {}", pos, normalizerPos);
            } else {
                ownedByOthers++;
                AdAstraMekanized.LOGGER.debug("  Gravity block {} owned by different normalizer: {}", pos, owner);
            }
        }

        AdAstraMekanized.LOGGER.debug("GRAVITY RELEASE COMPLETE: Normalizer {} released {}/{} blocks (notOwned={}, ownedByOthers={})",
            normalizerPos, released, blocks.size(), notOwned, ownedByOthers);

        // Log current state
        AdAstraMekanized.LOGGER.debug("Remaining gravity-occupied blocks in dimension: {}", occupiedBlocks.size());
    }

    /**
     * Clear all gravity blocks for a specific dimension
     */
    public void clearDimension(ResourceKey<Level> dimension) {
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.remove(dimension);
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.remove(dimension);

        if (blockOwnership != null) {
            blockOwnership.clear();
        }
        if (occupiedBlocks != null) {
            occupiedBlocks.clear();
        }
    }

    /**
     * Get total number of gravity-normalized blocks in a dimension
     */
    public int getTotalGravityBlocks(ResourceKey<Level> dimension) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied != null ? occupied.size() : 0;
    }

    /**
     * Check if any normalizer has claimed a specific block in a dimension
     */
    public boolean isBlockOccupied(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied != null && occupied.contains(pos);
    }

    /**
     * Get the owner of a specific block in a dimension
     * @return The normalizer position that owns this block, or null if unclaimed
     */
    public BlockPos getBlockOwner(ResourceKey<Level> dimension, BlockPos pos) {
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.get(dimension);
        return blockOwnership != null ? blockOwnership.get(pos) : null;
    }
}
