# Barren Terrain Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make asteroid-like planets (Moon, Mercury, Ceres, Enceladus, Callisto, Pluto, Eris) flat with gentle hills and surface-accessible caves by replacing vanilla splines with constants and letting base_3d_noise drive gentle terrain variation.

**Architecture:** Replace the 60KB+ vanilla spline lookup tables (offset/factor/jaggedness) with simple constant values for barren worlds. The terrain formula `sloped_cheese = terrainFactor * quarter_negative((depth + jaggedness * half_negative(jagged)) * factor) + base_3d_noise` then simplifies to a flat baseline + gentle 3D noise hills. Cave system uses vanilla noise caves at full frequency with predictable surface intersection from the constant surface height.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft density function JSON, PlanetMaker builder API

**Key Reference:** `mc-terrain-generation` skill documents constant spline JSON format, terrain formula, and cave system internals.

---

## Background

### Why Previous Attempts Failed

1. **March 2026 fix** tuned `base_3d_noise` parameters (xz_factor, y_factor) but left vanilla splines intact. Splines still drove mountain-scale terrain (Y35-Y250+) regardless of noise tuning.
2. **December 2025 ultra-flat splines** replaced splines with constants but ALSO made base_3d_noise too smooth (xz_factor=180, y_factor=300), producing featureless plains.
3. **Cave frequency** from `minimal_airless` preset (0.1) is ignored for vanilla-quality planets — the `VANILLA_FINAL_DENSITY_TEMPLATE` has its own hardcoded cave system.

### The Fix

Constant splines (flat baseline) + moderate base_3d_noise (gentle hills) + vanilla noise caves at full frequency (surface-accessible with predictable surface height).

---

## Task 1: Add Constant Spline Fields to PlanetBuilder

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:447-465` (field declarations)

**Step 1: Add boolean flag and offset field**

After the existing terrain mode flags (around line 458), add:

```java
// Constant spline mode - replaces vanilla spline lookups with flat constants
// When true, offset/factor/jaggedness are simple constants instead of 60KB+ spline files
// base_3d_noise becomes the sole source of terrain variation (gentle hills)
private boolean useConstantSplines = false;
private float constantSurfaceOffset = -0.50375f; // Default: surface at Y63 (vanilla GLOBAL_OFFSET)
```

**Step 2: Add setter methods**

After the existing terrain preset methods (around line 1079), add:

```java
/**
 * Enable constant spline mode for flat/barren terrain.
 * Replaces vanilla's complex spline lookup tables with simple constants.
 * base_3d_noise becomes the sole source of terrain variation.
 */
public PlanetBuilder useConstantSplines(boolean enabled) {
    this.useConstantSplines = enabled;
    return this;
}

/**
 * Set the surface height for constant-spline planets.
 * Converts a Y level to the internal offset value.
 * @param y The desired surface Y level (e.g., 63 for sea level, 80 for elevated)
 */
public PlanetBuilder constantSurfaceHeight(int y) {
    this.constantSurfaceOffset = (y - 128) / 128.0f;
    return this;
}
```

**Step 3: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (new fields are unused but that's fine)

**Step 4: Commit**

```
feat: add useConstantSplines flag and constantSurfaceHeight API to PlanetBuilder
```

---

## Task 2: Generate Constant Spline JSON Files

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:4870-4883` (spline generation)

**Step 1: Add constant spline JSON generation methods**

Add these private helper methods near the `generateVanillaQualityDensityFunctions()` method (around line 4790):

