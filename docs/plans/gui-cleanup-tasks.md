# GUI Cleanup Tasks

Remaining items from the GUI audit (March 2026). Pick up where you left off.

## 1. Missing `oxygen_controller.png` texture
- `OxygenControllerScreen` references `adastramekanized:textures/gui/container/oxygen_controller.png`
- Texture file does not exist: will cause purple/black missing texture when the screen opens
- **Fix**: Either create the texture in Aseprite or add a fallback to a generic container texture (like oxygen distributor does)
- File: `src/main/java/com/hecookin/adastramekanized/client/screens/OxygenControllerScreen.java`

## 2. Oxygen HUD overlay for space suits
- Gameplay-critical: players wearing space suits in non-breathable atmospheres have no visual feedback for remaining oxygen
- Reference Ad Astra shows an oxygen tank bar on the HUD when the player is wearing a space suit
- **Fix**: Add a HUD overlay (similar to `VehicleOverlayRenderer`) that renders oxygen level when wearing suit in non-breathable dimension
- Related: `VehicleOverlayRenderer` in `client/overlay/` is a good pattern to follow

## 3. Dead `OxygenDistributorScreen` should be removed
- `src/main/java/com/hecookin/adastramekanized/client/screens/OxygenDistributorScreen.java` exists but is superseded by `GuiOxygenDistributor` in `client/gui/`
- The dead screen uses the old `AbstractContainerScreen` pattern while the active one uses the Mekanism-style GUI framework
- **Fix**: Delete `OxygenDistributorScreen.java` and remove any remaining references/imports

## 4. Unused Mekanism-style GUI framework
- Files in `client/gui/element/`: `GuiSlot.java`, `GuiGauge.java`, `GuiChemicalGauge.java`, `GuiEnergyGauge.java`
- Base class: `client/gui/base/GuiMekanismStyle.java`
- These were built for Mekanism-style machine GUIs but may not all be actively used
- **Fix**: Audit which elements are actually referenced by active GUIs (`GuiOxygenDistributor`, etc.). Remove unused ones, or connect them if they're needed for upcoming machine GUIs.

## 5. Fix ESC/pause behavior in Solar System UI
- `PlanetsScreen.isPauseScreen()` returns `true`, which pauses the game in singleplayer
- Pressing ESC while on the planet list (page 1) does nothing if the player is in a rocket (intended: prevents closing mid-flight)
- **Problem**: ESC should open the normal pause menu instead of being silently swallowed. Players expect ESC to always do something. Currently it's a dead key when riding a rocket on page 1.
- ESC on page 2 (detail) correctly navigates back to page 1
- **Fix**: When ESC is pressed on page 1 while in a rocket, open the vanilla pause screen (`Minecraft.getInstance().setScreen(new PauseScreen(true))`) instead of doing nothing. This lets players access settings/quit without closing the planet selection.
- File: `src/main/java/com/hecookin/adastramekanized/client/screens/PlanetsScreen.java` (lines 341-359, `onClose()` method)
