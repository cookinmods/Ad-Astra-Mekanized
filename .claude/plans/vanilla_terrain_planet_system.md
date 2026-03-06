# Vanilla-Quality Planet Generation System Plan

## Executive Summary

Overhaul the PlanetMaker system to generate planets with **identical vanilla Overworld terrain** while customizing only:
- Surface and underground blocks
- Mob spawning rules
- Ore generation
- Structure generation
- Biome assignment

**Key Insight**: Instead of creating custom noise/density functions (current approach), directly reference vanilla Overworld noise router entries. This guarantees vanilla-quality terrain with full cave systems.

---

## Current System Problems

1. **Custom density functions** create non-vanilla terrain that looks "off"
2. **Missing cave systems** - current biomes have `"carvers": {"air": []}` (empty)
3. **Coordinate shifting** doesn't create distinct terrain - just samples vanilla at offset
4. **Complex noise router** creates maintenance burden and bugs
5. **No biome modifiers** - spawning/features hardcoded in biomes

## Solution Architecture

### Approach: Direct Vanilla References + Biome Modifiers

```
PlanetMaker (Java)
    ├── noise_settings/*.json    → Reference minecraft:overworld/noise_router/*
    ├── biome/*.json             → Custom biomes with vanilla carvers + features
    ├── biome_modifier/*.json    → NEW: Control spawns/features per dimension
    ├── tags/worldgen/biome/*.json → NEW: Group biomes for modifiers
    └── tags/worldgen/structure/*.json → NEW: Control structures per planet
```

---

## Implementation Plan

### Phase 1: Refactor Noise Settings Generation

**Goal**: Generate noise_settings that directly reference vanilla Overworld

**File**: `PlanetMaker.java` - Update `generateNoiseSettings()` method

**Generated noise_settings.json structure**:
```json
{
  "sea_level": 63,
  "disable_mob_generation": false,
  "aquifers_enabled": true,
  "ore_veins_enabled": true,
  "legacy_random_source": false,
  "default_block": { "Name": "adastramekanized:moon_stone" },
  "default_fluid": { "Name": "minecraft:water" },
  "noise": {
    "min_y": -64,
    "height": 384,
    "size_horizontal": 1,
    "size_vertical": 2
  },
  "noise_router": {
    "barrier": "minecraft:overworld/noise_router/barrier",
    "fluid_level_floodedness": "minecraft:overworld/noise_router/fluid_level_floodedness",
    "fluid_level_spread": "minecraft:overworld/noise_router/fluid_level_spread",
    "lava": "minecraft:overworld/noise_router/lava",
    "temperature": "minecraft:overworld/noise_router/temperature",
    "vegetation": "minecraft:overworld/noise_router/vegetation",
    "continents": "minecraft:overworld/noise_router/continents",
    "erosion": "minecraft:overworld/noise_router/erosion",
    "depth": "minecraft:overworld/noise_router/depth",
    "ridges": "minecraft:overworld/noise_router/ridges",
    "initial_density_without_jaggedness": "minecraft:overworld/noise_router/initial_density_without_jaggedness",
    "final_density": "minecraft:overworld/noise_router/final_density",
    "vein_toggle": "minecraft:overworld/noise_router/vein_toggle",
    "vein_ridged": "minecraft:overworld/noise_router/vein_ridged",
    "vein_gap": "minecraft:overworld/noise_router/vein_gap"
  },
  "surface_rule": { /* Planet-specific surface rules */ },
  "spawn_target": [ /* Vanilla spawn targets */ ]
}
```

**Customizable via PlanetMaker**:
- `default_block` - Underground stone type
- `default_fluid` - Fluid type (water or air for airless planets)
- `sea_level` - Sea level (0 for airless planets)
- `aquifers_enabled` - Water pools (false for airless)
- `surface_rule` - Generated from surface/subsurface/deep block settings

**Tasks**:
1. [ ] Remove all custom density function generation code
2. [ ] Remove density_function folder generation
3. [ ] Update noise_router to use direct vanilla references
4. [ ] Keep surface_rule generation for block customization
5. [ ] Delete existing density_function files during generation

---

### Phase 2: Update Biome Generation with Vanilla Carvers

**Goal**: Generate biomes that have proper vanilla cave carvers

**File**: `PlanetMaker.java` - Update `generateBiome()` method