```java
/**
 * Generate constant offset.json for flat terrain.
 * The blend structure is required for proper chunk border blending.
 * @param surfaceOffset The offset value (e.g., -0.50375 for Y63)
 */
private static String generateConstantOffsetJson(float surfaceOffset) {
    return """
    {
      "type": "minecraft:flat_cache",
      "argument": {
        "type": "minecraft:cache_2d",
        "argument": {
          "type": "minecraft:add",
          "argument1": {
            "type": "minecraft:mul",
            "argument1": { "type": "minecraft:blend_offset" },
            "argument2": {
              "type": "minecraft:add",
              "argument1": 1.0,
              "argument2": {
                "type": "minecraft:mul",
                "argument1": -1.0,
                "argument2": { "type": "minecraft:cache_once", "argument": { "type": "minecraft:blend_alpha" } }
              }
            }
          },
          "argument2": {
            "type": "minecraft:mul",
            "argument1": %s,
            "argument2": { "type": "minecraft:cache_once", "argument": { "type": "minecraft:blend_alpha" } }
          }
        }
      }
    }
    """.formatted(surfaceOffset);
}

/**
 * Generate constant factor.json for flat terrain.
 * Effective factor = 6.0 (high compression = sharp ground/air boundary).
 */
private static String generateConstantFactorJson() {
    return """
    {
      "type": "minecraft:flat_cache",
      "argument": {
        "type": "minecraft:cache_2d",
        "argument": {
          "type": "minecraft:add",
          "argument1": 10.0,
          "argument2": {
            "type": "minecraft:mul",
            "argument1": { "type": "minecraft:blend_alpha" },
            "argument2": -4.0
          }
        }
      }
    }
    """;
}

/**
 * Generate zero jaggedness.json (no mountain peaks).
 */
private static String generateConstantJaggednessJson() {
    return """
    {
      "type": "minecraft:flat_cache",
      "argument": {
        "type": "minecraft:cache_2d",
        "argument": 0.0
      }
    }
    """;
}
```

**Step 2: Modify spline generation to use constants when flag is set**

Replace the spline copying code at lines 4870-4883. Change from:

```java
// 5. Copy and transform offset.json (replace minecraft:overworld/ with planet ref)
String offsetContent = readFileToString(templatePath + "vanilla_offset.json");
offsetContent = offsetContent.replace("minecraft:overworld/", planetRef + "/");
writeStringToFile(path + "offset.json", offsetContent);

// 6. Copy and transform factor.json
String factorContent = readFileToString(templatePath + "vanilla_factor.json");
factorContent = factorContent.replace("minecraft:overworld/", planetRef + "/");
writeStringToFile(path + "factor.json", factorContent);

// 7. Copy and transform jaggedness.json
String jaggednessContent = readFileToString(templatePath + "vanilla_jaggedness.json");
jaggednessContent = jaggednessContent.replace("minecraft:overworld/", planetRef + "/");
writeStringToFile(path + "jaggedness.json", jaggednessContent);
```

To:

```java
if (planet.useConstantSplines) {
    // Constant splines for flat/barren terrain
    // base_3d_noise becomes the sole source of terrain variation
    writeStringToFile(path + "offset.json", generateConstantOffsetJson(planet.constantSurfaceOffset));
    writeStringToFile(path + "factor.json", generateConstantFactorJson());
    writeStringToFile(path + "jaggedness.json", generateConstantJaggednessJson());
} else {
    // Full vanilla splines for complex terrain
    String offsetContent = readFileToString(templatePath + "vanilla_offset.json");
    offsetContent = offsetContent.replace("minecraft:overworld/", planetRef + "/");
    writeStringToFile(path + "offset.json", offsetContent);

    String factorContent = readFileToString(templatePath + "vanilla_factor.json");
    factorContent = factorContent.replace("minecraft:overworld/", planetRef + "/");
    writeStringToFile(path + "factor.json", factorContent);

    String jaggednessContent = readFileToString(templatePath + "vanilla_jaggedness.json");
    jaggednessContent = jaggednessContent.replace("minecraft:overworld/", planetRef + "/");
    writeStringToFile(path + "jaggedness.json", jaggednessContent);
}
```

**Step 3: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
feat: add constant spline JSON generation for flat/barren terrain
```

---

## Task 3: Add vanillaQualityBarren() Preset

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:1071-1079` (after vanillaQualityLunar)

**Step 1: Add the preset method**

After `vanillaQualityLunar()` (line 1079), add:

```java
/**
 * Barren asteroid/moon terrain preset.
 * Uses constant splines (flat baseline) with moderate base_3d_noise for gentle hills.
 * Surface height is configurable via constantSurfaceHeight().
 * No mountains, no deep valleys — just gentle rolling terrain.
 *
 * Expected terrain: flat baseline ~Y63, gentle hills +/- 10 blocks.
 *
 * Parameters tuned specifically for the "constant splines + noise hills" approach:
 * - terrainFactor 3.0: enough amplitude for visible hills from base_3d_noise
 * - xz_factor 100.0: moderate horizontal smoothing (not too smooth, not too bumpy)
 * - y_factor 200.0: smooth vertically to avoid overhangs on barren worlds
 * - smear 6.0: moderate smoothing for natural-looking hills
 */
public PlanetBuilder vanillaQualityBarren() {
    vanillaQualityStandard();
    this.useConstantSplines = true;
    this.terrainFactor = 3.0f;
    this.base3DNoiseXZFactor = 100.0f;
    this.base3DNoiseYFactor = 200.0f;
    this.jaggednessNoiseScale = 2000.0f;
    this.smearScaleMultiplier = 6.0f;
    return this;
}
```

