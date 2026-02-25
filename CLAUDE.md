# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ad Astra Mekanized is a Minecraft 1.21.1 NeoForge mod that provides space exploration features integrated with Mekanism's infrastructure systems. It's a complete replacement for Ad Astra, combining rockets, planets, space stations, and oxygen distribution with Mekanism's chemical systems, energy systems, and Immersive Engineering fuels.

## Git Commit Rules
- **Do NOT include `Co-Authored-By` lines in commit messages.** Claude should not be listed as a contributor.
- **Do NOT push to remote unless explicitly told to.** GitHub hooks notify Discord on push, so only push when the user says "push". Committing locally is fine without permission.

## Build System and Development Commands

### Primary Development Commands
```bash
# Build the mod
./gradlew build

# Development environment
./gradlew runClient     # Launch development client
./gradlew runServer     # Launch development server
./gradlew runData       # Run data generation and test mod loading

# Testing and verification
./gradlew test          # Run test suite
./gradlew clean build   # Clean build (recommended after integration changes)

# Local dependency setup (if needed)
./gradlew -b install-local-deps.gradle.kts setupLocalMaven
```

### Project Configuration
- **Platform**: NeoForge 1.21.1 (version 21.1.209)
- **Java Version**: Java 21 (required)
- **Gradle**: 8.14.3+ (wrapper included)
- **NeoGradle**: 7.0.192

## Architecture Overview

### Package Structure
```
com.hecookin.adastramekanized/
├── api/                    # Public API interfaces for integration
│   ├── IChemicalIntegration.java
│   ├── IEnergyIntegration.java
│   └── IFuelIntegration.java
├── integration/            # Mod integration handlers (reflection-based)
│   ├── ModIntegrationManager.java
│   ├── mekanism/MekanismIntegration.java
│   ├── immersiveengineering/ImmersiveEngineeringIntegration.java
│   └── create/CreateIntegration.java
├── config/                 # Configuration system
└── [future packages for rockets, planets, oxygen, fuel systems]
```

### Integration Architecture

The mod uses a **hybrid reflection-based integration approach** to support optional dependencies:

- **Dependencies are marked as optional** in neoforge.mods.toml
- **Runtime reflection** provides full API access when mods are present
- **Graceful fallbacks** when integration mods are missing
- **No compile-time dependencies** on integration mods

#### Required Dependencies
- **Mekanism**: Chemical systems (oxygen distribution), energy systems
- **Tectonic** + **Lithostitched**: Planet terrain density function system
- **ChemLib Mekanized**: Chemical library (must load before this mod)

#### Optional Dependencies
- **Create**: Adds 25 mechanical crafting recipes (rockets, engines, tanks, oxygen gear) and 5 pressing recipes (ingot-to-sheet). Reflection-based kinetic/stress integration. All recipes use `neoforge:mod_loaded` conditions.
- **Create Crafts & Additions**: Adds 3 rolling machine recipes (iron/steel/etrium sheet-to-rod). Zero Java integration, purely conditional recipe JSON.
- **Immersive Engineering**: Fuel systems (diesel/biodiesel replace Ad Astra fuels)
- **Born in Chaos**: Horror mob integration for planet spawning

#### Integration Manager Usage
Always access integrations through the central manager:
```java
ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
if (manager.isMekanismAvailable()) {
    manager.getMekanismIntegration().handleOxygen(...);
}
```

## Development Guidelines

### Code Standards
- Use existing project logging: `AdAstraMekanized.LOGGER`
- Follow NeoForge 1.21.1 patterns and conventions
- Always check integration availability before using mod-specific features
- Provide fallback behavior for all integrated systems
- Cache reflected classes and methods for performance

### Integration Best Practices
1. **Test both with and without integration mods** present
2. **Use graceful fallbacks** for all integration failures
3. **Access integrations only through ModIntegrationManager**
4. **Check integration status** before attempting mod-specific operations

