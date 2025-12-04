package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.menus.GravityNormalizerMenu;
import com.hecookin.adastramekanized.common.network.GravityNormalizerButtonPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class GravityNormalizerScreen extends AbstractContainerScreen<GravityNormalizerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/gravity_normalizer.png");
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, "textures/gui/slot_steel.png");

    // Color palette (same as oxygen distributor)
    private static final String[] COLOR_NAMES = {
        "Cyan", "Red", "Green", "Yellow", "Magenta", "Blue",
        "Orange", "Purple", "White", "Grey", "Black", "Brown", "Tan"
    };

    private static final int[] COLOR_VALUES = {
        0xFF00FFFF, // Cyan
        0xFFFF0000, // Red
        0xFF00FF00, // Green
        0xFFFFFF00, // Yellow
        0xFFFF00FF, // Magenta
        0xFF0000FF, // Blue
        0xFFFFA500, // Orange
        0xFF800080, // Purple
        0xFFFFFFFF, // White
        0xFF808080, // Grey
        0xFF000000, // Black
        0xFF8B4513, // Brown
        0xFFD2B48C  // Tan
    };

    // Buttons
    private Button powerButton;
    private Button visibilityButton;
    private ColorSquareButton colorButton;
    private boolean zoneVisibility;
    private int currentColorIndex;

    // Gravity slider
    private GravitySlider gravitySlider;
    private float lastSyncedGravity = -1f; // Track to detect server-side gravity changes

    public GravityNormalizerScreen(GravityNormalizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Match oxygen distributor dimensions
        this.imageWidth = 177;
        this.imageHeight = 244;
        this.titleLabelX = 15;
        this.titleLabelY = 66;
        this.inventoryLabelY = 152;
    }

    @Override
    protected void init() {
        super.init();

        // Initialize state from menu (properly synced)
        this.zoneVisibility = menu.getVisibility();
        this.currentColorIndex = menu.getColorIndex();

        // Power button at (8, 83), size 24x14
        // Button shows what will happen when clicked (ON if currently off, OFF if currently on)
        this.powerButton = this.addRenderableWidget(
            Button.builder(
                Component.literal(menu.getBlockEntity().isManuallyDisabled() ? "ON" : "OFF"),
                button -> {
                    boolean currentlyDisabled = menu.getBlockEntity().isManuallyDisabled();
                    boolean newDisabled = !currentlyDisabled;

                    PacketDistributor.sendToServer(new GravityNormalizerButtonPacket(
                        menu.getBlockEntity().getBlockPos(),
                        GravityNormalizerButtonPacket.Action.POWER,
                        newDisabled ? 0 : 1  // 0=OFF (disabled), 1=ON (enabled)
                    ));
                    button.setMessage(Component.literal(newDisabled ? "ON" : "OFF"));
                })
                .pos(this.leftPos + 8, this.topPos + 83)
                .size(24, 14)
                .build()
        );

        // Visibility button at (8, 109), size 24x14 with scaled text
        this.visibilityButton = this.addRenderableWidget(
            new ScaledTextButton(this.leftPos + 8, this.topPos + 109, 24, 14,
                Component.literal(zoneVisibility ? "Hide" : "Show"),
                button -> {
                    zoneVisibility = !zoneVisibility;
                    PacketDistributor.sendToServer(new GravityNormalizerButtonPacket(
                        menu.getBlockEntity().getBlockPos(),
                        GravityNormalizerButtonPacket.Action.VISIBILITY,
                        zoneVisibility ? 1 : 0
                    ));
                    button.setMessage(Component.literal(zoneVisibility ? "Hide" : "Show"));
                }, 0.85f)
        );

        // Color square button at (13, 131), size 14x14
        this.colorButton = this.addRenderableWidget(
            new ColorSquareButton(this.leftPos + 13, this.topPos + 131, 14, 14)
        );

        // Gravity slider - positioned at (76, 77) - moved down 7 and right 2
        int sliderX = this.leftPos + 76;
        int sliderY = this.topPos + 77;
        int sliderWidth = 60;
        int sliderHeight = 10;

        float currentGravity = menu.getTargetGravity();
        double sliderValue = (currentGravity - GravityNormalizerBlockEntity.MIN_GRAVITY) /
            (GravityNormalizerBlockEntity.MAX_GRAVITY - GravityNormalizerBlockEntity.MIN_GRAVITY);

        gravitySlider = new GravitySlider(sliderX, sliderY, sliderWidth, sliderHeight, sliderValue);
        this.addRenderableWidget(gravitySlider);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // Render slot backgrounds
        renderSlotBackgrounds(guiGraphics);

        // Render status text
        renderStatusText(guiGraphics, left, top);

        // Render energy gauge (moved up 1 more pixel)
        renderEnergyGauge(guiGraphics, left + 148, top + 82);

        // Render argon gauge (moved up 1 more pixel)
        renderArgonGauge(guiGraphics, left + 51, top + 81);
    }

    private void renderSlotBackgrounds(GuiGraphics graphics) {
        for (int i = 0; i < this.menu.slots.size(); i++) {
            var slot = this.menu.slots.get(i);
            if (slot.container == this.minecraft.player.getInventory()) {
                graphics.blit(SLOT_TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1,
                    0, 0, 18, 18, 18, 18);
            }
        }
    }

    private void renderStatusText(GuiGraphics guiGraphics, int leftPos, int topPos) {
        int textColor = 0xAA00FF; // Purple for gravity

        int blockCount = menu.getBlockCount();
        float energyUsage = menu.getEnergyUsage();
        float argonUsage = menu.getArgonUsage();
        int machineState = menu.getMachineState();
        float targetGravity = menu.getTargetGravity();

        int maxBlocks = GravityNormalizerBlockEntity.getMaxBlocks();

        // Energy per tick
        Component energyText = Component.literal(String.format("Energy: %.2f FE/t", energyUsage));
        guiGraphics.drawString(this.font, energyText, leftPos + 11, topPos + 9, textColor);

        // Argon per tick
        Component argonText = Component.literal(String.format("Argon: %.3f mB/t", argonUsage));
        guiGraphics.drawString(this.font, argonText, leftPos + 11, topPos + 20, textColor);

        // Blocks distributed
        Component blocksText = Component.literal(String.format("Blocks: %d/%d", blockCount, maxBlocks));
        guiGraphics.drawString(this.font, blocksText, leftPos + 11, topPos + 31, textColor);

        // Show gravity when active, status text when inactive/standby
        if (machineState == 2) {  // ACTIVE
            // Show gravity display instead of status
            String gravityText = String.format("Gravity: %.2fx", targetGravity);
            guiGraphics.drawString(this.font, gravityText, leftPos + 11, topPos + 42, textColor);
        } else {
            // Show status text when not active
            String statusText;
            int statusColor;
            switch (machineState) {
                case 0 -> {
                    statusText = "Inactive";
                    statusColor = 0xd95763;  // Red
                }
                case 1 -> {
                    statusText = "Standby";
                    statusColor = 0xFF8C00;  // Orange
                }
                default -> {
                    statusText = "Unknown";
                    statusColor = 0x808080;
                }
            }
            Component statusComponent = Component.literal(statusText);
            guiGraphics.drawString(this.font, statusComponent, leftPos + 11, topPos + 42, statusColor);
        }
    }

    private void renderEnergyGauge(GuiGraphics guiGraphics, int x, int y) {
        long energy = menu.getEnergy();
        long maxEnergy = menu.getMaxEnergy();

        int gaugeWidth = 11;
        int gaugeHeight = 46;  // 1 pixel taller

        if (maxEnergy > 0 && energy > 0) {
            float ratio = (float) energy / maxEnergy;
            int scaledHeight = (int) (gaugeHeight * ratio);
            if (scaledHeight > 0) {
                int yStart = y + gaugeHeight - scaledHeight;
                guiGraphics.fill(x, yStart, x + gaugeWidth, y + gaugeHeight, 0x7348de00);  // Same as oxygen distributor
            }
        }
    }

    private void renderArgonGauge(GuiGraphics guiGraphics, int x, int y) {
        long argon = menu.getChemicalAmount();
        long maxArgon = menu.getChemicalCapacity();

        int gaugeWidth = 12;
        int gaugeHeight = 47;  // 1 pixel taller

        if (maxArgon > 0 && argon > 0) {
            float ratio = (float) argon / maxArgon;
            int scaledHeight = Math.min(gaugeHeight, (int) (gaugeHeight * ratio));
            if (scaledHeight > 0) {
                // Argon color (violet/purple tint)
                int argonColor = 0xCC9966FF;
                int yStart = y + gaugeHeight - scaledHeight;
                guiGraphics.fill(x, yStart, x + gaugeWidth, y + gaugeHeight, argonColor);
                guiGraphics.fill(x, yStart, x + 1, y + gaugeHeight, 0x40FFFFFF);
                guiGraphics.fill(x + gaugeWidth - 1, yStart, x + gaugeWidth, y + gaugeHeight, 0x40000000);
                guiGraphics.fill(x, yStart, x + gaugeWidth, yStart + 1, 0x60FFFFFF);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x2a262b, false);
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x2a262b, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Update button states if they've changed
        // Button shows what will happen when clicked (ON if currently off, OFF if currently on)
        if (this.powerButton != null && menu.getBlockEntity() != null) {
            boolean isDisabled = menu.getBlockEntity().isManuallyDisabled();
            Component expectedPowerText = Component.literal(isDisabled ? "ON" : "OFF");
            if (!this.powerButton.getMessage().equals(expectedPowerText)) {
                this.powerButton.setMessage(expectedPowerText);
            }
        }

        // Update visibility button if changed
        if (this.visibilityButton != null) {
            boolean currentVisibility = menu.getVisibility();
            Component expectedVisText = Component.literal(currentVisibility ? "Hide" : "Show");
            if (!this.visibilityButton.getMessage().equals(expectedVisText)) {
                this.visibilityButton.setMessage(expectedVisText);
                this.zoneVisibility = currentVisibility;
            }
        }

        // Update color button if changed
        if (this.colorButton != null) {
            int currentColor = menu.getColorIndex();
            if (currentColor != this.currentColorIndex && currentColor >= 0 && currentColor < COLOR_NAMES.length) {
                this.currentColorIndex = currentColor;
            }
        }

        // Update slider if server-side gravity changed (for persistence)
        if (this.gravitySlider != null) {
            float serverGravity = menu.getTargetGravity();
            if (lastSyncedGravity < 0 || Math.abs(serverGravity - lastSyncedGravity) > 0.01f) {
                lastSyncedGravity = serverGravity;
                double newSliderValue = (serverGravity - GravityNormalizerBlockEntity.MIN_GRAVITY) /
                    (GravityNormalizerBlockEntity.MAX_GRAVITY - GravityNormalizerBlockEntity.MIN_GRAVITY);
                gravitySlider.setSliderValue(newSliderValue);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Render tooltips
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Energy tooltip (moved up 1 more, height 46)
        if (mouseX >= left + 148 && mouseX < left + 148 + 11 &&
            mouseY >= top + 82 && mouseY < top + 82 + 46) {
            long energy = menu.getEnergy();
            long maxEnergy = menu.getMaxEnergy();
            Component text = Component.literal(String.format("Energy: %,d / %,d FE", energy, maxEnergy));
            guiGraphics.renderTooltip(this.font, text, mouseX, mouseY);
        }

        // Argon tooltip (moved up 1 more, height 47)
        if (mouseX >= left + 51 && mouseX < left + 51 + 12 &&
            mouseY >= top + 81 && mouseY < top + 81 + 47) {
            long argon = menu.getChemicalAmount();
            long maxArgon = menu.getChemicalCapacity();
            Component text = Component.literal(String.format("Argon: %,d / %,d mB", argon, maxArgon));
            guiGraphics.renderTooltip(this.font, text, mouseX, mouseY);
        }

        // Color button tooltip
        if (this.colorButton != null && this.colorButton.isHovered()) {
            Component text = Component.literal(COLOR_NAMES[currentColorIndex] + " (Click to change)");
            guiGraphics.renderTooltip(this.font, text, mouseX, mouseY);
        }
    }

    /**
     * Custom color square button that renders as a filled colored square
     */
    private class ColorSquareButton extends Button {

        public ColorSquareButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> {
                // Left click - cycle forward
                currentColorIndex = (currentColorIndex + 1) % COLOR_NAMES.length;
                PacketDistributor.sendToServer(new GravityNormalizerButtonPacket(
                    menu.getBlockEntity().getBlockPos(),
                    GravityNormalizerButtonPacket.Action.COLOR,
                    currentColorIndex
                ));
            }, Button.DEFAULT_NARRATION);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.active && this.visible && this.isHovered()) {
                if (button == 1) {  // Right click - cycle backwards
                    currentColorIndex = currentColorIndex - 1;
                    if (currentColorIndex < 0) {
                        currentColorIndex = COLOR_NAMES.length - 1;
                    }
                    PacketDistributor.sendToServer(new GravityNormalizerButtonPacket(
                        menu.getBlockEntity().getBlockPos(),
                        GravityNormalizerButtonPacket.Action.COLOR,
                        currentColorIndex
                    ));
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    return true;
                } else if (button == 0) {  // Left click
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            }
            return false;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Draw filled color square
            int color = COLOR_VALUES[currentColorIndex];
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);

            // Draw border (darker when hovered)
            int borderColor = isHovered() ? 0xFFFFFFFF : 0xFF404040;
            // Top border
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);
            // Bottom border
            guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);
            // Left border
            guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);
            // Right border
            guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);
        }
    }

    /**
     * Custom slider for gravity control
     */
    private class GravitySlider extends AbstractSliderButton {
        private boolean userDragging = false;

        public GravitySlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            float gravity = getGravityFromSlider();
            setMessage(Component.literal(String.format("%.1fx", gravity)));
        }

        @Override
        protected void applyValue() {
            float gravity = getGravityFromSlider();
            lastSyncedGravity = gravity; // Prevent sync loop
            PacketDistributor.sendToServer(new GravityNormalizerButtonPacket(
                menu.getBlockEntity().getBlockPos(),
                GravityNormalizerButtonPacket.Action.SET_GRAVITY,
                gravity
            ));
        }

        private float getGravityFromSlider() {
            return (float) (GravityNormalizerBlockEntity.MIN_GRAVITY +
                value * (GravityNormalizerBlockEntity.MAX_GRAVITY - GravityNormalizerBlockEntity.MIN_GRAVITY));
        }

        public void setSliderValue(double newValue) {
            this.value = Math.max(0, Math.min(1, newValue));
            updateMessage();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            userDragging = true;
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            userDragging = false;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Draw slider background
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF555555);

            // Draw border around slider
            int borderColor = isHovered() ? 0xFFFFFFFF : 0xFF404040;
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);  // Top
            guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);  // Bottom
            guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);  // Left
            guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);  // Right

            // Draw slider handle
            int handleX = getX() + (int) (value * (width - 6));
            guiGraphics.fill(handleX, getY(), handleX + 6, getY() + height, 0xFFAA00FF);

            // Draw value text centered
            String text = getMessage().getString();
            int textWidth = GravityNormalizerScreen.this.font.width(text);
            guiGraphics.drawString(GravityNormalizerScreen.this.font, text,
                getX() + (width - textWidth) / 2, getY() + 1, 0xFFFFFF, false);
        }
    }

    /**
     * Custom button with scaled text for better fit
     */
    private class ScaledTextButton extends Button {
        private final float textScale;

        public ScaledTextButton(int x, int y, int width, int height, Component message, OnPress onPress, float textScale) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
            this.textScale = textScale;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Render button background using vanilla method
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected void renderScrollingString(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int width, int color) {
            // Render text with scaling
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();

            // Calculate center position
            int textWidth = font.width(this.getMessage());
            float scaledWidth = textWidth * textScale;
            float centerX = this.getX() + this.width / 2.0f;
            float centerY = this.getY() + (this.height - 8 * textScale) / 2.0f;

            // Scale around center
            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(textScale, textScale, 1.0f);
            poseStack.translate(-textWidth / 2.0f, 0, 0);

            guiGraphics.drawString(font, this.getMessage(), 0, 0, color, false);

            poseStack.popPose();
        }
    }
}
