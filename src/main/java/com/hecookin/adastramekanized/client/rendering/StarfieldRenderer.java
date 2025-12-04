package com.hecookin.adastramekanized.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Shared starfield renderer for loading screens and menu backgrounds.
 * Provides a consistent space-themed visual across all screens.
 */
public class StarfieldRenderer {

    private static final int STAR_COUNT = 1000;
    private static final int SHOOTING_STAR_COUNT = 1;

    // Singleton instance with pre-computed star data
    private static StarfieldRenderer instance;

    private final float[] starX;
    private final float[] starY;
    private final float[] starBrightness;
    private final float[] starSize;
    private final int[] starColor;

    // Shooting stars
    private final float[] shootingStarX;
    private final float[] shootingStarY;
    private final float[] shootingStarSpeed;
    private final float[] shootingStarAngle;
    private final float[] shootingStarLife;

    private final long creationTime;

    private StarfieldRenderer() {
        this.creationTime = System.currentTimeMillis();

        // Initialize stars with random positions and properties
        Random random = new Random(42); // Fixed seed for consistent starfield
        starX = new float[STAR_COUNT];
        starY = new float[STAR_COUNT];
        starBrightness = new float[STAR_COUNT];
        starSize = new float[STAR_COUNT];
        starColor = new int[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = random.nextFloat();
            starY[i] = random.nextFloat();
            starBrightness[i] = 0.3f + random.nextFloat() * 0.7f;
            starSize[i] = 0.25f + random.nextFloat() * 0.75f;  // Half size for subtler stars

            // Slight color variation (white to blue-ish to yellow-ish)
            float colorVar = random.nextFloat();
            if (colorVar < 0.7f) {
                starColor[i] = 0xFFFFFF;  // White stars
            } else if (colorVar < 0.85f) {
                starColor[i] = 0xAABBFF;  // Blue-ish stars
            } else if (colorVar < 0.95f) {
                starColor[i] = 0xFFFFAA;  // Yellow-ish stars
            } else {
                starColor[i] = 0xFFAAAA;  // Red-ish stars (rare)
            }
        }

        // Initialize shooting stars
        shootingStarX = new float[SHOOTING_STAR_COUNT];
        shootingStarY = new float[SHOOTING_STAR_COUNT];
        shootingStarSpeed = new float[SHOOTING_STAR_COUNT];
        shootingStarAngle = new float[SHOOTING_STAR_COUNT];
        shootingStarLife = new float[SHOOTING_STAR_COUNT];

        Random shootingRandom = new Random();
        for (int i = 0; i < SHOOTING_STAR_COUNT; i++) {
            resetShootingStar(i, shootingRandom);
        }
    }

    public static StarfieldRenderer getInstance() {
        if (instance == null) {
            instance = new StarfieldRenderer();
        }
        return instance;
    }

    private void resetShootingStar(int index, Random random) {
        shootingStarX[index] = random.nextFloat();
        shootingStarY[index] = random.nextFloat() * 0.5f; // Top half of screen
        shootingStarSpeed[index] = 0.3f + random.nextFloat() * 0.4f;
        shootingStarAngle[index] = 0.5f + random.nextFloat() * 0.3f; // Diagonal down-right
        shootingStarLife[index] = -1f - random.nextFloat();  // Random delay between -1f and -2f
    }