### Development Status
- **Phase 1 Complete**: Integration architecture foundation is fully implemented
- **Phase 2 Complete**: Dynamic planet generation and teleportation system implemented
- **Current Status**: Planet generation functional but needs terrain quality improvements
- **Next Priority**: Enhanced terrain generation, ore spawning systems, improved biome diversity

## Key Files and Configuration

### Core Implementation Files
- `AdAstraMekanized.java` - Main mod class with integration manager initialization
- `ModIntegrationManager.java` - Central integration coordinator
- `AdAstraMekanizedConfig.java` - Configuration system with integration settings

### Build Configuration
- `build.gradle` and `build.gradle.kts` - Gradle build configuration
- `gradle.properties` - Project metadata (mod_id: adastramekanized, version: 1.0.0)
- `install-local-deps.gradle.kts` - Local dependency setup for development

### Resources
- `src/main/resources/META-INF/neoforge.mods.toml` - Mod metadata with optional dependencies
- Local development dependencies in `libs/` directory (Mekanism, IE homebaked builds)

## Testing and Verification

### Integration Testing
The mod includes automatic integration tests during startup. Monitor logs for:
- "Mekanism integration initialized" / "Mekanism integration failed"
- "Immersive Engineering integration initialized" / "IE integration failed"
- "Create integration initialized" / "Create integration failed"
- Integration status summary from ModIntegrationManager

### Recommended Testing Approach
- Clean builds after integration changes: `./gradlew clean build`
- Test mod loading with: `./gradlew runData`
- Verify both with and without optional dependencies present
- Integration tests run automatically during mod setup

## Asset Development Guidelines

### Block and Item Model Requirements
When creating block models, **always use the correct parent namespace**:

```json
// CORRECT - Working block model
{
  "parent": "block/cube_all",
  "textures": {
    "all": "adastramekanized:block/block_name"
  }
}

// INCORRECT - Will cause texture loading failure
{
  "parent": "cube_all",
  "textures": {
    "all": "adastramekanized:block/block_name"
  }
}
```

**Critical Asset Requirements:**
1. **Block Models**: Must use `"parent": "block/cube_all"` (with block/ prefix)
2. **Item Models**: Use `"parent": "adastramekanized:block/block_name"` for blocks
3. **Blockstates**: Use proper multi-line JSON formatting (never compressed single-line)
4. **Textures**: Must be valid 16x16 PNG files in RGBA format

### Common Asset Issues and Solutions

#### Texture Loading Failures
**Problem**: Textures appear as purple/black missing texture blocks
**Root Causes**:
1. Missing `block/` prefix in block model parent reference
2. Compressed JSON in blockstate files (single-line format)
3. Invalid texture file paths or missing textures

**Solution Process**:
1. Compare failing models with working models (e.g., desh_block.json)
2. Verify all block models use `"parent": "block/cube_all"`
3. Ensure blockstate JSON uses proper multi-line formatting
4. Confirm texture files exist and are valid PNG format

#### Asset Generation Workflow
1. **Manual Creation**: Create models/blockstates manually following working examples
2. **Batch Operations**: Use proper JSON formatting when creating multiple files
3. **Verification**: Always test build after asset changes: `./gradlew build`
4. **Comparison**: Compare new assets with known working examples

### Historical Issues Resolved
**September 2024 - Planet Stone Texture Loading**:
- **Issue**: All 18 planet stone blocks showed missing textures
- **Root Cause**: Block models used `"parent": "cube_all"` instead of `"parent": "block/cube_all"`
- **Secondary Issue**: Some blockstates had compressed JSON format from bash generation
- **Resolution**: Fixed parent references and reformatted blockstates to multi-line JSON
- **Prevention**: Always reference working block models when creating new assets

## Recipe Development Guidelines

### ⚠️ CRITICAL: Recipe Generation System

**MOST IMPORTANT RULE**: All standard crafting recipes MUST be added to `ModRecipeProvider`, NOT created as manual JSON files.

