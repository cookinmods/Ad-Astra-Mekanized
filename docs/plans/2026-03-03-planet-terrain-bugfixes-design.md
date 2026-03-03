# Planet Terrain Generation Bug Fixes

**Date:** 2026-03-03
**Scope:** Fix terrain generation bugs affecting all 30 terrain planets
**Priority:** Bug fixes only — no new features, no biome diversity changes

## Summary

Six bugs in PlanetMaker.java produce incorrect terrain for all planets:

1. Wrong `base3DNoiseYScale` default (0.2 vs vanilla 0.125) — 60% more vertical chaos
2. Preset methods don't explicitly set noise scales, inheriting bad defaults
3. Several presets have backwards factor values (low factor = bumpier, not smoother)
4. Aquifer noise router fields hardcoded to 0.0 — aquifers non-functional
5. All planets share identical cave patterns (hardcoded overworld references)
6. `makePlanets` task doesn't clean density_function/ directory

## Bug 1: `base3DNoiseYScale` Default

**File:** `PlanetMaker.java:485`
**Current:** `private float base3DNoiseYScale = 0.2f;`
**Fix:** `private float base3DNoiseYScale = 0.125f;`

**Additionally:** All 7 preset methods must explicitly set both scale values:
```java
this.base3DNoiseXZScale = 0.25f;
this.base3DNoiseYScale = 0.125f;
```

This applies to: `vanillaQualityStandard()`, `vanillaQualityFlat()`, `vanillaQualityMountainous()`,
`vanillaQualityAlien()`, `vanillaQualityCratered()`, `vanillaQualityLunar()`, `vanillaQualityArchipelago()`

## Bug 2: Preset Parameter Corrections

**Reference (from mc-terrain-generation skill):**
- xz_factor 160-200 = flat plains
- xz_factor 100-140 = gentle rolling
- xz_factor 80 = vanilla Overworld
- xz_factor 40-80 = dramatic mountains
- Higher y_factor = smoother vertically

### vanillaQualityFlat() — lines 984-990
**Problem:** `xzFactor=40` is in the *mountainous* range, not flat.
```java
// BEFORE (wrong)
this.terrainFactor = 2.0f;
this.base3DNoiseXZFactor = 40.0f;    // This is mountainous!
this.jaggednessNoiseScale = 2000.0f;

// AFTER (correct)
this.terrainFactor = 2.0f;           // Keep low terrain factor
this.base3DNoiseXZFactor = 180.0f;   // Actually flat (high = smooth)
this.base3DNoiseYFactor = 300.0f;    // Very smooth vertically
this.jaggednessNoiseScale = 2000.0f; // Keep rare peaks
```

### vanillaQualityMountainous() — lines 996-1001
**Problem:** `xzFactor=120` is in the *gentle rolling* range, not dramatic.
```java
// BEFORE (too gentle for "mountainous")
this.terrainFactor = 6.0f;
this.jaggednessNoiseScale = 1000.0f;
this.base3DNoiseXZFactor = 120.0f;   // This is gentle rolling!

// AFTER (actually mountainous)
this.terrainFactor = 6.0f;           // Keep high terrain factor
this.jaggednessNoiseScale = 1000.0f; // Keep close peaks
this.base3DNoiseXZFactor = 50.0f;    // Dramatic features (low = bumpier)
this.base3DNoiseYFactor = 80.0f;     // More vertical variation
```

### vanillaQualityLunar() — lines 1036-1044
**Problem:** `xzFactor=35, yFactor=60` creates MORE variation, not less.
```java
// BEFORE (backwards — creates bumpy terrain)
this.terrainFactor = 1.8f;
this.base3DNoiseXZFactor = 35.0f;    // Very bumpy!
this.base3DNoiseYFactor = 60.0f;     // Very chaotic vertically!
this.jaggednessNoiseScale = 2500.0f;
this.smearScaleMultiplier = 8.0f;

// AFTER (smooth lunar terrain)
this.terrainFactor = 1.8f;           // Keep subdued height
this.base3DNoiseXZFactor = 160.0f;   // Smooth rolling plains
this.base3DNoiseYFactor = 300.0f;    // Very smooth vertically
this.jaggednessNoiseScale = 2500.0f; // Keep rare peaks
this.smearScaleMultiplier = 8.0f;    // Keep max smoothing
```

