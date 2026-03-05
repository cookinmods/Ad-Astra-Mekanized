package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.menus.PlanetsMenu;
import com.hecookin.adastramekanized.common.network.ModNetworking;
import com.hecookin.adastramekanized.common.network.ServerboundLandPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side screen for planet selection.
 * Features multi-page navigation (solar systems → planets → details).
 * Based on Ad Astra's PlanetsScreen.
 */
public class PlanetsScreen extends AbstractContainerScreen<PlanetsMenu> {

    private final List<Button> buttons = new ArrayList<>();
    private Button backButton;
    private double scrollAmount;

    private int pageIndex;
    private ResourceLocation selectedSolarSystem = ResourceLocation.fromNamespaceAndPath("adastramekanized", "solar_system");
    private Planet selectedPlanet;
    private boolean isEarthSelected = false;
    private boolean isMysteryPlanet = false;


    public PlanetsScreen(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = width;
        this.imageHeight = height;

        // Always start on planet list: solar system is auto-selected
        pageIndex = 1;
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        // Only reset scroll when returning to planet list, not when selecting a planet
        if (pageIndex <= 1) {
            scrollAmount = 0;
        }

        switch (pageIndex) {
            case 1, 2 -> {
                createPlanetButtons();
                if (pageIndex == 2 && (selectedPlanet != null || isEarthSelected)) {
                    createSelectedPlanetButtons();
                }
            }
        }

        // Back button (left side): only show on detail page
        backButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            isEarthSelected = false;
            selectedPlanet = null;
            isMysteryPlanet = false;
            pageIndex = 1;
            rebuildWidgets();
        }).bounds(10, height / 2 - 85, 20, 20).build());

        backButton.visible = pageIndex > 1;
    }

    private void createPlanetButtons() {
        // Add mystery button first if tier 4+ and there are undiscovered planets
        if (menu.tier() >= 4) {
            var undiscoveredPlanets = menu.getUndiscoveredRandomPlanets();
            if (!undiscoveredPlanets.isEmpty()) {
                var mysteryButton = addWidget(Button.builder(
                    Component.literal("???").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE),
                    b -> selectRandomPlanet()
                ).bounds(10, 0, 99, 20).build());

                buttons.add(mysteryButton);
            }
        }

        // Add Earth button
        buttons.add(addWidget(Button.builder(
            Component.literal("Earth"),
            b -> {
                pageIndex = 2;
                isEarthSelected = true;
                selectedPlanet = null;
                isMysteryPlanet = false;
                rebuildWidgets();
            }
        ).bounds(10, 0, 99, 20).build()));

        // Add all other planets
        for (var planet : menu.getSortedPlanets()) {
            if (!menu.getSolarSystem(planet).equals(selectedSolarSystem)) continue;

            buttons.add(addWidget(Button.builder(
                Component.literal(planet.displayName()),
                b -> {
                    pageIndex = 2;
                    selectedPlanet = planet;
                    isMysteryPlanet = false;
                    rebuildWidgets();
                }
            ).bounds(10, 0, 99, 20).build()));
        }
    }

    private void selectRandomPlanet() {
        var undiscoveredPlanets = menu.getUndiscoveredRandomPlanets();
        if (undiscoveredPlanets.isEmpty()) {
            return;
        }

        var random = minecraft.level.random;
        var randomPlanet = undiscoveredPlanets.get(random.nextInt(undiscoveredPlanets.size()));

        pageIndex = 2;
        selectedPlanet = randomPlanet;
        isMysteryPlanet = true;
        rebuildWidgets();
    }

    private void createSelectedPlanetButtons() {
        if (isEarthSelected) {
            // Earth Land button
            var landButton = addRenderableWidget(Button.builder(
                Component.literal("Land"),
                b -> land(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"))
            ).bounds(114, height / 2 - 77, 99, 20).build());
            return;
        }

        if (selectedPlanet == null) return;

        // Planet Land button
        var landButton = addRenderableWidget(Button.builder(
            Component.literal("Land"),
            b -> land(selectedPlanet.id())
        ).bounds(114, height / 2 - 77, 99, 20).build());

        // Placeholder: Space station buttons would go here
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderButtons(graphics, mouseX, mouseY, partialTick);
        backButton.visible = pageIndex > 1;

        // Prevent buttons from being pressed when outside view area
        buttons.forEach(button -> button.active = button.getY() > height / 2 - 63 && button.getY() < height / 2 + 88);
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int scrollPixels = (int) scrollAmount;

        // Scissor box for scrollable button area
        graphics.enableScissor(0, height / 2 - 43, 112, height / 2 + 88);

        for (var button : buttons) {
            button.render(graphics, mouseX, mouseY, partialTick);
        }

        for (int i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);
            button.setY((i * 24 - scrollPixels) + (height / 2 - 41));
        }

        graphics.disableScissor();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Dark space background
        graphics.fill(0, 0, width, height, 0xFF000419);

        // Static starfield background
        renderStarfield(graphics);

        // Selection menu box
        renderSelectionMenu(graphics);
    }

    protected void renderSelectionMenu(GuiGraphics graphics) {
        int menuX = 7;
        int menuY = height / 2 - 88;
        int menuWidth = pageIndex == 2 ? 240 : 105;
        int menuHeight = 177;

        // Menu background (semi-transparent dark box)
        graphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);

        // Menu border (subtle outline)
        graphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, 0xFF0F2559); // Top
        graphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, 0xFF0F2559); // Bottom
        graphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, 0xFF0F2559); // Left
        graphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF0F2559); // Right

        // Title
        Component titleText;
        if (pageIndex == 2 && isMysteryPlanet) {
            titleText = Component.literal("???").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
        } else if (pageIndex == 2 && isEarthSelected) {
            titleText = Component.literal("Earth");
        } else if (pageIndex == 2 && selectedPlanet != null) {
            titleText = Component.literal(selectedPlanet.displayName());
        } else {
            titleText = Component.literal("Solar System");
        }

        graphics.drawCenteredString(font, titleText, 57, height / 2 - 60, 0xFFFFFF);

        // Planet info (when hovering over selected planet)
        if (pageIndex == 2 && (isEarthSelected || selectedPlanet != null)) {
            renderPlanetInfo(graphics);
        }
    }

    private void renderPlanetInfo(GuiGraphics graphics) {
        // Position info box below Land button with padding
        int boxX = 114;
        int boxY = height / 2 - 52;  // 5 pixels below Land button (which ends at -57)
        int boxWidth = 126;  // Wider to fit text comfortably
        int boxHeight = 54;  // Height for 3 lines + padding

        // Draw info box background (semi-transparent with border)
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xCC000000);

        // Border
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF0F2559); // Top
        graphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, 0xFF0F2559); // Bottom
        graphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, 0xFF0F2559); // Left
        graphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF0F2559); // Right

        // Text starts with padding from top and left
        int textX = boxX + 6;
        int textY = boxY + 8;
        int lineSpacing = 14;

        if (isMysteryPlanet) {
            // Mystery planet message
            Component message = Component.literal("Explore the Galaxy")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
            graphics.drawCenteredString(font, message, boxX + boxWidth / 2, textY + 5, 0xFFAA55FF);

            Component message2 = Component.literal("for new Planets")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
            graphics.drawCenteredString(font, message2, boxX + boxWidth / 2, textY + 5 + lineSpacing, 0xFFAA55FF);
        } else if (isEarthSelected) {
            // Earth info
            graphics.drawString(font,
                Component.literal("Gravity: 1.0x").withStyle(ChatFormatting.GRAY),
                textX, textY, 0xAAAAAA);

            graphics.drawString(font,
                Component.literal("Temperature: 20°C").withStyle(ChatFormatting.GRAY),
                textX, textY + lineSpacing, 0xAAAAAA);

            graphics.drawString(font,
                Component.literal("Atmosphere: Breathable").withStyle(ChatFormatting.GRAY),
                textX, textY + lineSpacing * 2, 0xAAAAAA);
        } else if (selectedPlanet != null) {
            // Planet info
            graphics.drawString(font,
                Component.literal("Gravity: " + String.format("%.1fx", selectedPlanet.properties().gravity()))
                    .withStyle(ChatFormatting.GRAY),
                textX, textY, 0xAAAAAA);

            graphics.drawString(font,
                Component.literal("Temperature: " + String.format("%.0f°C", selectedPlanet.properties().temperature()))
                    .withStyle(ChatFormatting.GRAY),
                textX, textY + lineSpacing, 0xAAAAAA);

            String atmosphere = selectedPlanet.atmosphere().hasAtmosphere() ?
                (selectedPlanet.atmosphere().breathable() ? "Breathable" : "Toxic") : "None";
            graphics.drawString(font,
                Component.literal("Atmosphere: " + atmosphere).withStyle(ChatFormatting.GRAY),
                textX, textY + lineSpacing * 2, 0xAAAAAA);
        }
    }

    private void renderStarfield(GuiGraphics graphics) {
        // Static starfield
        int starCount = 400;
        var starRandom = net.minecraft.util.RandomSource.create(10842);

        // Generate static star positions
        for (int i = 0; i < starCount; i++) {
            // Generate star position
            int starX = (int) (starRandom.nextFloat() * width);
            int starY = (int) (starRandom.nextFloat() * height);

            // Star size (1-2 pixels)
            int size = 1 + starRandom.nextInt(2);

            // Star brightness variation
            float brightness = 0.6f + starRandom.nextFloat() * 0.4f;
            int alpha = (int) (brightness * 255);
            int color = (alpha << 24) | 0xFFFFFF;

            // Draw star as small filled rectangle
            graphics.fill(starX, starY, starX + size, starY + size, color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < 112 && mouseX > 6 && mouseY > height / 2f - 43 && mouseY < height / 2f + 88) {
            setScrollAmount(scrollAmount - scrollY * 16 / 2f);
        }
        return true;
    }

    @Override
    public void onClose() {
        // Back navigation from detail page to planet list
        if (pageIndex > 1) {
            isEarthSelected = false;
            selectedPlanet = null;
            isMysteryPlanet = false;
            pageIndex = 1;
            rebuildWidgets();
            return;
        }

        Player player = menu.player();
        // Only allow closing if not in rocket (creative/spectator can always close)
        if (player.isCreative() || player.isSpectator()) {
            super.onClose();
        } else if (!(player.getVehicle() instanceof Rocket)) {
            super.onClose();
        }
    }

    protected void setScrollAmount(double amount) {
        scrollAmount = Mth.clamp(amount, 0.0, Math.max(0, buttons.size() * 24 - 131));
    }

    public void land(ResourceLocation planetId) {
        ModNetworking.sendToServer(new ServerboundLandPacket(planetId.toString(), true));
        close();
    }

    protected void close() {
        pageIndex = 1;
        super.onClose();
    }

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) return string;
        String[] words = string.replace("_", " ").split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return result.toString().trim();
    }
}