#### Why Manual JSON Recipes Don't Work
- **Problem**: Recipes created manually in `src/main/resources/data/adastramekanized/recipe/` will NOT appear in-game
- **Root Cause**: The datagen system (`ModRecipeProvider`) controls recipe generation and overrides manual files
- **Symptoms**: Recipes appear to be correct in JSON but don't show up in JEI or crafting table

#### Correct Recipe Development Workflow

**❌ WRONG - Manual JSON (will NOT work)**:
```bash
# Creating files in src/main/resources/data/adastramekanized/recipe/
echo '{"type": "minecraft:crafting_shapeless", ...}' > my_recipe.json
```

**✅ CORRECT - Using ModRecipeProvider (WILL work)**:
```java
// In ModRecipeProvider.buildCraftingRecipes():
ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_SHEETBLOCK.get())
    .requires(ModItems.STEEL_SHEET.get(), 9)
    .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
    .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_sheetblock"));
```

#### Required Steps for All Recipes

1. **Add to ModRecipeProvider**: Write recipe builder code in appropriate method:
   - `buildCraftingRecipes()` - Crafting table recipes
   - `buildNasaWorkbenchRecipes()` - NASA Workbench recipes
   - `buildSmeltingRecipes()` - Furnace recipes
   - `buildBlastingRecipes()` - Blast furnace recipes

2. **Run Data Generation**: Execute datagen to create actual JSON files:
   ```bash
   ./gradlew runData
   ```

3. **Verify Generated Files**: Check `src/generated/resources/data/adastramekanized/recipe/` for your recipes

4. **Build and Test**: Run build and verify in-game:
   ```bash
   ./gradlew build
   ```

#### Recipe Types and Locations

| Recipe Type | Method | Generated Location |
|------------|--------|-------------------|
| Crafting (shapeless/shaped) | `buildCraftingRecipes()` | `src/generated/resources/data/.../recipe/` |
| NASA Workbench | `buildNasaWorkbenchRecipes()` | `src/generated/resources/data/.../recipe/` |
| Smelting | `buildSmeltingRecipes()` | `src/generated/resources/data/.../recipe/` |
| Blasting | `buildBlastingRecipes()` | `src/generated/resources/data/.../recipe/` |
| Create Pressing | Manual JSON with conditions | `src/main/resources/data/.../recipe/pressing/` |
| Create Rolling | Manual JSON with conditions | `src/main/resources/data/.../recipe/rolling/` |

**Note**: Create integration recipes are an exception - they use manual JSON with `neoforge:mod_loaded` conditions since they require special conditional loading.

#### Common Mistakes and Solutions

**October 2025 - Sheetblock Recipes Not Showing**:
- **Issue**: Created 12 manual JSON sheetblock recipes that didn't appear in-game
- **Root Cause**: Recipes were manually created in `src/main/resources/` instead of using `ModRecipeProvider`
- **Resolution**:
  1. Added all 12 recipes to `buildCraftingRecipes()` using `ShapelessRecipeBuilder`
  2. Deleted manual JSON files to avoid conflicts
  3. Ran `./gradlew runData` to generate proper recipes
  4. Verified recipes appeared in `src/generated/resources/`
- **Prevention**: ALWAYS use `ModRecipeProvider` for standard Minecraft recipes

#### Recipe Development Checklist

- [ ] Recipe code added to appropriate `ModRecipeProvider` method
- [ ] `./gradlew runData` executed successfully
- [ ] Generated JSON exists in `src/generated/resources/`
- [ ] No manual JSON conflicts in `src/main/resources/` for same recipe
- [ ] `./gradlew build` completes without errors
- [ ] Recipe tested in-game (shows in JEI and works in crafting)

## Future Feature Considerations