**Step 2: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add vanillaQualityBarren() terrain preset for asteroid-like planets
```

---

## Task 4: Add surface_accessible Cave Preset

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:2941-2955` (addCavePreset switch)

**Step 1: Add the cave preset**

In the `addCavePreset()` switch statement, add a new case after `minimal_airless` (after line 2954):

```java
case "surface_accessible":
    // Full vanilla cave frequency for barren planets with constant splines.
    // Constant splines make surface height predictable, so entrance caves
    // consistently intersect the surface creating visible openings.
    // Vanilla noise caves (cheese, spaghetti, noodle) run at full frequency
    // since the VANILLA_FINAL_DENSITY_TEMPLATE controls noise cave generation.
    // Carvers add physical tunnels from the surface.
    caveConfig(1.0f, 1.0f);    // Full vanilla frequency (for carver-based caves)
    cheeseCaves(true);
    spaghettiCaves(true);
    noodleCaves(true);
    caveHeightRange(-64, 256);
    ravineConfig(0.05f, 2.0f); // Moderate ravines (surface cracks/skylights)
    break;
```

**Step 2: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add surface_accessible cave preset for barren planets
```

---

## Task 5: Add sparse Crater Frequency

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:7213-7221` (crater rarity switch)

**Step 1: Add sparse case to the crater frequency switch**

In the crater rarity switch statement (around line 7213), add a new case:

```java
case "sparse":
    smallRarity = 8; mediumRarity = 32; largeRarity = 128;
    break;
```

The full switch should now be:

```java
switch (planet.craterFrequency) {
    case "heavy":
        smallRarity = 2; mediumRarity = 8; largeRarity = 48;
        break;
    case "light":
        smallRarity = 4; mediumRarity = 16; largeRarity = 80;
        break;
    case "sparse":
        smallRarity = 8; mediumRarity = 32; largeRarity = 128;
        break;
    default: // moderate
        smallRarity = 3; mediumRarity = 12; largeRarity = 64;
        break;
}
```

**Step 2: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add sparse crater frequency option for barren planets
```

---

## Task 6: Update PlanetGenerationRunner — Switch 7 Planets

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java`

**Step 1: Update Moon (lines ~277-328)**

Change:
- `.vanillaQualityLunar()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`
- If craters are enabled, change frequency to `"light"`

**Step 2: Update Mercury (lines ~544-590)**

Change:
- `.vanillaQualityCratered()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`
- `.enableCraters("heavy")` → `.enableCraters("light")`

**Step 3: Update Callisto (lines ~748-808)**

Change:
- `.vanillaQualityCratered()` → `.vanillaQualityBarren()`
- `.addCavePreset("balanced_vanilla")` → `.addCavePreset("surface_accessible")`
- `.enableCraters()` (default/moderate) → `.enableCraters("light")`

**Step 4: Update Enceladus (lines ~859-897)**

Change:
- `.vanillaQualityCratered()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`

**Step 5: Update Ceres (lines ~951-1010)**

Change:
- `.vanillaQualityCratered()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`
- `.enableCraters("light")` → `.enableCraters("sparse")`

**Step 6: Update Pluto (lines ~1014-1053)**

Change:
- `.vanillaQualityFlat()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`

**Step 7: Update Eris (lines ~1057-1093)**

Change:
- `.vanillaQualityFlat()` → `.vanillaQualityBarren()`
- `.addCavePreset("minimal_airless")` → `.addCavePreset("surface_accessible")`

**Step 8: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 9: Commit**

```
feat: switch 7 barren planets to vanillaQualityBarren preset

Moon, Mercury, Ceres, Enceladus, Callisto, Pluto, Eris now use
constant splines + moderate noise for flat terrain with gentle hills,
surface_accessible caves, and reduced crater frequency.
```

