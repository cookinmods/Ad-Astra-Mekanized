package com.hecookin.adastramekanized.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders gravity zones as translucent ice-textured blocks on the client side.
 * Used to visualize gravity normalization from gravity normalizers.
 * Supports multiple normalizers with different colored ice for debugging.
 */
@OnlyIn(Dist.CLIENT)
public class GravityZoneRenderer {

    private static final GravityZoneRenderer INSTANCE = new GravityZoneRenderer();

    // Track gravity zones per normalizer with different colors
    private final Map<BlockPos, NormalizerZone> normalizerZones = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockPos> blockOwnership = new ConcurrentHashMap<>(); // Which normalizer owns which block
    private boolean renderingEnabled = false;

    // Color palette for different normalizers - same as oxygen distributor
    private static final float[][] NORMALIZER_COLORS = {
        {0.0f, 1.0f, 1.0f}, // Cyan - bright cyan (no red)
        {1.0f, 0.0f, 0.0f}, // Red - pure red (no green/blue)
        {0.0f, 1.0f, 0.0f}, // Green - pure green (no red/blue)
        {1.0f, 1.0f, 0.0f}, // Yellow - bright yellow (no blue)
        {1.0f, 0.0f, 1.0f}, // Magenta - bright magenta (no green)
        {0.4f, 0.4f, 1.0f}, // Blue - slightly darker blue to contrast with ice
        {1.0f, 0.5f, 0.0f}, // Orange - bright orange
        {0.5f, 0.0f, 1.0f}, // Purple - deep purple
        {1.0f, 1.0f, 1.0f}, // White - pure white
        {0.5f, 0.5f, 0.5f}, // Grey - medium grey
        {0.2f, 0.2f, 0.2f}, // Black/Dark Grey - near black
        {0.55f, 0.27f, 0.07f}, // Brown - saddle brown
        {0.82f, 0.71f, 0.55f}  // Tan - burlywood tan
    };

    // Use vanilla ice texture for gravity visualization
    private static final ResourceLocation ICE_TEXTURE = ResourceLocation.withDefaultNamespace("block/ice");
    private static final float ALPHA = 0.08f; // Transparent for subtle visualization

    // Helper class to track zones per normalizer
    private static class NormalizerZone {
        final BlockPos normalizerPos;
        final Set<BlockPos> gravityBlocks;
        float[] color;
        int colorIndex;
        boolean visible;
        float targetGravity;
        long lastUpdateTime;

        NormalizerZone(BlockPos pos, int colorIndex) {
            this.normalizerPos = pos;
            this.gravityBlocks = ConcurrentHashMap.newKeySet();
            this.colorIndex = colorIndex;
            this.color = NORMALIZER_COLORS[colorIndex % NORMALIZER_COLORS.length];
            this.visible = false;
            this.targetGravity = 1.0f;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        void setColorIndex(int index) {
            this.colorIndex = index;
            this.color = NORMALIZER_COLORS[index % NORMALIZER_COLORS.length];
        }
    }

    private GravityZoneRenderer() {}