**Generated biome.json structure** (example for moon_highlands):
```json
{
  "temperature": 0.5,
  "downfall": 0.0,
  "has_precipitation": false,
  "effects": {
    "fog_color": 657930,
    "sky_color": 657930,
    "water_color": 4159204,
    "water_fog_color": 329011
  },
  "spawners": {},
  "spawn_costs": {},
  "carvers": {
    "air": [
      "minecraft:cave",
      "minecraft:cave_extra_underground",
      "minecraft:canyon"
    ]
  },
  "features": [
    [],
    ["minecraft:lake_lava_underground", "minecraft:lake_lava_surface"],
    ["minecraft:amethyst_geode"],
    ["minecraft:monster_room", "minecraft:monster_room_deep"],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:ore_copper_large"
    ],
    ["minecraft:ore_infested"],
    ["minecraft:spring_water", "minecraft:spring_lava"],
    ["minecraft:glow_lichen"],
    []
  ]
}
```

**Key Changes**:
- `spawners` and `spawn_costs` are EMPTY - controlled via biome_modifiers
- `carvers` includes vanilla cave carvers
- `features` includes full vanilla underground feature set

**PlanetMaker Builder Methods**:
```java
// Enable/disable caves per planet
.cavesEnabled(true)           // Include vanilla carvers (default true)
.cavesEnabled(false)          // Remove carvers (for void/space dimensions)

// Feature control
.useVanillaOres(true)         // Include vanilla ore features
.useVanillaUnderground(true)  // Include geodes, monster rooms, etc.
.clearFeatures()              // Remove all features
.addFeature(step, "minecraft:ore_diamond")  // Add specific feature
```

**Tasks**:
1. [ ] Add `cavesEnabled` boolean to PlanetBuilder
2. [ ] Update biome generation to include vanilla carvers when enabled
3. [ ] Add vanilla underground features to feature array (steps 1-9)
4. [ ] Make spawners/spawn_costs empty (defer to biome_modifiers)
5. [ ] Add methods to control which vanilla features to include

---

### Phase 3: Implement NeoForge Biome Modifiers

**Goal**: Control mob spawning and features via biome_modifier files (like Dimension-Expansion)

**New Directory Structure**:
```
src/main/resources/data/adastramekanized/
├── neoforge/
│   └── biome_modifier/
│       ├── moon/
│       │   ├── add_spawns.json
│       │   ├── spawn_costs/
│       │   │   ├── silverfish.json
│       │   │   └── endermite.json
│       │   └── remove_features.json (optional)
│       ├── mars/
│       │   ├── add_spawns.json
│       │   └── spawn_costs/*.json
│       └── [planet]/*.json
└── tags/
    └── worldgen/
        └── biome/
            ├── moon_biomes.json
            ├── mars_biomes.json
            └── [planet]_biomes.json
```

**Biome Tag Example** (`tags/worldgen/biome/moon_biomes.json`):
```json
{
  "values": [
    "adastramekanized:moon_highlands",
    "adastramekanized:moon_maria",
    "adastramekanized:moon_crater_rim",
    "adastramekanized:moon_crater_floor",
    "adastramekanized:moon_polar"
  ]
}
```

**Spawn Modifier Example** (`neoforge/biome_modifier/moon/add_spawns.json`):
```json
{
  "type": "neoforge:add_spawns",
  "biomes": "#adastramekanized:moon_biomes",
  "spawners": [
    {
      "type": "minecraft:silverfish",
      "weight": 100,
      "minCount": 4,
      "maxCount": 8
    },
    {
      "type": "minecraft:endermite",
      "weight": 50,
      "minCount": 2,
      "maxCount": 4
    }
  ]
}
```

**Spawn Cost Example** (`neoforge/biome_modifier/moon/spawn_costs/silverfish.json`):
```json
{
  "type": "neoforge:add_spawn_costs",
  "biomes": "#adastramekanized:moon_biomes",
  "entity_types": "minecraft:silverfish",
  "spawn_cost": {
    "energy_budget": 1.0,
    "charge": 0.02
  }
}
```

