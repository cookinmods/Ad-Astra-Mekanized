# State of Planet Terrain & Structure Generation

> **Date**: 2026-02-27
> **Purpose**: Comprehensive audit of terrain generation, structure placement, external dependencies, and actionable improvements.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Terrain Generation Pipeline](#2-terrain-generation-pipeline)
3. [External Dependencies Audit](#3-external-dependencies-audit)
4. [Structure Generation System](#4-structure-generation-system)
5. [Cave Generation](#5-cave-generation)
6. [Ore Generation](#6-ore-generation)
7. [Known Issues](#7-known-issues)
8. [Recommendations](#8-recommendations)

---

## 1. Architecture Overview

### Single Source of Truth

All 31 planets are defined in **`PlanetGenerationRunner.configurePlanets()`**. The `makePlanets` Gradle task executes this and generates all JSON files. **No JSON files should be hand-edited** — they are regenerated from scratch each run.

### Files Generated Per Planet

```
planets/{planet}.json                              # Planet metadata
dimension/{planet}.json                            # Dimension definition (generator + biome source)
dimension_type/{planet}.json                       # Dimension type (height, light, ceiling, etc.)
worldgen/noise_settings/{planet}.json              # Noise router + surface rules
worldgen/density_function/{planet}/                # 10-12 density function files:
    continents.json                                #   Coordinate-shifted continentalness
    erosion.json                                   #   Coordinate-shifted erosion
    ridges.json, ridges_folded.json                #   Coordinate-shifted ridges
    offset.json (60KB)                             #   Full vanilla height spline
    factor.json (34KB)                             #   Full vanilla factor spline
    jaggedness.json (11KB)                         #   Full vanilla jaggedness spline
    depth.json                                     #   Y-gradient + offset reference
    base_3d_noise.json                             #   Configurable 3D noise
    sloped_cheese.json                             #   Core terrain formula
    final_density.json                             #   Terrain + cave composition
worldgen/biome/{planet}_*.json                     # 1-N biome definitions
worldgen/configured_feature/{planet}_ore_*.json    # Ore features
worldgen/placed_feature/{planet}_ore_*.json        # Ore placements
neoforge/biome_modifier/{planet}/*.json            # Mob spawns, IE ores
tags/worldgen/biome/{planet}_biomes.json           # Biome grouping tag
```

External mod tags are also generated:
```
kobolds/tags/worldgen/biome/kobold_den_biomes.json
kobolds/worldgen/structure/kobold_den.json         # Structure override
ribbits/tags/worldgen/biome/has_structure/ribbit_village.json
dungeons_arise/tags/worldgen/biome/has_structure/*.json
dungeons_arise_seven_seas/tags/worldgen/biome/has_structure/*.json
```

### Generation Flow

```
./gradlew makePlanets
  → Cleans all generated worldgen files
  → Runs PlanetGenerationRunner.main()
    → configurePlanets() registers all 31 planets via registerPlanet()
    → PlanetMaker.generateAllPlanets() iterates and calls per-planet:
        generatePlanetData()
        generateDimensionData()
        generateDimensionType()
        generateCustomNoiseFiles()        # Coordinate-shifted noise references
        generateCustomDensityFunctions()  # 10-12 density function JSONs
        generateNoiseSettings()           # Noise router, surface rules
        generateOreFeatures()             # Standard ores
        generateConditionalOreFeatures()  # IE ores (mod_loaded conditions)
        generateCustomBiomes()            # Biome definitions with carvers/features
        generateBiomeTag()                # Planet biome grouping tag
        generateBiomeModifier()           # NeoForge mob spawn modifiers
        generateConditionalOreBiomeModifiers()
        generateStructureBiomeTags()      # External mod structure biome tags
        generatePatchouliEntry()          # In-game guidebook page
```

---

## 2. Terrain Generation Pipeline

### Vanilla-Quality Terrain System

The mod copies Minecraft's complete terrain algorithm with per-planet coordinate shifting. The core formula (in `sloped_cheese.json`):

```
terrain = terrainFactor * quarter_negative(
    (depth + jaggedness * half_negative(jagged)) * factor
) + base_3d_noise
```

**Coordinate shifting** makes each planet unique: noise inputs (continents, erosion, ridges) are sampled at `(x + shiftX, z + shiftZ)` instead of `(x, z)`. Same algorithm, different location in noise space = different terrain.

### Available Presets

| Preset | terrainFactor | jaggedScale | xzFactor | smear | Terrain Character |
|--------|--------------|-------------|----------|-------|-------------------|
| `vanillaQualityStandard()` | 4.0 | 1500.0 | 80.0 | 8.0 | Overworld baseline |
| `vanillaQualityFlat()` | 2.0 | 2000.0 | 40.0 | 8.0 | Gentle rolling hills |
| `vanillaQualityMountainous()` | 6.0 | 1000.0 | 120.0 | 8.0 | Dramatic peaks/valleys |
| `vanillaQualityAlien()` | 5.0 | 1200.0 | 60.0 | 6.0 | Otherworldly feel |
| `vanillaQualityCratered()` | 3.0 | 800.0 | 100.0 | 4.0 | Moon-like craters |
| `vanillaQualityArchipelago()` | 4.0 | 1500.0 | 50.0 | 8.0 | Island chains |

### Dual Terrain Path (Historical)

```
Path 1: useVanillaQualityTerrain() [CURRENT - All 31 planets use this]
  → Full vanilla spline system + coordinate shift
  → Generates all density function JSONs per planet
  → Produces unique vanilla-quality terrain

Path 2: useIdenticalVanillaTerrain() [LEGACY - Not used by any planet]
  → Direct reference to minecraft:overworld noise settings
  → No custom density functions
```

### Surface Rules

Generated in `generateNoiseSettings()` as a `minecraft:sequence` of conditions:
1. **Bedrock**: Y-gradient from -64 to -59
2. **Surface block**: Top layer (e.g., moon_sand), 1-6 blocks deep
3. **Subsurface block**: Below surface layer
4. **Deepslate transition**: Y-gradient from -8 to 0
5. **Default block**: Fills everything else (e.g., moon_stone)

### Dimension Type Configuration

Each planet gets a dimension_type JSON with:
- `min_y: -64`, `height: 384` (standard -64 to 320 range)
- `has_skylight` / `has_ceiling` per planet atmosphere
- `ambient_light` from planet config
- `monster_spawn_light_level` / `monster_spawn_block_light_limit`
- `natural: true` (time progresses)

---

## 3. External Dependencies Audit

### Tectonic + Lithostitched: UNUSED — Safe to Remove

**Current status**: Listed as `type = "required"` in `neoforge.mods.toml` and `implementation` in `build.gradle`.

**Reality**: **Zero planets use Tectonic terrain generation.** The infrastructure exists but was never activated:

| Component | File | Status |
|-----------|------|--------|
| `TectonicNeoForgeIntegration.java` | `worldgen/integration/` | Registers `config_constant` and `config_noise` density function types. **Never referenced in any generated JSON.** |
| `PlanetConfigHandler.java` | `worldgen/densityfunction/` | Stores config values for moon/mars. **Never queried by any density function.** |
| `ConfigConstant.java` | `worldgen/densityfunction/` | Custom density function type. **Never used in JSON.** |
| `ConfigNoise.java` | `worldgen/densityfunction/` | Custom density function type. **Never used in JSON.** |
| `NoiseRouterBuilder.java` | `worldgen/builder/` | Builds Tectonic noise routers. **Never called.** |
| `TectonicNoiseConfig.java` | `worldgen/config/` | Configuration class. **Never instantiated by planet code.** |
| `withTectonicGeneration()` | PlanetMaker.java | Method exists. **Never called by any planet.** |

**Impact of removal**:
- **Breaks**: Compilation of 6 unused Java files, dependency declarations in mods.toml/build.gradle
- **Does NOT break**: Any planet generation, terrain, biomes, ores, mobs, structures, or gameplay

**Files to remove**:
```
src/main/java/.../worldgen/integration/TectonicNeoForgeIntegration.java
src/main/java/.../worldgen/densityfunction/ConfigConstant.java
src/main/java/.../worldgen/densityfunction/ConfigNoise.java
src/main/java/.../worldgen/densityfunction/PlanetConfigHandler.java
src/main/java/.../worldgen/config/TectonicNoiseConfig.java
src/main/java/.../worldgen/builder/NoiseRouterBuilder.java
```

**Config to update**:
```
neoforge.mods.toml  → Remove tectonic + lithostitched dependency blocks
build.gradle        → Remove tectonic + lithostitched implementation lines
build.gradle.kts    → Remove tectonic + lithostitched implementation lines
AdAstraMekanized.java → Remove TectonicNeoForgeIntegration.initialize() call
PlanetMaker.java    → Remove withTectonicGeneration(), useTectonicGeneration flag,
                       all tectonic-related builder parameters (lines 643-712)
```

**Benefits of removal**:
- Users no longer need to install Tectonic + Lithostitched (2 fewer required mods)
- Faster compilation (fewer files)
- Cleaner codebase (removes ~500 lines of dead code)
- No more confusion about "required" dependencies that do nothing

---

## 4. Structure Generation System

### How Structures Work in Custom Dimensions

Minecraft's structure generation pipeline:
```
1. Chunk generator starts generating a chunk
2. For each structure_set in the registry:
   a. Check placement rules (spacing, separation, salt)
   b. If this chunk is a valid placement position:
      c. Check structure's biome filter against chunk's biome
      d. If biome matches: place structure
      e. Apply terrain_adaptation to modify density around structure
3. Generate terrain blocks using density functions
4. Place structure blocks into terrain
5. Run carvers (caves, canyons)
6. Place features (ores, vegetation, dungeons)
```

**Key insight**: Structure placement depends on the chunk's biome matching the structure's `biomes` field. Structure sets are **global** — they run in ALL dimensions with a `minecraft:noise` generator.

### Current Modded Structure Integration

The mod integrates structures from external mods via **biome tag overrides**:

| Mod | Structure | Override Method | Planets |
|-----|-----------|-----------------|---------|
| Kobolds | `kobold_den` | Override `kobold_den.json` to use `#kobolds:kobold_den_biomes` | Moon, Callisto, Ceres, Glacio, Profundus |
| Ribbits | `ribbit_village` | Add biomes to `#ribbits:has_structure/ribbit_village` | Terra Nova, Paludis |
| WhenDungeonsArise | Various | Add biomes to type-specific tags | Multiple planets |
| Seven Seas | Ship structures | Add biomes to ocean tags | Ocean planets |

### ModdedStructureController

`ModdedStructureController.java` provides a **runtime whitelist** system:
- Controlled mods: `kobolds`, `ribbits`, `born_in_chaos_v1`
- `whitelistModStructures(dimension, modId)` — Allow all structures from a mod in a dimension
- `whitelistStructure(dimension, structureId)` — Allow specific structures
- `isStructureAllowed(dimension, structureId)` — Check if permitted

**Question**: Does this controller actually intercept structure placement events, or does it only manage biome tags? If it intercepts placement, it could be blocking structures even when biome tags are correct.

### Kobold Den Issue (Active Bug)

**Symptom**: Kobold dens don't appear on the moon despite biome tag fixes and structure override.

**What we've done**:
1. Override `kobold_den.json` biomes from `#minecraft:is_overworld` → `#kobolds:kobold_den_biomes` ✓
2. Include `#minecraft:is_overworld` in `kobold_den_biomes` to preserve overworld spawning ✓
3. Add all 5 kobold planet biomes to `kobold_den_biomes` ✓
4. Changed `terrain_adaptation` from `encapsulate` → `beard_box` ✓

**Possible root causes still unresolved**:
1. **`ModdedStructureController` event interception** — If the controller blocks structure placement via an event handler, the biome fix alone won't work. Need to verify the controller isn't blocking.
2. **Custom structure type `kobolds:kobold_den`** — This is a custom structure type registered by the kobolds mod. It may have additional biome/dimension checks in its Java code that we can't override via JSON.
3. **Structure set dimension scoping** — In some NeoForge versions, structure sets may only run in dimensions whose noise settings match certain criteria.
4. **Terrain density too high** — At Y=12-32 on the moon, the density gradient produces ~0.9 (very solid). Even `beard_box` might not carve enough space if the density is overwhelmingly positive.
5. **The structure spawns but is completely invisible** — Structure materials (stone bricks) might be replaced by surface rules or default block placement.

**Recommended investigation**:
```
1. /locate structure kobolds:kobold_den     # Does it find one?
2. If found, teleport and dig down           # Is it buried in solid blocks?
3. Use spectator mode to phase through       # Can you see the structure?
4. Check logs for kobold den placement info  # Does the mod log placement?
```

### No Custom Structures Defined

The mod defines **zero** custom structures or structure sets:
- `data/adastramekanized/worldgen/structure/` — Does not exist
- `data/adastramekanized/worldgen/structure_set/` — Does not exist

All structures come from external mods or vanilla. The mod only manipulates biome tags to enable/disable external structures per planet.

---

## 5. Cave Generation

### Carvers (Block Removal)

Biomes with `enableVanillaUndergroundFeatures` get vanilla air carvers:
```json
"carvers": {
  "air": [
    "minecraft:cave",                    // Standard caves
    "minecraft:cave_extra_underground",  // Deep caves
    "minecraft:canyon"                   // Ravines
  ]
}
```

These run AFTER terrain generation and carve air pockets into solid blocks.

### Density Function Caves (in `final_density.json`)

The `final_density` function composes terrain with cave systems using `minecraft:min`:

```
final_density = min(
    squeeze(0.64 * interpolated(blend_density(terrain_with_caves))),
    overworld/caves/noodle
)
```

Where `terrain_with_caves` uses `range_choice` on `sloped_cheese`:
- **When sloped_cheese < 1.5625** (solid terrain region): Apply cave entrances
- **When sloped_cheese >= 1.5625** (underground region): Apply full cave system:
  - Cave layer noise (large caverns)
  - Cave cheese noise (cheese caves, threshold 0.27)
  - Spaghetti 2D caves
  - Cave entrances
  - Pillar generation

### Configurable Cave Parameters

```java
.caveFrequency(0.5f)          // 0.0 = no caves, 1.0 = maximum caves
.cheeseCaveThreshold(0.27f)   // Higher = fewer cheese caves
.enableCheeseCaves(true)      // Large open caverns
.enableNoodleCaves(true)      // Thin winding tunnels
```

**Note**: The configurable threshold adjusts the cheese cave additive value:
`threshold = 0.27 + (1.0 - caveFrequency) * 0.7`
- At caveFrequency=0.0: threshold=0.97 (almost no caves)
- At caveFrequency=1.0: threshold=0.27 (vanilla amount)

### Cave Status by Planet Type

All planets using vanilla-quality terrain get vanilla caves via both carvers and density functions. There is no way to disable density-function caves independently — they're baked into `final_density.json`.

---

## 6. Ore Generation

### Standard Ores

Configured via `.configureOre(type, veinsPerChunk)`:
```java
.configureOre("iron", 10)       // 10 veins/chunk, size 9, Y=-64 to 48
.configureOre("diamond", 3)     // 3 veins/chunk, size 4, Y=-64 to 16
.configureOre("osmium", 15)     // 15 veins/chunk, Mekanism ore
```

Generates two files per ore:
- `configured_feature/{planet}_ore_{type}_simple.json` — Block targets + vein size
- `placed_feature/{planet}_ore_{type}_simple.json` — Count, height range, square spread

Ores are placed as features in biome JSONs under `underground_ores` step.

### Conditional IE Ores

For Immersive Engineering ores (silver, nickel):
- Generates `{planet}_ore_{type}_ie.json` with `neoforge:mod_loaded` condition
- Creates biome modifiers:
  - `ie_add_ores.json` — Adds IE ore features (when IE installed)
  - `ie_remove_fallback_ores.json` — Removes vanilla fallback (when IE installed)

### Ore Target Blocks

Ores replace blocks matching these tags:
- `minecraft:stone_ore_replaceables` — Stone variants
- `minecraft:deepslate_ore_replaceables` — Deepslate variants
- Custom planet stone variants added to these tags

---

## 7. Known Issues

### P0: Kobold Dens Not Spawning on Planets
- Structure biome filter fixed, terrain adaptation changed to `beard_box`
- Still not appearing in-game
- May require deeper investigation into structure placement pipeline or `ModdedStructureController`

### P1: Tectonic/Lithostitched Required But Unused
- Declared as `type = "required"` in mods.toml
- Users must install 2 mods that provide zero functionality
- All 31 planets use vanilla-quality terrain, not Tectonic

### P2: `final_density.json` References Overworld Cave Functions
- All planets reference `minecraft:overworld/caves/*` density functions
- This means planet caves are identical to Overworld caves
- No per-planet cave customization despite `caveFrequency` parameter being in the API
- The `caveFrequency` only adjusts the cheese cave threshold, not the actual cave density function references

### P3: No Custom Structure Support
- Mod has zero custom structures or structure sets
- All structures come from external mods
- No fallback structures if external mods aren't installed
- Planets without external structure mods have only vanilla dungeons

### P4: Terrain Density at Underground Structure Levels
- Moon (and similar airless worlds with `vanillaQualityFlat()`) has very high terrain density at Y=12-32
- This creates solid rock with minimal cave pockets at structure placement levels
- `beard_box` terrain adaptation may not create sufficient clearance

---

## 8. Recommendations

### Immediate: Remove Tectonic/Lithostitched Dependencies

**Effort**: Low (delete files, update configs)
**Impact**: High (2 fewer required mods for users)

1. Delete 6 unused Java files in `worldgen/`
2. Remove dependency blocks from `neoforge.mods.toml`
3. Remove implementation lines from `build.gradle` and `build.gradle.kts`
4. Remove `TectonicNeoForgeIntegration.initialize()` from `AdAstraMekanized.java`
5. Clean dead Tectonic parameters from `PlanetMaker.java`

### Short-term: Investigate Structure Placement Pipeline

**Effort**: Medium
**Impact**: Fixes kobold dens and potentially all modded structures

1. Add debug logging to `ModdedStructureController` to trace structure placement decisions
2. Test with `/locate structure kobolds:kobold_den` on moon
3. If `/locate` fails: the structure set isn't running in the dimension — investigate why
4. If `/locate` succeeds but structure is buried: terrain adaptation issue
5. Consider creating a custom structure set wrapper that explicitly targets planet dimensions

### Medium-term: Per-Planet Cave Customization

**Effort**: Medium-High
**Impact**: Unique underground experience per planet

Currently all planets reference `minecraft:overworld/caves/*`. To customize:
1. Generate planet-specific `caves/entrances.json`, `caves/spaghetti_2d.json`, etc.
2. Reference planet-specific functions in `final_density.json`
3. Use `caveFrequency` parameter to scale noise values
4. Airless worlds (Moon, Mercury) should have fewer/no caves
5. Cave worlds (Profundus) should have amplified caves

### Long-term: Custom Structure System

**Effort**: High
**Impact**: Rich planet-specific content without external mod dependencies

Options:
1. **Jigsaw structures** — Define custom planet structures (alien ruins, crashed ships, mining outposts)
2. **Structure set wrappers** — Create per-dimension structure sets that explicitly target planets
3. **Processor lists** — Planet-specific block replacement for existing structures (e.g., kobold dens use moon_stone instead of stone)

### Long-term: Remove Vanilla Cave References

**Effort**: Medium
**Impact**: True per-planet underground variety

Replace hardcoded `minecraft:overworld/caves/*` references in `final_density.json` with planet-specific cave density functions. This would allow:
- Airless worlds with lava tubes instead of water caves
- Ice worlds with ice caverns
- Volcanic worlds with magma chambers
- Deep worlds with massive cheese caves

---

## Appendix: Planet Terrain Presets In Use

| Planet | Preset | Coord Shift | Caves |
|--------|--------|-------------|-------|
| Moon | `vanillaQualityFlat` | 5000, 5000 | Yes |
| Mars | `vanillaQualityMountainous` | 15000, 15000 | Yes |
| Venus | `vanillaQualityFlat` | 30000, 30000 | Yes |
| Mercury | `vanillaQualityCratered` | 45000, 30000 | Yes |
| Europa | `vanillaQualityFlat` | 60000, 30000 | Yes |
| Io | `vanillaQualityMountainous` | 75000, 30000 | Yes |
| Ganymede | `vanillaQualityStandard` | 90000, 30000 | Yes |
| Callisto | `vanillaQualityCratered` | 30000, 45000 | Yes |
| Titan | `vanillaQualityAlien` | 45000, 45000 | Yes |
| Enceladus | `vanillaQualityCratered` | 60000, 45000 | Yes |
| Triton | `vanillaQualityFlat` | 75000, 45000 | Yes |
| Ceres | `vanillaQualityCratered` | 90000, 45000 | Yes |
| Pluto | `vanillaQualityFlat` | 30000, 60000 | Yes |
| Eris | `vanillaQualityFlat` | 45000, 60000 | Yes |
| Kepler-22b | `vanillaQualityArchipelago` | 60000, 60000 | Yes |
| Kepler-442b | `vanillaQualityStandard` | 75000, 60000 | Yes |
| Proxima B | `vanillaQualityAlien` | 90000, 60000 | Yes |
| TRAPPIST-1e | `vanillaQualityStandard` | 30000, 75000 | Yes |
| Gliese-667c | `vanillaQualityMountainous` | 45000, 75000 | Yes |
| Pyrios | `vanillaQualityMountainous` | 60000, 75000 | Yes |
| Frigidum | `vanillaQualityFlat` | 75000, 75000 | Yes |
| Arenos | `vanillaQualityFlat` | 90000, 75000 | Yes |
| Paludis | `vanillaQualityFlat` | 30000, 90000 | Yes |
| Luxoria | `vanillaQualityAlien` | 45000, 90000 | Yes |
| Glacio | `vanillaQualityStandard` | 60000, 90000 | Yes |
| Vulcan | `vanillaQualityCratered` | 75000, 90000 | Yes |
| Terra Nova | `vanillaQualityStandard` | 90000, 90000 | Yes |
| Primordium | `vanillaQualityStandard` | 105000, 90000 | Yes |
| Bellator | `vanillaQualityStandard` | 105000, 105000 | Yes |
| Profundus | `vanillaQualityAlien` | 120000, 120000 | Yes |
| Earth Orbit | Void world | N/A | No |