### Presets kept as-is:
- **vanillaQualityAlien()**: xzFactor=60, yFactor=140, smear=6 — mid-range chaos fits alien intent
- **vanillaQualityCratered()**: xzFactor=100, smear=4 — gentle rolling + crater features works
- **vanillaQualityArchipelago()**: xzFactor=50 — smaller landmasses fits island worlds
- **vanillaQualityStandard()**: xzFactor=80, yFactor=160 — vanilla-accurate

## Bug 3: Aquifer Noise Router Fields

**File:** `PlanetMaker.java:471-474, 5029-5036`
**Problem:** `barrierNoise`, `fluidLevelFloodedness`, `fluidLevelSpread`, `lavaNoise` all default to `0.0f`.
Written directly as constants into noise settings. Aquifer system produces uniform flat water levels.

### Fix: Generate per-planet aquifer density functions

For each planet with `aquifersEnabled(true)` (14 planets), generate 4 additional density function files:
```
worldgen/density_function/{planet}/
├── aquifer_barrier.json
├── aquifer_fluid_level_floodedness.json
├── aquifer_fluid_level_spread.json
└── aquifer_lava.json
```

Each file uses `shifted_noise` with the planet's coordinate shift, referencing the appropriate vanilla noise parameter:
- `minecraft:aquifer_barrier` noise
- `minecraft:aquifer_fluid_level_floodedness` noise
- `minecraft:aquifer_fluid_level_spread` noise
- `minecraft:aquifer_lava` noise

In noise router generation (line 5029-5036), when `aquifersEnabled`:
```java
// Instead of constant 0.0:
noiseRouter.add("barrier", reference("adastramekanized:" + planet.id + "/aquifer_barrier"));
noiseRouter.add("fluid_level_floodedness", reference("adastramekanized:" + planet.id + "/aquifer_fluid_level_floodedness"));
noiseRouter.add("fluid_level_spread", reference("adastramekanized:" + planet.id + "/aquifer_fluid_level_spread"));
noiseRouter.add("lava", reference("adastramekanized:" + planet.id + "/aquifer_lava"));
```

When `aquifersEnabled(false)`: keep `0.0` constants (no change needed).

### Planets affected:
Europa, Titan, Kepler22b, Kepler442b, Proxima B, Trappist1E, Gliese667C, Paludis, Luxoria, Glacio, Terra Nova, Primordium

## Bug 4: Per-Planet Cave Density Functions

**File:** `PlanetMaker.java:29-169 (VANILLA_FINAL_DENSITY_TEMPLATE), line 4918`
**Problem:** Template contains 6 hardcoded `minecraft:overworld/caves/*` references. Only `sloped_cheese` is replaced per-planet. All planets share identical cave layouts.

### Fix: Generate 5 cave density function files per planet

For each planet using vanilla-quality terrain, generate:
```
worldgen/density_function/{planet}/caves/
├── entrances.json
├── spaghetti_2d.json
├── spaghetti_roughness.json
├── pillars.json
└── noodle.json
```

Each file copies the vanilla overworld cave density function structure but applies the planet's coordinate shift to noise sampling coordinates.

### Template replacement update (line 4918):

```java
String finalDensityJson = VANILLA_FINAL_DENSITY_TEMPLATE
    .replace("minecraft:overworld/sloped_cheese", planetRef + "/sloped_cheese")
    .replace("minecraft:overworld/caves/entrances", planetRef + "/caves/entrances")
    .replace("minecraft:overworld/caves/spaghetti_2d", planetRef + "/caves/spaghetti_2d")
    .replace("minecraft:overworld/caves/spaghetti_roughness_function", planetRef + "/caves/spaghetti_roughness")
    .replace("minecraft:overworld/caves/pillars", planetRef + "/caves/pillars")
    .replace("minecraft:overworld/caves/noodle", planetRef + "/caves/noodle");
```