### Celestial Time Systems
**Per-Planet Day/Night Cycles**: Potential future feature to implement different day/night cycle lengths per planet without affecting game tick rate. This would require:
- Integration with time management mods (e.g., BetterDays)
- Custom sky rendering system overrides
- Complex client-server synchronization
- Careful handling of time-dependent game systems (crops, spawning, redstone)
- Cross-dimensional player transitions

**Current Status**: Not implemented. Standard Minecraft time progression used for all dimensions.
**Complexity**: High risk due to impact on core game systems and mod compatibility.

## Current Implementation Progress

### ✅ Phase 1 Complete: Integration Architecture Foundation
**Status**: ✅ Completed September 2025
- Reflection-based integration system for optional mod dependencies
- Central integration manager with graceful fallbacks
- Comprehensive configuration system
- Build system properly configured for hybrid approach

### ✅ Phase 2 Complete: Block & Material Migration + Planet System Core
**Status**: ✅ Completed September 2025
- Complete block and material migration (112+ blocks, 25+ items)
- Planet stone variants, industrial blocks, ore blocks, alien wood sets
- Core planet data structures with Codec serialization
- JSON-based planet loading and client-server networking
- Planet registry system with thread-safe management

### ✅ Phase 3 Complete: Comprehensive PlanetMaker System
**Status**: ✅ Completed September 2025

#### Advanced Planet Generation System:
1. **PlanetMaker API**:
   - ✅ Fluent builder pattern with 50+ configurable parameters
   - ✅ Complete noise router control (continents, erosion, ridges, jaggedness)
   - ✅ Advanced terrain shaping (height variations, density factors, gradients)
   - ✅ Comprehensive surface block configuration
   - ✅ Noise-based ore generation with proper rarity distribution

2. **Planet Examples Implemented**:
   - ✅ **Moon**: Craterous lunar landscape with moon-specific materials
   - ✅ **Mars**: Dramatic Martian terrain with canyons and highlands
   - ✅ **Venus**: Thick toxic atmosphere with sulfuric acid rain
   - ✅ **Glacio**: Icy world with thin atmosphere and frozen terrain

3. **Technical Features**:
   - ✅ Automatic JSON generation for dimensions and noise settings
   - ✅ Noise-based ore distribution using `minecraft:ore_gap` threshold system
   - ✅ Surface rule validation with proper `surface_type` parameters
   - ✅ Multi-parameter terrain control (continental, erosion, ridge scales)
   - ✅ Advanced hill/mountain generation with jaggedness controls

#### Current Working Features:
- ✅ Planet teleportation: `/planet teleport moon`, `/planet teleport mars`, `/planet teleport venus`, `/planet teleport glacio`
- ✅ Planet information: `/planet info <planet_name>` for detailed planet characteristics
- ✅ Planet listing: `/planet list` to view all available worlds
- ✅ Automatic planet file generation with `PlanetGenerationRunner`

#### Advanced Terrain Controls:
1. **Noise Parameters**: Continental scale (1.0-100.0), erosion scale (1.0-100.0), ridge scale (1.0-50.0)
2. **Height Variation**: Four-parameter system for base/secondary/tertiary/fine height control
3. **Mountain Generation**: Jaggedness scale (0.1-1.0), noise scale (100.0-2000.0), depth factors
4. **Ore Distribution**: Threshold-based rarity (Diamond: 8%, Iron: 30%, Copper: 35%)
5. **Surface Control**: Complete block override system with prevention flags

#### Technical Implementation:
- **PlanetMaker.java**: Core builder class with comprehensive parameter control
- **PlanetGenerationRunner.java**: Example configurations and planet setup
- **Generated JSON files**: Automatic creation of dimension and noise settings
- **Ore generation**: Noise-based system using proper threshold validation

### 🔄 Current Phase: Documentation and Polish
**Status**: 🔄 In Progress

#### Next Priorities:
1. **Documentation Updates**: Complete planetary generation documentation
2. **Performance Optimization**: Optimize chunk generation for complex planets
3. **Visual Enhancements**: Improve sky rendering and atmospheric effects
4. **Resource Balancing**: Fine-tune ore distribution and terrain features

