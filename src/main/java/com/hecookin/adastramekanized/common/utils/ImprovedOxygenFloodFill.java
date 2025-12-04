package com.hecookin.adastramekanized.common.utils;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.GlobalOxygenManager;
import com.hecookin.adastramekanized.common.gravity.GlobalGravityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Improved oxygen flood fill algorithm that respects block boundaries,
 * checks GlobalOxygenManager for ownership, and supports dynamic expansion.
 */
public class ImprovedOxygenFloodFill {

    private static final Direction[] DIRECTIONS = Direction.values();

    // Cache for pathfinding results - cleared every 100 ticks
    private static final Map<CacheKey, PathResult> pathCache = new ConcurrentHashMap<>();
    private static long lastCacheClear = 0;
    private static final long CACHE_DURATION = 100; // ticks

    /**
     * Ring-based expansion with dynamic radius and GlobalOxygenManager checking
     */
    public static Set<BlockPos> findOxygenatableArea(
            Level level,
            BlockPos distributorPos,
            int currentRadius,
            int maxBlocks,
            long currentTick) {

        // Clear cache periodically
        if (currentTick - lastCacheClear > CACHE_DURATION) {
            pathCache.clear();
            lastCacheClear = currentTick;
            AdAstraMekanized.LOGGER.debug("Cleared pathfinding cache after {} ticks", CACHE_DURATION);
        }

        Set<BlockPos> oxygenatedPositions = new LinkedHashSet<>();
        Set<BlockPos> visitedPositions = new HashSet<>();
        ResourceKey<Level> dimension = level.dimension();
        GlobalOxygenManager globalManager = GlobalOxygenManager.getInstance();

        AdAstraMekanized.LOGGER.debug("ImprovedFloodFill: Starting from distributor {} with radius {}, max blocks {}",
            distributorPos, currentRadius, maxBlocks);

        // PRIORITY PHASE: Immediately claim 3x3x3 cube around distributor
        // This ensures distributors have control over their immediate surroundings
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos cubePos = distributorPos.offset(dx, dy, dz);

                    // Skip the distributor itself
                    if (cubePos.equals(distributorPos)) {
                        continue;
                    }

                    // Try to claim this position with priority
                    if (canClaimPosition(level, cubePos, distributorPos, dimension, globalManager)) {
                        oxygenatedPositions.add(cubePos);
                        visitedPositions.add(cubePos);

                        if (oxygenatedPositions.size() >= maxBlocks) {
                            AdAstraMekanized.LOGGER.debug("Reached max blocks during priority cube claiming");
                            return oxygenatedPositions;
                        }
                    }
                }
            }
        }

        AdAstraMekanized.LOGGER.debug("Priority cube claimed {} blocks", oxygenatedPositions.size());

        // Priority queue for ring-based expansion (closer blocks first)
        PriorityQueue<PositionWithDistance> expansionQueue = new PriorityQueue<>(
            Comparator.comparingInt(pwd -> pwd.distance)
        );

        // Add all claimed priority cube positions as starting points for expansion
        for (BlockPos claimedPos : oxygenatedPositions) {
            expansionQueue.offer(new PositionWithDistance(claimedPos, 0));
        }

        // If we couldn't claim ANY blocks in the priority cube, we're completely blocked
        if (oxygenatedPositions.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("ImprovedFloodFill: Could not claim any blocks in priority cube - distributor is blocked");
            return oxygenatedPositions;
        }

        // Ring-based expansion
        while (!expansionQueue.isEmpty() && oxygenatedPositions.size() < maxBlocks) {
            PositionWithDistance current = expansionQueue.poll();

            // Skip if beyond current radius
            if (current.distance > currentRadius) {
                continue;
            }

            // Try to claim this position
            if (canClaimPosition(level, current.pos, distributorPos, dimension, globalManager)) {
                // Check if there's a valid path (use cache)
                // Check if there's a valid path from distributor to this position
                if (hasValidPath(level, distributorPos, current.pos, distributorPos, dimension, globalManager, currentRadius)) {
                    oxygenatedPositions.add(current.pos);

                    // Add adjacent positions to queue for next ring
                    for (Direction dir : DIRECTIONS) {
                        BlockPos adjacent = current.pos.relative(dir);
                        if (!visitedPositions.contains(adjacent)) {
                            visitedPositions.add(adjacent);
                            int newDistance = current.distance + 1;
                            if (newDistance <= currentRadius) {
                                expansionQueue.offer(new PositionWithDistance(adjacent, newDistance));
                            }
                        }
                    }
                }
            }
        }

        AdAstraMekanized.LOGGER.debug("ImprovedFloodFill: Found {} oxygenatable positions (radius={}, maxBlocks={})",
            oxygenatedPositions.size(), currentRadius, maxBlocks);

        return oxygenatedPositions;
    }

    /**
     * Check if a position can be claimed by this distributor
     */
    private static boolean canClaimPosition(
            Level level,
            BlockPos pos,
            BlockPos distributorPos,
            ResourceKey<Level> dimension,
            GlobalOxygenManager globalManager) {

        // Check if position needs oxygen
        if (!needsOxygen(level, pos)) {
            return false;
        }

        // Check ownership in GlobalOxygenManager
        BlockPos owner = globalManager.getBlockOwner(dimension, pos);

        // Can claim if:
        // 1. Not owned by anyone (owner == null)
        // 2. Already owned by us (owner.equals(distributorPos))
        return owner == null || owner.equals(distributorPos);
    }

    /**
     * Check if there's a valid path between positions (with caching)
     */
    private static boolean hasValidPath(
            Level level,
            BlockPos from,
            BlockPos to,
            BlockPos distributorPos,
            ResourceKey<Level> dimension,
            GlobalOxygenManager globalManager,
            int maxDistance) {

        if (from.equals(to)) {
            return true;
        }

        // Check cache first
        CacheKey key = new CacheKey(from, to, distributorPos);
        PathResult cached = pathCache.get(key);
        if (cached != null) {
            return cached.hasPath;
        }

        // Perform pathfinding
        boolean result = findPath(level, from, to, distributorPos, dimension, globalManager, maxDistance);

        // Cache result
        pathCache.put(key, new PathResult(result));

        return result;
    }

    /**
     * A* pathfinding that treats blocks owned by other distributors as walls
     */
    private static boolean findPath(
            Level level,
            BlockPos from,
            BlockPos to,
            BlockPos distributorPos,
            ResourceKey<Level> dimension,
            GlobalOxygenManager globalManager,
            int maxDistance) {

        if (from.distManhattan(to) > maxDistance) {
            return false;
        }

        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();

        openSet.add(new PathNode(from, 0, from.distManhattan(to)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.pos.equals(to)) {
                return true;
            }

            if (closedSet.contains(current.pos)) {
                continue;
            }

            closedSet.add(current.pos);

            if (current.cost > maxDistance) {
                continue;
            }

            for (Direction dir : DIRECTIONS) {
                BlockPos neighborPos = current.pos.relative(dir);

                if (closedSet.contains(neighborPos)) {
                    continue;
                }

                // Check if we can pass through this position
                if (!canOxygenPassThrough(level, current.pos, neighborPos, dir, distributorPos, dimension, globalManager)) {
                    continue;
                }

                int newCost = current.cost + 1;
                int heuristic = neighborPos.distManhattan(to);
                openSet.add(new PathNode(neighborPos, newCost, heuristic));
            }
        }

        return false;
    }

    /**
     * Check if oxygen can pass from one position to another
     * NOW CHECKS GlobalOxygenManager - blocks owned by others are treated as walls
     */
    private static boolean canOxygenPassThrough(
            Level level,
            BlockPos from,
            BlockPos to,
            Direction direction,
            BlockPos distributorPos,
            ResourceKey<Level> dimension,
            GlobalOxygenManager globalManager) {

        // Check if the destination is owned by another distributor
        BlockPos owner = globalManager.getBlockOwner(dimension, to);
        if (owner != null && !owner.equals(distributorPos)) {
            // Treat blocks owned by other distributors as solid walls
            return false;
        }

        BlockState fromState = level.getBlockState(from);
        BlockState toState = level.getBlockState(to);

        // Can always pass into air
        if (toState.isAir()) {
            return true;
        }

        // Check if the destination block blocks oxygen
        if (toState.isSolid() && toState.isCollisionShapeFullBlock(level, to)) {
            return false; // Solid blocks block oxygen flow
        }

        // Check collision shapes to see if there's an opening
        VoxelShape fromShape = fromState.getCollisionShape(level, from, CollisionContext.empty());
        VoxelShape toShape = toState.getCollisionShape(level, to, CollisionContext.empty());

        // Check if there's an opening between the blocks
        if (!fromShape.isEmpty() && !toShape.isEmpty()) {
            // Check face connectivity
            VoxelShape fromFace = fromShape.getFaceShape(direction);
            VoxelShape toFace = toShape.getFaceShape(direction.getOpposite());

            // If both faces are solid, oxygen can't pass
            if (!fromFace.isEmpty() && !toFace.isEmpty()) {
                return false;
            }
        }

        // Allow oxygen through partial blocks
        return true;
    }

    /**
     * Determines if a block position needs oxygen.
     * Solid full blocks don't need oxygen, but partial blocks and air do.
     */
    private static boolean needsOxygen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Air always needs oxygen
        if (state.isAir()) {
            return true;
        }

        // Water and lava don't get oxygenated
        if (state.getFluidState().isSource()) {
            return false;
        }

        // Stairs need oxygen (players can walk on them)
        if (state.getBlock() instanceof StairBlock) {
            return true;
        }

        // Slabs need oxygen unless they're double slabs
        if (state.getBlock() instanceof SlabBlock) {
            return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
        }

        // Doors need oxygen in their space
        if (state.getBlock() instanceof DoorBlock) {
            return true;
        }

        // Trapdoors need oxygen when open
        if (state.getBlock() instanceof TrapDoorBlock) {
            return state.getValue(TrapDoorBlock.OPEN);
        }

        // Pressure plates, buttons, etc. need oxygen
        if (!state.isCollisionShapeFullBlock(level, pos)) {
            // Check if players can pass through
            VoxelShape collisionShape = state.getCollisionShape(level, pos, CollisionContext.empty());

            // If collision shape is not full (less than 1 block tall), it needs oxygen
            if (collisionShape.isEmpty() || collisionShape.max(Direction.Axis.Y) < 1.0) {
                return true;
            }
        }

        // Full solid blocks don't need oxygen
        if (state.isSolid() && state.isCollisionShapeFullBlock(level, pos)) {
            return false;
        }

        // Default to needing oxygen for safety
        return true;
    }

    // Helper classes

    private static class PositionWithDistance {
        final BlockPos pos;
        final int distance;

        PositionWithDistance(BlockPos pos, int distance) {
            this.pos = pos;
            this.distance = distance;
        }
    }

    private static class PathNode implements Comparable<PathNode> {
        final BlockPos pos;
        final int cost;
        final int heuristic;

        PathNode(BlockPos pos, int cost, int heuristic) {
            this.pos = pos;
            this.cost = cost;
            this.heuristic = heuristic;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(
                this.cost + this.heuristic,
                other.cost + other.heuristic
            );
        }
    }

    private static class CacheKey {
        final BlockPos from;
        final BlockPos to;
        final BlockPos distributor;

        CacheKey(BlockPos from, BlockPos to, BlockPos distributor) {
            this.from = from.immutable();
            this.to = to.immutable();
            this.distributor = distributor.immutable();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return from.equals(cacheKey.from) &&
                   to.equals(cacheKey.to) &&
                   distributor.equals(cacheKey.distributor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, distributor);
        }
    }

    private static class PathResult {
        final boolean hasPath;

        PathResult(boolean hasPath) {
            this.hasPath = hasPath;
        }
    }

    // =====================================================================
    // GRAVITY-SPECIFIC METHODS
    // Uses GlobalGravityManager instead of GlobalOxygenManager
    // =====================================================================

    /**
     * Finds area for gravity normalization, using GlobalGravityManager for ownership.
     * Similar to oxygen flood fill but uses the gravity manager.
     */
    public static Set<BlockPos> findGravityNormalizableArea(
            Level level,
            BlockPos normalizerPos,
            int currentRadius,
            int maxBlocks,
            long currentTick) {

        Set<BlockPos> gravityPositions = new LinkedHashSet<>();
        Set<BlockPos> visitedPositions = new HashSet<>();
        ResourceKey<Level> dimension = level.dimension();
        GlobalGravityManager gravityManager = GlobalGravityManager.getInstance();

        AdAstraMekanized.LOGGER.debug("GravityFloodFill: Starting from normalizer {} with radius {}, max blocks {}",
            normalizerPos, currentRadius, maxBlocks);

        // PRIORITY PHASE: Immediately claim 3x3x3 cube around normalizer
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos cubePos = normalizerPos.offset(dx, dy, dz);

                    // Skip the normalizer itself
                    if (cubePos.equals(normalizerPos)) {
                        continue;
                    }

                    // Check if position needs gravity normalization
                    if (canClaimGravityPosition(level, cubePos, normalizerPos, dimension, gravityManager)) {
                        gravityPositions.add(cubePos);
                        visitedPositions.add(cubePos);

                        if (gravityPositions.size() >= maxBlocks) {
                            return gravityPositions;
                        }
                    }
                }
            }
        }

        AdAstraMekanized.LOGGER.debug("Gravity priority cube claimed {} blocks", gravityPositions.size());

        // Priority queue for ring-based expansion
        PriorityQueue<PositionWithDistance> expansionQueue = new PriorityQueue<>(
            Comparator.comparingInt(pwd -> pwd.distance)
        );

        // Add all claimed priority cube positions as starting points
        for (BlockPos claimedPos : gravityPositions) {
            expansionQueue.offer(new PositionWithDistance(claimedPos, 0));
        }

        // If we couldn't claim ANY blocks in the priority cube, we're blocked
        if (gravityPositions.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("GravityFloodFill: Could not claim any blocks in priority cube - normalizer is blocked");
            return gravityPositions;
        }

        // EXPANSION PHASE
        while (!expansionQueue.isEmpty() && gravityPositions.size() < maxBlocks) {
            PositionWithDistance current = expansionQueue.poll();

            if (current.distance > currentRadius) {
                continue;
            }

            // Check if we can claim this position
            if (canClaimGravityPosition(level, current.pos, normalizerPos, dimension, gravityManager)) {
                // Simplified path check - just check if gravity can pass through
                if (canGravityPassThrough(level, normalizerPos, current.pos, normalizerPos, dimension, gravityManager)) {
                    gravityPositions.add(current.pos);

                    // Add adjacent positions
                    for (Direction dir : DIRECTIONS) {
                        BlockPos adjacent = current.pos.relative(dir);
                        if (!visitedPositions.contains(adjacent)) {
                            visitedPositions.add(adjacent);
                            int newDistance = current.distance + 1;
                            if (newDistance <= currentRadius) {
                                expansionQueue.offer(new PositionWithDistance(adjacent, newDistance));
                            }
                        }
                    }
                }
            }
        }

        AdAstraMekanized.LOGGER.debug("GravityFloodFill: Total {} positions found", gravityPositions.size());
        return gravityPositions;
    }

    /**
     * Check if a position can be claimed for gravity normalization.
     * Uses GlobalGravityManager instead of GlobalOxygenManager.
     */
    private static boolean canClaimGravityPosition(
            Level level,
            BlockPos pos,
            BlockPos normalizerPos,
            ResourceKey<Level> dimension,
            GlobalGravityManager gravityManager) {

        // Check if position can be normalized (uses same logic as oxygen for air/passable blocks)
        if (!needsOxygen(level, pos)) {
            return false;
        }

        // Check ownership in GlobalGravityManager (NOT GlobalOxygenManager)
        BlockPos owner = gravityManager.getBlockOwner(dimension, pos);

        // Can claim if:
        // 1. Not owned by anyone (owner == null)
        // 2. Already owned by us (owner.equals(normalizerPos))
        return owner == null || owner.equals(normalizerPos);
    }

    /**
     * Simple check if gravity can pass between positions.
     * Gravity is more permissive than oxygen - it can pass through any non-full-solid block.
     */
    private static boolean canGravityPassThrough(
            Level level,
            BlockPos from,
            BlockPos to,
            BlockPos normalizerPos,
            ResourceKey<Level> dimension,
            GlobalGravityManager gravityManager) {

        // Check if the destination is owned by another normalizer
        BlockPos owner = gravityManager.getBlockOwner(dimension, to);
        if (owner != null && !owner.equals(normalizerPos)) {
            return false;
        }

        BlockState toState = level.getBlockState(to);

        // Can always pass into air
        if (toState.isAir()) {
            return true;
        }

        // Can pass through partial blocks
        if (!toState.isCollisionShapeFullBlock(level, to)) {
            return true;
        }

        // Can't pass through full solid blocks
        return false;
    }
}