### Cave preset interaction:
- Planets with `enableCheeseCaves=false`: cheese cave section uses constant 0.0
- Planets with `enableSpaghettiCaves=false`: spaghetti/roughness files omitted, replaced with 0.0
- Planets with `enableNoodleCaves=false`: noodle file omitted, replaced with constant
- Planets with cave preset "none": all cave references become 0.0 constants, no cave files generated

### Vanilla cave density function sources:
Extract from the Minecraft JAR:
```bash
find ~/.gradle/caches -name "*.jar" -path "*minecraft*1.21.1*" | head -1
# Extract: data/minecraft/worldgen/density_function/overworld/caves/
```

## Bug 5: Build System Cleanup

**File:** `build.gradle:264-330`

Add density function cleanup to `makePlanets` task's `doFirst` block:
```groovy
delete fileTree(dataDir) {
    include "planets/*.json", "dimension/*.json", "dimension_type/*.json",
            "worldgen/noise_settings/*.json", "worldgen/configured_feature/*.json",
            "worldgen/placed_feature/*.json", "worldgen/biome/*.json",
            "worldgen/density_function/**/*.json",  // <-- ADD THIS
            "neoforge/biome_modifier/**/*.json"
}
```

## Bug 6: Legacy File Cleanup

Delete orphaned files not referenced by any current system:

1. `worldgen/density_function/mars/noise/` directory (raw_continents.json, raw_erosion.json, raw_ridges.json)
2. Root-level vegetation density functions:
   - `worldgen/density_function/glacio_vegetation.json`
   - `worldgen/density_function/mars_vegetation.json`
   - `worldgen/density_function/mercury_vegetation.json`
   - `worldgen/density_function/venus_vegetation.json`
3. Any orphaned noise files in `worldgen/noise/` for non-existent planets (mustafar, hoth, tatooine, dagobah, pandora)

## Execution Order

1. Fix `base3DNoiseYScale` default (1 line change)
2. Fix all preset methods (add explicit scales + correct factor values)
3. Implement aquifer density function generation
4. Implement per-planet cave density function generation + template replacements
5. Update build.gradle cleanup scope
6. Delete legacy files manually
7. Run `./gradlew makePlanets` to regenerate all planet files
8. Run `./gradlew build` to verify compilation
9. Visual testing in-game for key planets (moon, mars, kepler442b)

## Risk Assessment

| Change | Risk | Mitigation |
|--------|------|------------|
| y_scale default | Low | Aligns with vanilla; all planets improve |
| Preset corrections | Low | Based on verified parameter reference table |
| Aquifer generation | Medium | Only affects 14 aquifer-enabled planets; test Europa/Kepler442b |
| Cave generation | Medium-High | Structural template change; test caves on 2-3 planets |
| Build cleanup | Low | Only affects regeneration, no runtime change |
| Legacy deletion | Low | Files confirmed unreferenced |

## CLAUDE.md Updates Needed

After implementation, update the preset parameter table in CLAUDE.md to reflect corrected values:

| Preset | terrainFactor | jaggedScale | xzFactor | yFactor | smear |
|--------|---------------|-------------|----------|---------|-------|
| Standard | 4.0 | 1500.0 | 80.0 | 160.0 | 8.0 |
| Flat | 2.0 | 2000.0 | 180.0 | 300.0 | 8.0 |
| Mountainous | 6.0 | 1000.0 | 50.0 | 80.0 | 8.0 |
| Alien | 5.0 | 1200.0 | 60.0 | 140.0 | 6.0 |
| Cratered | 3.0 | 800.0 | 100.0 | 160.0 | 4.0 |
| Lunar | 1.8 | 2500.0 | 160.0 | 300.0 | 8.0 |
| Archipelago | 4.0 | 1500.0 | 50.0 | 160.0 | 8.0 |