**For detailed TerraBlender information, see [TERRABLENDER_INTEGRATION.md](./TERRABLENDER_INTEGRATION.md)**
**For celestial configuration, see [CELESTIAL_CONFIGURATION.md](./CELESTIAL_CONFIGURATION.md)**

### Development Tools & Commands
```bash
# Build and testing
./gradlew clean build                  # Clean build (recommended after integration changes)
./gradlew runClient                    # Launch development client
./gradlew runData                      # Test mod loading and integrations

# Planet generation and testing
# Run PlanetGenerationRunner to generate all planets
# Set OVERWRITE_EXISTING = true to regenerate existing planets

# Planet commands (in-game)
/planet list                          # View all generated planets
/planet teleport moon                 # Travel to Moon (craterous lunar landscape)
/planet teleport mars                 # Travel to Mars (dramatic Martian terrain)
/planet teleport venus                # Travel to Venus (toxic sulfuric atmosphere)
/planet teleport glacio               # Travel to Glacio (icy frozen world)
/planet info <planet_name>           # View detailed planet characteristics

# Time progression testing
/time set day                          # Test day cycle
/time set night                        # Test night cycle
```

## Planet Generation System

### Architecture Overview

**Single Source of Truth: `PlanetGenerationRunner.configurePlanets()`**

The planet system uses a centralized registry architecture where planets are defined once and automatically generate both JSON files and dimension effects:

```
PlanetGenerationRunner (Java Code - Single Definition)
         ↓
   registerPlanet(id) → Static Registry Map
         ↓                         ↓
    .generate() → JSON         DimensionEffectsHandler → Auto-generated fallback effects
```

**Key Benefits:**
- ✅ Define planets once in Java - no manual duplication
- ✅ Automatic JSON generation for world data
- ✅ Automatic dimension effects fallback (clouds, fog, atmosphere)
- ✅ Adding new planets requires changes in only ONE place

### PlanetMaker API Usage

**IMPORTANT:** Always use `registerPlanet(id)` instead of `PlanetMaker.planet(id)` to ensure automatic dimension effects registration.

```java
// Example: Mars with vanilla-quality mountainous terrain
registerPlanet("mars")
    .vanillaQualityMountainous()       // Dramatic terrain preset
    .coordinateShift(15000, 15000)     // Unique terrain offset
    .gravity(0.38f)                    // Mars has 3/8 Earth gravity
    .temperature(-65)                  // Mars average surface temperature

    // Surface blocks
    .surfaceBlock("adastramekanized:mars_sand")
    .subsurfaceBlock("adastramekanized:mars_stone")
    .defaultBlock("adastramekanized:mars_stone")
    .bedrockBlock("minecraft:bedrock")

    // World configuration
    .seaLevel(0)
    .aquifersEnabled(false)            // Dry desert planet

    // Atmosphere and visuals
    .skyColor(0xD2691E)                // Orange-brown Martian sky
    .fogColor(0xCD853F)                // Sandy fog
    .hasAtmosphere(true)               // Has atmosphere (not breathable)
    .breathableAtmosphere(false)       // Requires oxygen gear
    .ambientLight(0.2f)                // Dim ambient light

    // Weather and celestial
    .cloudsEnabled(false)              // No clouds
    .rainEnabled(false)                // No rain
    .starsVisibleDuringDay(false)      // Stars only at night

    // Ore generation
    .oreVeinsEnabled(true)             // Enable vanilla ore system
    .configureOre("redstone", 25)      // Thermal energy deposits
    .configureOre("osmium", 15)        // Mekanism ore
    .configureOre("ostrum", 5)         // Progression ore

    // Biomes
    .clearBiomes()
    .addBiome("adastramekanized:mars_highlands", ...)
    .addBiome("adastramekanized:mars_canyons", ...)

    .generate();                       // Adds to generation queue AND registry
```