**PlanetMaker Changes**:
- Generate biome tag file per planet
- Generate add_spawns.json from mob spawn configuration
- Generate spawn_costs/*.json files
- Remove mob spawn data from biome JSON (keep empty)

**Tasks**:
1. [ ] Create `generateBiomeTag()` method
2. [ ] Create `generateSpawnModifier()` method
3. [ ] Create `generateSpawnCosts()` method
4. [ ] Update `generate()` to create biome_modifier files
5. [ ] Update mob spawn API to generate modifiers instead of inline data

---

### Phase 4: Implement Structure Control via Tags

**Goal**: Per-planet control over which structures can generate

**New Directory Structure**:
```
src/main/resources/data/adastramekanized/tags/worldgen/
├── biome/
│   ├── has_structure/
│   │   ├── moon_dungeon.json       → Biomes where moon dungeons spawn
│   │   ├── mars_temple.json        → Biomes where mars temples spawn
│   │   └── [structure].json
│   └── [planet]_biomes.json
└── structure/
    └── [not needed - use biome tags]
```

**Vanilla Structure Control**:
Vanilla structures use biome tags like `#minecraft:has_structure/village_plains`. To prevent villages on the moon:
1. Moon biomes are NOT in the `minecraft:has_structure/village_*` tags
2. Custom moon structures have their own biome tags

**Biome Tag for Custom Structures** (`tags/worldgen/biome/has_structure/moon_dungeon.json`):
```json
{
  "values": [
    "adastramekanized:moon_highlands",
    "adastramekanized:moon_maria"
  ]
}
```

**Structure Definition** (`worldgen/structure/moon_dungeon.json`):
```json
{
  "type": "minecraft:jigsaw",
  "biomes": "#adastramekanized:has_structure/moon_dungeon",
  "step": "underground_structures",
  "spawn_overrides": {},
  "terrain_adaptation": "beard_thin",
  "start_pool": "adastramekanized:moon_dungeon/start",
  "size": 4,
  "start_height": { "absolute": -20 },
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "max_distance_from_center": 80,
  "use_expansion_hack": false
}
```

**PlanetMaker Changes**:
```java
// Structure control
.allowStructure("minecraft:village_plains")    // Whitelist vanilla structure
.disableAllVanillaStructures()                 // Remove from all vanilla structure tags
.addCustomStructure("moon_dungeon", biomes)    // Add custom structure with biome list
```

**Tasks**:
1. [ ] Create structure tag generation methods
2. [ ] Generate "has_structure" biome tags for custom structures
3. [ ] Add structure control API to PlanetBuilder
4. [ ] Document vanilla structure tag names for reference

---

### Phase 5: Simplify PlanetMaker API

**Goal**: Streamline the builder API to focus on what actually differs per planet

**New Simplified API**:
```java
registerPlanet("moon")
    // Core blocks
    .surfaceBlock("adastramekanized:moon_sand")
    .subsurfaceBlock("adastramekanized:moon_sand")
    .deepBlock("adastramekanized:moon_stone")
    .undergroundBlock("adastramekanized:moon_stone")  // replaces default_block
    .fluidBlock("minecraft:air")                       // for airless planets

    // World properties
    .seaLevel(0)                          // No oceans
    .aquifersEnabled(false)               // No water pools
    .cavesEnabled(true)                   // Keep vanilla caves
    .oreVeinsEnabled(true)                // Keep vanilla ore veins

    // Biomes (multi-noise source)
    .clearBiomes()
    .addBiome("adastramekanized:moon_highlands", /* climate params */)
    .addBiome("adastramekanized:moon_maria", /* climate params */)

    // OR use vanilla biomes
    .useVanillaBiome("minecraft:plains")
    .useVanillaBiome("minecraft:desert")

    // Mob spawning (generates biome_modifiers)
    .clearAllMobSpawns()
    .addMobSpawn("monster", "minecraft:silverfish", 100, 4, 8)
    .addMobSpawn("monster", "minecraft:endermite", 50, 2, 4)

    // Structure control
    .disableAllVanillaStructures()
    .allowStructure("minecraft:stronghold")  // Re-enable specific structure

    // Visual properties (unchanged)
    .skyColor(0x0A0A0A)
    .fogColor(0x0A0A0A)
    .hasAtmosphere(false)

    .generate();
