package com.hecookin.adastramekanized.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Places bowl-shaped impact craters on the terrain surface.
 * Craters consist of three zones:
 * - Floor zone (0-70% of radius): flat bottom at full depth
 * - Wall zone (70-100%): sloping wall from floor to surface
 * - Rim zone (100-130%): raised rim above the original surface
 */
public class CraterFeature extends Feature<CraterConfiguration> {

    public CraterFeature(Codec<CraterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CraterConfiguration> context) {
        CraterConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int radius = config.radius().sample(random);
        int depth = config.depth().sample(random);
        int rimHeight = config.rimHeight().sample(random);
        BlockState floorBlock = config.floorBlock();
        BlockState rimBlock = config.rimBlock();

        // Get surface height at center
        int centerY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());

        // Don't place craters too close to world floor
        if (centerY - depth < level.getMinBuildHeight() + 5) {
            return false;
        }

        float radiusSq = radius * radius;
        float rimOuterRadius = radius * 1.3f;
        float rimOuterRadiusSq = rimOuterRadius * rimOuterRadius;
        float floorRadius = radius * 0.7f;
        float floorRadiusSq = floorRadius * floorRadius;

        boolean placed = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int outerBound = (int) Math.ceil(rimOuterRadius);

        for (int dx = -outerBound; dx <= outerBound; dx++) {
            for (int dz = -outerBound; dz <= outerBound; dz++) {
                float distSq = dx * dx + dz * dz;

                // Outside all zones
                if (distSq > rimOuterRadiusSq) continue;

                int blockX = origin.getX() + dx;
                int blockZ = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockX, blockZ);

                if (distSq <= floorRadiusSq) {
                    // Floor zone: carve down to full depth, fill with floor block
                    int floorY = centerY - depth;
                    // Remove blocks above floor up to surface
                    for (int y = surfaceY; y > floorY; y--) {
                        mutable.set(blockX, y, blockZ);
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                    // Place floor block
                    mutable.set(blockX, floorY, blockZ);
                    level.setBlock(mutable, floorBlock, 2);
                    placed = true;

                } else if (distSq <= radiusSq) {
                    // Wall zone: slope from floor depth to surface
                    float dist = (float) Math.sqrt(distSq);
                    float wallProgress = (dist - floorRadius) / (radius - floorRadius); // 0 at floor edge, 1 at rim
                    // Smooth curve using cosine interpolation
                    float heightFraction = (1.0f - (float) Math.cos(wallProgress * Math.PI)) * 0.5f;
                    int wallY = centerY - depth + (int) (depth * heightFraction);

                    // Remove blocks above wall height
                    for (int y = surfaceY; y > wallY; y--) {
                        mutable.set(blockX, y, blockZ);
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                    // Place surface block on wall
                    mutable.set(blockX, wallY, blockZ);
                    level.setBlock(mutable, floorBlock, 2);
                    placed = true;

                } else {
                    // Rim zone: raise above surface
                    float dist = (float) Math.sqrt(distSq);
                    float rimProgress = (dist - radius) / (rimOuterRadius - radius); // 0 at crater edge, 1 at outer rim
                    // Smooth bell curve for rim shape
                    float rimFraction = (float) Math.cos(rimProgress * Math.PI) * 0.5f + 0.5f;
                    int rimTop = surfaceY + (int) (rimHeight * rimFraction);

                    // Build rim blocks above surface
                    for (int y = surfaceY + 1; y <= rimTop; y++) {
                        mutable.set(blockX, y, blockZ);
                        level.setBlock(mutable, rimBlock, 2);
                    }
                    if (rimTop > surfaceY) {
                        placed = true;
                    }
                }
            }
        }

        return placed;
    }
}