### Vanilla-Quality Terrain System

The mod supports three terrain generation modes:

1. **`useIdenticalVanillaTerrain()`** - Byte-for-byte identical to Overworld (no uniqueness)
2. **`useIdenticalVanillaTerrain()` + `coordinateShift()`** - Legacy simplified terrain (not vanilla quality)
3. **`useVanillaQualityTerrain()`** - **RECOMMENDED** Full vanilla density function set with coordinate shifting

#### How Vanilla-Quality Terrain Works

The vanilla-quality system copies Minecraft's complete terrain algorithm:
- **offset.json** (60KB) - Massive spline lookup table for terrain height offset
- **factor.json** (34KB) - Spline lookup for terrain distribution
- **jaggedness.json** (11KB) - Spline lookup for mountain peak sharpness
- **sloped_cheese.json** - Core terrain formula: `4 * quarter_negative((depth + jaggedness * half_negative(jagged)) * factor) + base_3d_noise`

All spline references are replaced from `minecraft:overworld/` to `adastramekanized:{planet}/`, and coordinate shifting produces unique terrain per planet.

#### Terrain Generation Modes Comparison

| Mode | Quality | Uniqueness | Use Case |
|------|---------|------------|----------|
| `useIdenticalVanillaTerrain()` | Vanilla | None (identical to Overworld) | Testing |
| Legacy + `coordinateShift()` | Simplified | Unique but simplified | Not recommended |
| `useVanillaQualityTerrain()` | Vanilla | Unique per planet | **Production planets** |

#### Vanilla-Quality Presets

```java
// Standard Overworld terrain (default)
.vanillaQualityStandard()

// Flat terrain with gentle rolling hills
.vanillaQualityFlat()

// Dramatic peaks and valleys
.vanillaQualityMountainous()

// Otherworldly terrain feel
.vanillaQualityAlien()

// Moon-style crater terrain
.vanillaQualityCratered()

// Island chains and water bodies
.vanillaQualityArchipelago()
```

#### Configurable Terrain Parameters

| Parameter | Default | Method | Effect |
|-----------|---------|--------|--------|
| `terrainFactor` | 4.0 | `.slopedCheeseMultiplier(float)` | Overall terrain scale (higher = more dramatic) |
| `jaggednessNoiseScale` | 1500.0 | `.jaggedNoiseScale(float)` | Mountain peak spacing (lower = closer peaks) |
| `base3DNoiseXZScale` | 0.25 | `.base3dNoiseScale(xz, y)` | Horizontal terrain frequency |
| `base3DNoiseYScale` | 0.125 | `.base3dNoiseScale(xz, y)` | Vertical terrain frequency |
| `base3DNoiseXZFactor` | 80.0 | `.base3dNoiseFactor(xz, y)` | Horizontal terrain amplitude |
| `base3DNoiseYFactor` | 160.0 | `.base3dNoiseFactor(xz, y)` | Vertical terrain amplitude |
| `smearScaleMultiplier` | 8.0 | `.base3dNoiseSmear(float)` | Terrain smoothing (lower = sharper) |
| `coordinateShiftX/Z` | auto | `.coordinateShift(x, z)` | Noise space offset for uniqueness |

#### Preset Parameter Values

| Preset | terrainFactor | jaggedScale | xzFactor | smear |
|--------|---------------|-------------|----------|-------|
| Standard | 4.0 | 1500.0 | 80.0 | 8.0 |
| Flat | 2.0 | 2000.0 | 40.0 | 8.0 |
| Mountainous | 6.0 | 1000.0 | 120.0 | 8.0 |
| Alien | 5.0 | 1200.0 | 60.0 | 6.0 |
| Cratered | 3.0 | 800.0 | 100.0 | 4.0 |
| Archipelago | 4.0 | 1500.0 | 50.0 | 8.0 |

#### Example: Moon with Vanilla-Quality Terrain

