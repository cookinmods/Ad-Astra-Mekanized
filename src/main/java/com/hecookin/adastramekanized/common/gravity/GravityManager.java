package com.hecookin.adastramekanized.common.gravity;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages gravity-normalized zones in the world.
 * Tracks positions where gravity normalizers are active and their target gravity values.
 */
public class GravityManager {

    private static final GravityManager INSTANCE = new GravityManager();

    // Track gravity zones per dimension: position -> target gravity multiplier
    private final Map<ResourceLocation, Map<BlockPos, Float>> gravityZones = new ConcurrentHashMap<>();

    private GravityManager() {}

    public static GravityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Get the target gravity at a specific position.
     * @return The target gravity multiplier (1.0 = Earth gravity), or null if not in a gravity zone
     */
    public Float getTargetGravity(Level level, BlockPos pos) {
        if (level == null) return null;

        ResourceLocation dimId = level.dimension().location();
        Map<BlockPos, Float> zones = gravityZones.get(dimId);

        if (zones == null) return null;
        return zones.get(pos);
    }

    /**
     * Check if an entity is in a gravity-normalized zone.
     * Uses the entity's eye position for the check.
     */
    public Float getTargetGravity(Entity entity) {
        if (entity == null) return null;

        BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        return getTargetGravity(entity.level(), pos);
    }

    /**
     * Check if a position is in any gravity zone.
     */
    public boolean isInGravityZone(Level level, BlockPos pos) {
        return getTargetGravity(level, pos) != null;
    }

    /**
     * Set gravity for a single position.
     */
    public void setGravity(Level level, BlockPos pos, float targetGravity) {
        if (level.isClientSide()) return;

        ResourceLocation dimId = level.dimension().location();
        Map<BlockPos, Float> zones = gravityZones.computeIfAbsent(dimId, k -> new ConcurrentHashMap<>());

        zones.put(pos.immutable(), targetGravity);
    }

    /**
     * Set gravity for multiple positions at once (more efficient for normalizers).
     */
    public void setGravity(Level level, Set<BlockPos> positions, float targetGravity) {
        if (level.isClientSide() || positions == null || positions.isEmpty()) {
            return;
        }

        ResourceLocation dimId = level.dimension().location();
        Map<BlockPos, Float> zones = gravityZones.computeIfAbsent(dimId, k -> new ConcurrentHashMap<>());

        int sizeBefore = zones.size();
        for (BlockPos pos : positions) {
            zones.put(pos.immutable(), targetGravity);
        }

        AdAstraMekanized.LOGGER.debug("GravityManager: Set {} positions to gravity {} in dimension {}, total zones: {} -> {}",
            positions.size(), targetGravity, dimId, sizeBefore, zones.size());
    }

    /**
     * Remove gravity override for a single position.
     */
    public void removeGravity(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        ResourceLocation dimId = level.dimension().location();
        Map<BlockPos, Float> zones = gravityZones.get(dimId);

        if (zones != null) {
            zones.remove(pos);
        }
    }

    /**
     * Remove gravity overrides for multiple positions at once.
     */
    public void removeGravity(Level level, Set<BlockPos> positions) {
        if (level.isClientSide() || positions == null || positions.isEmpty()) {
            return;
        }

        ResourceLocation dimId = level.dimension().location();
        Map<BlockPos, Float> zones = gravityZones.get(dimId);

        if (zones != null) {
            int sizeBefore = zones.size();
            for (BlockPos pos : positions) {
                zones.remove(pos);
            }
            AdAstraMekanized.LOGGER.debug("GravityManager: Removed {} positions from dimension {}, total zones: {} -> {}",
                positions.size(), dimId, sizeBefore, zones.size());
        }
    }

    /**
     * Clear all gravity zones for a dimension.
     */
    public void clearDimensionZones(ResourceLocation dimensionId) {
        Map<BlockPos, Float> removed = gravityZones.remove(dimensionId);
        if (removed != null) {
            AdAstraMekanized.LOGGER.info("GravityManager: Cleared {} gravity zones for dimension {}",
                removed.size(), dimensionId);
        }
    }

    /**
     * Get total count of gravity-affected positions in a dimension.
     */
    public int getZoneCount(ResourceLocation dimensionId) {
        Map<BlockPos, Float> zones = gravityZones.get(dimensionId);
        return zones != null ? zones.size() : 0;
    }

    /**
     * Clear all cached data.
     */
    public void clearCache() {
        gravityZones.clear();
        AdAstraMekanized.LOGGER.info("Cleared gravity zone cache");
    }
}