    /**
     * Render the complete starfield background.
     *
     * @param guiGraphics The GUI graphics context
     * @param screenWidth Screen width in pixels
     * @param screenHeight Screen height in pixels
     * @param alpha Overall alpha for fade effects (0.0 - 1.0)
     */
    public void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float alpha) {
        long elapsed = System.currentTimeMillis() - creationTime;
        float time = elapsed / 1000f;

        // Render black background
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        // Render stars
        renderStars(guiGraphics, screenWidth, screenHeight, time, alpha);

        // Render shooting stars
        renderShootingStars(guiGraphics, screenWidth, screenHeight, time, alpha);
    }

    /**
     * Render just the starfield without the black background.
     * Useful for overlaying on existing content.
     */
    public void renderStarsOnly(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float alpha) {
        long elapsed = System.currentTimeMillis() - creationTime;
        float time = elapsed / 1000f;

        renderStars(guiGraphics, screenWidth, screenHeight, time, alpha);
        renderShootingStars(guiGraphics, screenWidth, screenHeight, time, alpha);
    }

    private void renderStars(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float time, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        for (int i = 0; i < STAR_COUNT; i++) {
            // Twinkle effect
            float twinkle = (float) Math.sin(time * 2f + i * 0.1f) * 0.3f + 0.7f;
            float brightness = starBrightness[i] * twinkle * alpha;

            float x = starX[i] * screenWidth;
            float y = starY[i] * screenHeight;
            float size = starSize[i];

            int color = starColor[i];
            int r = ((color >> 16) & 0xFF);
            int g = ((color >> 8) & 0xFF);
            int b = (color & 0xFF);
            int a = (int) (brightness * 255);

            // Draw star as a small quad
            bufferBuilder.addVertex(matrix, x - size, y - size, 0).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, x - size, y + size, 0).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, x + size, y + size, 0).setColor(r, g, b, a);
            bufferBuilder.addVertex(matrix, x + size, y - size, 0).setColor(r, g, b, a);
        }

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
        RenderSystem.disableBlend();
    }

    private void renderShootingStars(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float time, float alpha) {
        Random random = new Random((long) (time * 1000));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        boolean hasVertices = false;
        for (int i = 0; i < SHOOTING_STAR_COUNT; i++) {
            shootingStarLife[i] += 0.01f;

            if (shootingStarLife[i] > 1f) {
                resetShootingStar(i, random);
                continue;
            }

            // Skip rendering during delay period (negative life)
            if (shootingStarLife[i] < 0f) {
                continue;
            }

            float progress = shootingStarLife[i];
            float x = (shootingStarX[i] + progress * shootingStarSpeed[i] * shootingStarAngle[i]) * screenWidth;
            float y = (shootingStarY[i] + progress * shootingStarSpeed[i]) * screenHeight;

            // Fade in and out
            float starAlpha = (float) Math.sin(progress * Math.PI) * alpha;
            int a = (int) (starAlpha * 255);

            if (a > 0 && x >= 0 && x <= screenWidth && y >= 0 && y <= screenHeight) {
                hasVertices = true;
                // Draw shooting star with trail
                float tailLength = 20f;
                float dx = -shootingStarAngle[i] * tailLength;
                float dy = -tailLength;

                // Head (bright)
                bufferBuilder.addVertex(matrix, x - 1, y - 1, 0).setColor(255, 255, 255, a);
                bufferBuilder.addVertex(matrix, x - 1, y + 1, 0).setColor(255, 255, 255, a);
                bufferBuilder.addVertex(matrix, x + 1, y + 1, 0).setColor(255, 255, 255, a);
                bufferBuilder.addVertex(matrix, x + 1, y - 1, 0).setColor(255, 255, 255, a);

                // Trail (fading)
                int tailAlpha = a / 3;
                bufferBuilder.addVertex(matrix, x + dx, y + dy, 0).setColor(200, 200, 255, 0);
                bufferBuilder.addVertex(matrix, x + dx, y + dy + 1, 0).setColor(200, 200, 255, 0);
                bufferBuilder.addVertex(matrix, x, y + 1, 0).setColor(255, 255, 255, tailAlpha);
                bufferBuilder.addVertex(matrix, x, y, 0).setColor(255, 255, 255, tailAlpha);
            }
        }

        if (hasVertices) {
            MeshData meshData = bufferBuilder.build();
            if (meshData != null) {
                BufferUploader.drawWithShader(meshData);
            }
        }
        RenderSystem.disableBlend();
    }
}