```java
registerPlanet("moon")
    .vanillaQualityCratered()          // Use cratered terrain preset
    .coordinateShift(5000, 5000)       // Unique terrain location
    .surfaceBlock("adastramekanized:moon_sand")
    .defaultBlock("adastramekanized:moon_stone")
    .gravity(0.166f)
    .hasAtmosphere(false)
    .generate();
```

#### Generated Density Function Files

When using `useVanillaQualityTerrain()`, these files are generated per planet:
```
worldgen/density_function/{planet}/
├── continents.json        # Coordinate-shifted continentalness
├── erosion.json           # Coordinate-shifted erosion
├── ridges.json            # Coordinate-shifted ridges
├── ridges_folded.json     # Folded ridges reference
├── offset.json            # Full vanilla spline (60KB)
├── factor.json            # Full vanilla spline (34KB)
├── jaggedness.json        # Full vanilla spline (11KB)
├── depth.json             # Y-gradient + offset
├── base_3d_noise.json     # Configurable 3D noise
└── sloped_cheese.json     # Core terrain formula
```

### Available Planets (31 Total)

The mod includes 31 pre-configured planets in `PlanetGenerationRunner.configurePlanets()`:

#### Solar System
1. **moon** - Airless lunar landscape with Earth visible, lava oceans, 1/6 gravity
2. **earth_orbit** - Space station dimension, void world with Earth below
3. **mars** - Red planet with canyons, thin atmosphere, two moons (Phobos & Deimos)
4. **venus** - Thick toxic atmosphere, heavy fog, yellowish sky
5. **mercury** - Airless metallic world, extreme temperatures

#### Jovian Moons
6. **europa** - Icy moon of Jupiter
7. **io** - Volcanic moon of Jupiter
8. **ganymede** - Largest moon in the solar system
9. **callisto** - Heavily cratered Jovian moon

#### Outer Solar System
10. **titan** - Saturn's largest moon with thick atmosphere
11. **enceladus** - Icy Saturn moon with geysers
12. **triton** - Neptune's retrograde moon
13. **ceres** - Dwarf planet in the asteroid belt
14. **pluto** - Dwarf planet at the edge of the solar system
15. **eris** - Distant dwarf planet

#### Exoplanets
16. **kepler22b** - First confirmed habitable-zone exoplanet
17. **kepler442b** - Super-Earth in habitable zone
18. **proxima_b** - Nearest exoplanet to Earth
19. **trappist1e** - TRAPPIST-1 system habitable zone planet
20. **gliese667c** - Triple-star system exoplanet

#### Original Planets
21. **pyrios** - Extreme volcanic world
22. **frigidum** - Ultra-cold frozen world
23. **arenos** - Arid desert world
24. **paludis** - Swamp/wetland world
25. **luxoria** - Bioluminescent exotic world
26. **glacio** - Icy world with thin atmosphere, snow and ice
27. **vulcan** - Volcanic rocky world
28. **terra_nova** - Earth-like habitable world
29. **primordium** - Primordial ancient world
30. **bellator** - War-torn hostile world
31. **profundus** - Deep ocean/cave world

### Automated Dimension Effects System

**How it works:**
When `registerPlanet(id)` is called, the planet builder is stored in a static registry. During client startup, if planet JSON files haven't loaded yet, `DimensionEffectsHandler` automatically generates dimension effects from the registry:

```java
// In PlanetGenerationRunner - planet is registered
registerPlanet("neptune")
    .hasAtmosphere(false)
    .cloudsEnabled(false)
    .generate();

// In DimensionEffectsHandler - effects auto-generated from builder properties:
// - Cloud height: Float.NaN (no clouds, because cloudsEnabled=false)
// - Fog: None (no atmosphere)
// - No manual createNeptuneEffects() method needed!
```