---

## Task 7: Regenerate Planet Files and Verify

**Step 1: Run planet generation**

Run: `./gradlew makePlanets`
Expected: All 31 planets regenerate successfully

**Step 2: Verify constant spline files were generated**

Check that the 7 barren planets have small constant spline files instead of 60KB+ vanilla splines:

```bash
wc -c src/main/resources/data/adastramekanized/worldgen/density_function/moon/offset.json
```

Expected: ~500-800 bytes (NOT 60,000+ bytes)

**Step 3: Verify other planets still have full splines**

```bash
wc -c src/main/resources/data/adastramekanized/worldgen/density_function/mars/offset.json
```

Expected: ~60,000+ bytes (vanilla splines intact)

**Step 4: Verify Moon's base_3d_noise.json has correct parameters**

Check the file contains:
- `"xz_factor": 100.0`
- `"y_factor": 200.0`
- `"xz_scale": 0.25`
- `"y_scale": 0.125`
- `"smear_scale_multiplier": 6.0`

**Step 5: Clean build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```
chore: regenerate all planet files with barren terrain preset
```

---

## Task 8: In-Game Verification

**Step 1: Launch development client**

Run: `./gradlew runClient`

**Step 2: Test Moon terrain**

```
/planet teleport moon
```

Verify:
- [ ] Terrain is mostly flat with gentle rolling hills
- [ ] No mountain peaks or deep valleys
- [ ] Surface height is roughly consistent (~Y63 +/- 10)
- [ ] No huge spikes or unnatural formations

**Step 3: Test Moon caves**

Explore the Moon surface looking for:
- [ ] Visible cave entrances in hillsides
- [ ] Surface holes/skylights leading to caves below
- [ ] Cave system accessible without digging

**Step 4: Test Moon craters (if enabled)**

- [ ] Craters are occasional landmarks, not terrain-defining
- [ ] Crater rims don't create extreme spikes

**Step 5: Test other barren planets**

```
/planet teleport mercury
/planet teleport ceres
/planet teleport pluto
```

Verify each has similar flat+gentle terrain.

**Step 6: Test non-barren planets still work**

```
/planet teleport mars
/planet teleport venus
/planet teleport io
```

Verify these still have their intended dramatic terrain.

---

## Parameter Tuning Guide

If the terrain doesn't look right after initial implementation, here's how to adjust:

| Problem | Fix | Parameter |
|---------|-----|-----------|
| Too flat / featureless | Increase terrainFactor | 3.0 → 4.0 |
| Too bumpy / chaotic | Increase xz_factor | 100.0 → 140.0 |
| Overhangs / floating terrain | Increase y_factor | 200.0 → 300.0 |
| Hills too far apart | Decrease xz_factor | 100.0 → 70.0 |
| Hills too close together | Increase xz_factor | 100.0 → 130.0 |
| Surface too low | Use `.constantSurfaceHeight(80)` | offset |
| Surface too high | Use `.constantSurfaceHeight(50)` | offset |
| No visible caves | Check carver settings | cave preset |
| Too many caves / swiss cheese | Reduce ravineConfig frequency | 0.05 → 0.02 |

**Important**: After any parameter change, run `./gradlew makePlanets` then `./gradlew build` before testing.

---

## Root Cause Reference

### Why Vanilla Splines Cause Mountains

The vanilla `offset.json` (60KB) is a nested cubic spline that maps `continentalness × erosion × ridges` to surface height. Output range: -0.22 to 0.96, which maps to Y35-Y250+. Even with smooth base_3d_noise, these splines produce mountain-scale features because `depth = y_gradient + offset` drives the surface height.

### Why Constant Splines + Noise Works

With constant offset, the surface height is fixed. The formula simplifies:
```
sloped_cheese = terrainFactor * quarter_negative(depth * factor) + base_3d_noise
```
The `depth * factor` term creates a sharp ground/air boundary at the constant offset height. `base_3d_noise` then adds gentle variation to this boundary, creating hills and valleys of ±10 blocks.

### Why Caves Become Accessible

The vanilla entrance cave formula `0.37 + yClampedGradient(-10, 30)` targets near-surface cave generation. With constant splines, the surface is always at ~Y63, so entrance caves consistently form between Y53-Y93 and reliably punch through the terrain surface.