```

**Remove/Deprecate**:
- All terrain noise parameters (continentalScale, erosionScale, ridgeScale, etc.)
- Coordinate shifting (not needed - identical terrain is the goal)
- Custom density function generation
- terrainTweaks() builder

**Tasks**:
1. [ ] Remove deprecated noise parameters from PlanetBuilder
2. [ ] Remove density function generation code
3. [ ] Simplify API documentation
4. [ ] Update all planet definitions in PlanetGenerationRunner

---

### Phase 6: Update Gradle makePlanets Task

**Goal**: Ensure clean generation of all required files

**Files Generated Per Planet**:
```
dimension/[planet].json
dimension_type/[planet].json
worldgen/noise_settings/[planet].json
worldgen/biome/[planet]_*.json (per biome)
planets/[planet].json
advancement/planets/visited_[planet].json
neoforge/biome_modifier/[planet]/add_spawns.json
neoforge/biome_modifier/[planet]/spawn_costs/*.json (per mob)
tags/worldgen/biome/[planet]_biomes.json
```

**Cleanup Step**:
- Delete old density_function/[planet]/ directories
- Delete old noise/*.json files
- Ensure clean state before regeneration

**Tasks**:
1. [ ] Add cleanup step to PlanetMaker.generateAllPlanets()
2. [ ] Generate biome_modifier files
3. [ ] Generate biome tags
4. [ ] Update file generation order for dependencies

---

### Phase 7: Testing and Validation

**Test Cases**:
1. [ ] Terrain matches vanilla Overworld shape exactly
2. [ ] Caves generate identically to vanilla
3. [ ] Custom surface blocks appear correctly
4. [ ] Mob spawning works via biome_modifiers
5. [ ] Vanilla structures are properly disabled
6. [ ] Custom ores generate at correct rates
7. [ ] Multi-biome planets have proper biome transitions
8. [ ] Vanilla biome planets work correctly

**Validation Commands** (in-game):
```
/planet teleport moon
/locatebiome adastramekanized:moon_highlands
/locate structure minecraft:village_plains  (should fail on moon)
/fill ~-10 ~-10 ~-10 ~10 ~10 ~10 air  (check cave generation)
```

---

## Migration Guide

### For Existing Planets

1. Remove all terrain noise customizations
2. Update surface block settings
3. Add mob spawn configurations
4. Add structure control settings
5. Run `./gradlew makePlanets`
6. Test in-game

### Breaking Changes

- `terrainTweaks()` builder removed
- `continentalScale`, `erosionScale`, `ridgeScale` removed
- `coordinateShift` removed
- `jaggedness*` parameters removed
- All density function files will be deleted

---

## File Structure Summary

```
src/main/resources/data/adastramekanized/
├── dimension/                          # Dimension definitions
│   ├── moon.json
│   └── [planet].json
├── dimension_type/                     # Dimension type configs
│   ├── moon.json
│   └── [planet].json
├── neoforge/
│   └── biome_modifier/                 # NEW: Spawning control
│       ├── moon/
│       │   ├── add_spawns.json
│       │   └── spawn_costs/*.json
│       └── [planet]/*.json
├── planets/                            # Planet metadata
│   ├── moon.json
│   └── [planet].json
├── tags/
│   └── worldgen/
│       └── biome/
│           ├── moon_biomes.json        # NEW: Biome grouping
│           └── [planet]_biomes.json
├── worldgen/
│   ├── biome/                          # Biome definitions
│   │   ├── moon_highlands.json
│   │   └── [biome].json
│   ├── configured_feature/             # Ore configs (existing)
│   ├── noise_settings/                 # Terrain + surface rules
│   │   ├── moon.json
│   │   └── [planet].json
│   └── placed_feature/                 # Ore placement (existing)
└── advancement/planets/                # Visit advancements
```

**Deleted Files**:
- `worldgen/density_function/[planet]/*.json` (all custom density functions)
- `worldgen/density_function/[planet]_vegetation.json`

---

## Success Criteria

1. Terrain on all planets looks **identical** to vanilla Overworld
2. Full vanilla cave systems on all planets (unless explicitly disabled)
3. Custom surface blocks work correctly
4. Mob spawning controlled via biome_modifiers
5. Structure generation controlled per-planet
6. PlanetMaker API is simpler and more maintainable
7. `./gradlew makePlanets` generates all required files
8. No compile or runtime errors

---

## Estimated Complexity

- **Phase 1** (Noise Settings): Medium - Core refactoring
- **Phase 2** (Biomes): Medium - Add carvers and features
- **Phase 3** (Biome Modifiers): Medium - New file generation
- **Phase 4** (Structure Control): Low - Tag generation
- **Phase 5** (API Simplification): Low - Removal/cleanup
- **Phase 6** (Gradle Task): Low - Minor updates
- **Phase 7** (Testing): Low - Manual testing

**Total Effort**: Significant refactoring but results in much simpler, more maintainable system