**Properties automatically used for dimension effects:**
- `hasAtmosphere()` → Controls fog rendering
- `cloudsEnabled()` → Sets cloud height (192.0 or NaN)
- `rainEnabled()` → Controls precipitation
- `skyColor()` → Sky rendering color
- `fogColor()` → Fog rendering color

### Ore Generation System

The noise-based ore generation uses threshold-based rarity controlled by `configureOre()`:

```java
.configureOre("diamond", 4)    // 4 diamond veins per chunk
.configureOre("iron", 50)      // 50 iron veins per chunk
.configureOre("gold", 3)       // 3 gold veins per chunk
```

**Threshold-based rarity (for noise-based generation):**
- **Diamond**: 0.92-1.0 threshold (8% spawn chance)
- **Gold**: 0.85-1.0 threshold (15% spawn chance)
- **Iron**: 0.7-1.0 threshold (30% spawn chance)
- **Copper**: 0.65-1.0 threshold (35% spawn chance)

Ore distribution is controlled by `minecraft:ore_gap` noise with proper depth conditions.

### Adding a New Planet

**Complete workflow (single location!):**

```java
// In PlanetGenerationRunner.configurePlanets()
registerPlanet("neptune")
    .gravity(1.1f)
    .surfaceBlock("minecraft:blue_ice")
    .skyColor(0x0066FF)
    .hasAtmosphere(true)
    .cloudsEnabled(true)
    .starsVisibleDuringDay(false)
    .generate();

// That's it! The planet will:
// 1. Generate JSON files during ./gradlew makePlanets
// 2. Auto-register dimension effects (clouds, fog, atmosphere)
// 3. Be available via /planet teleport neptune
```

### Planet Configuration Examples

#### Airless World (Moon-style)
```java
registerPlanet("titan")
    .hasAtmosphere(false)          // No atmosphere
    .cloudsEnabled(false)          // No clouds
    .rainEnabled(false)            // No rain
    .starsVisibleDuringDay(true)   // Stars always visible
    .starCount(50000)              // Dense starfield
    .starBrightness(2.5f)          // Very bright stars
    .skyColor(0x000000)            // Black space
    .ambientLight(0.0f)            // No ambient light
    .generate();
```

#### Earth-like World
```java
registerPlanet("habitable_world")
    .hasAtmosphere(true)           // Breathable atmosphere
    .cloudsEnabled(true)           // Show clouds
    .rainEnabled(true)             // Allow rain/weather
    .starsVisibleDuringDay(false)  // Stars only at night
    .skyColor(0x87CEEB)            // Sky blue
    .ambientLight(0.8f)            // Bright ambient light
    .generate();
```

#### Icy World (Glacio)
- Vanilla-quality standard terrain with coordinate shift (60000, 90000)
- Gravity: 0.8, Temperature: -50
- Surface: Snow blocks, ice, packed ice
- Sea level: 50 with water oceans
- Ores: Coal (25), Silver (20), Calorite (3), Etrium (3)
- Atmosphere: Breathable, clouds enabled, snow enabled, no rain

## Project Documentation References

- `README.md` - Project overview and current implementation status
- `IMPLEMENTATION_ROADMAP.md` - Future development roadmap and planned features
- `DEVELOPMENT_HISTORY.md` - Complete development history and lessons learned
- `TERRABLENDER_INTEGRATION.md` - TerraBlender integration guide and configuration
- `CELESTIAL_CONFIGURATION.md` - Celestial bodies and sky customization guide
- Additional working directories with reference implementations:
  - `/home/keroppi/Development/Minecraft/Ad-Astra/` (original Ad Astra reference)
  - `/home/keroppi/Development/Minecraft/Mekanism/` (Mekanism reference)
- always update planetGenerationRunner when adding/editing planet generation
- always ensure gradle makePlanets is updated to generate proper changes when we alter planets and biom generation
- do not directly modify the generated files of planets, modify planetGenerationRunner so when gradle makePlanets is run it update properly
- consult existing terrain documentation every time before modifying planet terrain/bioms