    public static GravityZoneRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * Update gravity zones for a specific normalizer.
     * Each normalizer gets its own color for debugging.
     * Prevents overlap by checking block ownership.
     */
    public void updateNormalizerZones(BlockPos normalizerPos, Set<BlockPos> zones, int colorIndex, float targetGravity) {
        NormalizerZone zone = normalizerZones.computeIfAbsent(normalizerPos, pos ->
            new NormalizerZone(pos, colorIndex)
        );

        // Update color if changed
        if (zone.colorIndex != colorIndex) {
            zone.setColorIndex(colorIndex);
        }

        // Update target gravity
        zone.targetGravity = targetGravity;

        // Remove old ownership claims
        for (BlockPos oldPos : zone.gravityBlocks) {
            if (blockOwnership.get(oldPos) == normalizerPos) {
                blockOwnership.remove(oldPos);
            }
        }

        // Clear and rebuild with new positions
        zone.gravityBlocks.clear();
        if (zones != null && !zones.isEmpty()) {
            // Add all zones from server - server already handles conflict resolution
            zone.gravityBlocks.addAll(zones);
            for (BlockPos pos : zones) {
                blockOwnership.put(pos, normalizerPos);
            }
        }

        zone.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update zones without color change
     */
    public void updateNormalizerZones(BlockPos normalizerPos, Set<BlockPos> zones) {
        NormalizerZone zone = normalizerZones.get(normalizerPos);
        if (zone != null) {
            updateNormalizerZones(normalizerPos, zones, zone.colorIndex, zone.targetGravity);
        } else {
            // Default to cyan if not set
            updateNormalizerZones(normalizerPos, zones, 0, 1.0f);
        }
    }

    /**
     * Toggle visibility for a specific normalizer
     */
    public void setNormalizerVisibility(BlockPos normalizerPos, boolean visible) {
        NormalizerZone zone = normalizerZones.get(normalizerPos);
        if (zone != null) {
            zone.visible = visible;
        }
    }

    /**
     * Remove a normalizer and its zones
     */
    public void removeNormalizer(BlockPos normalizerPos) {
        NormalizerZone zone = normalizerZones.remove(normalizerPos);
        if (zone != null) {
            // Remove ownership claims
            for (BlockPos pos : zone.gravityBlocks) {
                if (blockOwnership.get(pos) == normalizerPos) {
                    blockOwnership.remove(pos);
                }
            }
        }
    }

    /**
     * Clear all gravity zones from all normalizers
     */
    public void clearAllZones() {
        normalizerZones.clear();
        blockOwnership.clear();
    }

    /**
     * Toggle global rendering on/off
     */
    public void setRenderingEnabled(boolean enabled) {
        this.renderingEnabled = enabled;
        if (!enabled) {
            clearAllZones();
        }
    }

    /**
     * Check if rendering is enabled globally
     */
    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    /**
     * Get all gravity blocks across all normalizers for face culling
     */
    private Set<BlockPos> getAllGravityBlocks() {
        Set<BlockPos> allBlocks = new HashSet<>();
        for (NormalizerZone zone : normalizerZones.values()) {
            if (zone.visible) {
                allBlocks.addAll(zone.gravityBlocks);
            }
        }
        return allBlocks;
    }

    /**
     * Render all visible gravity zones in the world
     * Called from world render events
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        if (!renderingEnabled || normalizerZones.isEmpty()) {
            return;
        }

        // Get the ice texture sprite from the block atlas
        TextureAtlasSprite iceSprite = Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(ICE_TEXTURE);

        // Use translucent render type for see-through effect
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // Get all gravity blocks for face culling
        Set<BlockPos> allGravityBlocks = getAllGravityBlocks();

        // Render each normalizer's zones with its own color
        for (NormalizerZone zone : normalizerZones.values()) {
            if (zone.visible && !zone.gravityBlocks.isEmpty()) {
                renderNormalizerZone(vertexConsumer, matrix, zone, cameraPos, iceSprite, allGravityBlocks);
            }
        }
    }

    /**
     * Render a single normalizer's gravity zone
     */
    private void renderNormalizerZone(VertexConsumer vertexConsumer, Matrix4f matrix,
                                      NormalizerZone zone, Vec3 cameraPos, TextureAtlasSprite sprite,
                                      Set<BlockPos> allGravityBlocks) {
        for (BlockPos pos : zone.gravityBlocks) {
            renderGravityBlock(vertexConsumer, matrix, pos, cameraPos, sprite,
                            zone.color[0], zone.color[1], zone.color[2], allGravityBlocks);
        }
    }

    /**
     * Render a single gravity block as a translucent ice-textured cube
     * Only renders faces that are exposed (not adjacent to other gravity blocks)
     */
    private void renderGravityBlock(VertexConsumer vertexConsumer, Matrix4f matrix, BlockPos pos,
                                  Vec3 cameraPos, TextureAtlasSprite sprite,
                                  float red, float green, float blue,
                                  Set<BlockPos> allGravityBlocks) {
        float x1 = (float) (pos.getX() - cameraPos.x);
        float y1 = (float) (pos.getY() - cameraPos.y);
        float z1 = (float) (pos.getZ() - cameraPos.z);
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;

        // Slightly shrink the cube to avoid z-fighting
        float shrink = 0.001f;
        x1 += shrink;
        y1 += shrink;
        z1 += shrink;
        x2 -= shrink;
        y2 -= shrink;
        z2 -= shrink;

        // Only render faces that don't have an adjacent gravity block
        // This creates a cleaner look when many gravity blocks are adjacent

        // Top face (Y+)
        if (!allGravityBlocks.contains(pos.above())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y2, z1,
                x2, y2, z1,
                x2, y2, z2,
                x1, y2, z2,
                0, 1, 0,  // Normal pointing up
                red, green, blue
            );
        }

        // Bottom face (Y-)
        if (!allGravityBlocks.contains(pos.below())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x2, y1, z2,
                x2, y1, z1,
                x1, y1, z1,
                0, -1, 0,  // Normal pointing down
                red, green, blue
            );
        }

        // North face (Z-)
        if (!allGravityBlocks.contains(pos.north())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z1,
                x2, y1, z1,
                x2, y2, z1,
                x1, y2, z1,
                0, 0, -1,  // Normal pointing north
                red, green, blue
            );
        }

        // South face (Z+)
        if (!allGravityBlocks.contains(pos.south())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z2,
                x1, y1, z2,
                x1, y2, z2,
                x2, y2, z2,
                0, 0, 1,  // Normal pointing south
                red, green, blue
            );
        }

        // West face (X-)
        if (!allGravityBlocks.contains(pos.west())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x1, y1, z1,
                x1, y2, z1,
                x1, y2, z2,
                -1, 0, 0,  // Normal pointing west
                red, green, blue
            );
        }

        // East face (X+)
        if (!allGravityBlocks.contains(pos.east())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z1,
                x2, y1, z2,
                x2, y2, z2,
                x2, y2, z1,
                1, 0, 0,  // Normal pointing east
                red, green, blue
            );
        }
    }

    /**
     * Render a textured quad face using the ice texture with custom color tint
     */
    private void renderTexturedQuad(VertexConsumer vertexConsumer, Matrix4f matrix, TextureAtlasSprite sprite,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float x3, float y3, float z3,
                                   float x4, float y4, float z4,
                                   float nx, float ny, float nz,
                                   float red, float green, float blue) {

        // Get texture UV coordinates from sprite
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Add vertices with texture coordinates from the ice sprite
        vertexConsumer.addVertex(matrix, x1, y1, z1)
            .setColor(red, green, blue, ALPHA)
            .setUv(minU, minV)
            .setLight(0xF000F0)  // Full bright
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x2, y2, z2)
            .setColor(red, green, blue, ALPHA)
            .setUv(maxU, minV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x3, y3, z3)
            .setColor(red, green, blue, ALPHA)
            .setUv(maxU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x4, y4, z4)
            .setColor(red, green, blue, ALPHA)
            .setUv(minU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);
    }
}
