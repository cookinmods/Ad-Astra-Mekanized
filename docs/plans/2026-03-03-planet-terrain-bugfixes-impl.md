# Planet Terrain Bug Fixes Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix 6 terrain generation bugs affecting all 30 planets — wrong noise defaults, backwards presets, non-functional aquifers, shared cave patterns, and build system gaps.

**Architecture:** All changes are in `PlanetMaker.java` (terrain generation code) and `build.gradle` (cleanup). The planet system generates JSON files via `./gradlew makePlanets`. Changes to Java code produce different JSON output when regenerated.

**Tech Stack:** Java 21, NeoForge 1.21.1, Gradle, Minecraft density function JSON format

**Design doc:** `docs/plans/2026-03-03-planet-terrain-bugfixes-design.md`

---

### Task 1: Fix `base3DNoiseYScale` Default Value

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:485`

**Step 1: Change the default value**

At line 485, change:
```java
private float base3DNoiseYScale = 0.2f;            // Vertical terrain frequency
```
to:
```java
private float base3DNoiseYScale = 0.125f;           // Vertical terrain frequency (vanilla default)
```

**Step 2: Add explicit scale setters to `vanillaQualityStandard()`**

At lines 967-978, add two lines after `this.useVanillaUndergroundFeatures = true;`:
```java
public PlanetBuilder vanillaQualityStandard() {
    this.useVanillaQualityTerrain = true;
    this.useVanillaNoise = true;
    this.useVanillaCaves = true;
    this.useVanillaUndergroundFeatures = true;
    this.base3DNoiseXZScale = 0.25f;        // Explicit vanilla default
    this.base3DNoiseYScale = 0.125f;        // Explicit vanilla default
    this.terrainFactor = 4.0f;
    this.jaggednessNoiseScale = 1500.0f;
    this.base3DNoiseXZFactor = 80.0f;
    this.base3DNoiseYFactor = 160.0f;
    this.smearScaleMultiplier = 8.0f;
    return this;
}
```

Since all other presets call `vanillaQualityStandard()` first, this propagates to all presets automatically.

**Step 3: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
fix: correct base3DNoiseYScale default from 0.2 to 0.125

Vanilla Minecraft uses y_scale=0.125 for base_3d_noise. Our default of
0.2 caused 60% more vertical chaos than intended on all planets.
Also adds explicit scale setters to vanillaQualityStandard() so all
presets inherit correct values.
```

---

### Task 2: Fix Terrain Preset Parameter Values

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:984-1055`

**Context:** The `xz_factor` and `y_factor` parameters in `old_blended_noise` control terrain smoothness. **Higher values = smoother terrain** (counterintuitive). Reference:
- xz_factor 160-200 = flat plains
- xz_factor 100-140 = gentle rolling
- xz_factor 80 = vanilla Overworld
- xz_factor 40-80 = dramatic mountains

**Step 1: Fix `vanillaQualityFlat()` (lines 984-990)**

Change from:
```java
public PlanetBuilder vanillaQualityFlat() {
    vanillaQualityStandard();
    this.terrainFactor = 2.0f;       // Less dramatic terrain
    this.base3DNoiseXZFactor = 40.0f; // Smaller features
    this.jaggednessNoiseScale = 2000.0f;  // Larger mountain spacing
    return this;
}
```
to:
```java
public PlanetBuilder vanillaQualityFlat() {
    vanillaQualityStandard();
    this.terrainFactor = 2.0f;              // Less dramatic terrain
    this.base3DNoiseXZFactor = 180.0f;      // Very smooth horizontally (higher = smoother)
    this.base3DNoiseYFactor = 300.0f;       // Very smooth vertically
    this.jaggednessNoiseScale = 2000.0f;    // Rare mountain peaks
    return this;
}
```

**Step 2: Fix `vanillaQualityMountainous()` (lines 996-1001)**

Change from:
```java
public PlanetBuilder vanillaQualityMountainous() {
    vanillaQualityStandard();
    this.terrainFactor = 6.0f;        // More dramatic terrain
    this.jaggednessNoiseScale = 1000.0f;  // Closer mountain peaks
    this.base3DNoiseXZFactor = 120.0f;    // Larger features
    return this;
}
```
to:
```java
public PlanetBuilder vanillaQualityMountainous() {
    vanillaQualityStandard();
    this.terrainFactor = 6.0f;              // More dramatic terrain
    this.jaggednessNoiseScale = 1000.0f;    // Closer mountain peaks
    this.base3DNoiseXZFactor = 50.0f;       // Dramatic features (lower = bumpier)
    this.base3DNoiseYFactor = 80.0f;        // More vertical variation
    return this;
}
```

**Step 3: Fix `vanillaQualityLunar()` (lines 1036-1044)**

Change from:
```java
public PlanetBuilder vanillaQualityLunar() {
    vanillaQualityStandard();
    this.terrainFactor = 1.8f;           // Subdued terrain, highlands still present
    this.base3DNoiseXZFactor = 35.0f;    // Less surface variation
    this.base3DNoiseYFactor = 60.0f;     // Moderate vertical range
    this.jaggednessNoiseScale = 2500.0f; // Rare peaks
    this.smearScaleMultiplier = 8.0f;    // Max smoothing (codec limit: 1.0-8.0)
    return this;
}
```
to:
```java
public PlanetBuilder vanillaQualityLunar() {
    vanillaQualityStandard();
    this.terrainFactor = 1.8f;              // Subdued terrain height
    this.base3DNoiseXZFactor = 160.0f;      // Smooth rolling plains (higher = smoother)
    this.base3DNoiseYFactor = 300.0f;       // Very smooth vertically
    this.jaggednessNoiseScale = 2500.0f;    // Rare peaks
    this.smearScaleMultiplier = 8.0f;       // Max smoothing (codec limit: 1.0-8.0)
    return this;
}
```

**Step 4: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```
fix: correct backwards terrain preset parameters

vanillaQualityFlat() used xzFactor=40 (mountainous range) instead of
180 (actually flat). vanillaQualityMountainous() used xzFactor=120
(gentle rolling) instead of 50 (dramatic). vanillaQualityLunar() used
xzFactor=35/yFactor=60 (very bumpy) instead of 160/300 (smooth).

Higher factor values = smoother terrain in old_blended_noise math.
```

---

### Task 3: Generate Per-Planet Aquifer Density Functions

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java`
  - Lines ~4951 (`generateCustomDensityFunctions` or `generateVanillaQualityDensityFunctions`)
  - Lines ~5029-5036 (noise router aquifer field writing)

**Context:** Vanilla aquifer noise router fields are density function references, not constants. For aquifer-enabled planets, we need to generate shifted noise density functions for barrier, floodedness, spread, and lava. The vanilla noise parameters are:
- `minecraft:aquifer_barrier` (firstOctave: -3, amplitudes: [1.0])
- `minecraft:aquifer_fluid_level_floodedness` (firstOctave: -7, amplitudes: [1.0])
- `minecraft:aquifer_fluid_level_spread` (firstOctave: -5, amplitudes: [1.0])
- `minecraft:aquifer_lava` (firstOctave: -1, amplitudes: [1.0])

**Step 1: Add aquifer density function generation method**

Add a new method to PlanetMaker (near the other density function generation methods, around line 4940):

```java
/**
 * Generate per-planet aquifer density function files for planets with aquifers enabled.
 * Uses shifted_noise to produce unique aquifer patterns per planet.
 */
private static void generateAquiferDensityFunctions(PlanetBuilder planet, int shiftX, int shiftZ) throws IOException {
    if (!planet.aquifersEnabled) return;

    String path = RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/";

    // Aquifer noise definitions: {fieldName, noiseId, xzScale, yScale}
    String[][] aquiferNoises = {
        {"aquifer_barrier",                  "minecraft:aquifer_barrier",                  "1.0", "0.5"},
        {"aquifer_fluid_level_floodedness",  "minecraft:aquifer_fluid_level_floodedness",  "1.0", "0.67"},
        {"aquifer_fluid_level_spread",       "minecraft:aquifer_fluid_level_spread",       "1.0", "0.7142857142857143"},
        {"aquifer_lava",                     "minecraft:aquifer_lava",                     "1.0", "1.0"}
    };

    for (String[] def : aquiferNoises) {
        String fileName = def[0];
        String noiseId = def[1];
        double xzScale = Double.parseDouble(def[2]);
        double yScale = Double.parseDouble(def[3]);

        // Build shift_x: minecraft:shift_x + planetShift
        JsonObject shiftXObj = new JsonObject();
        shiftXObj.addProperty("type", "minecraft:add");
        shiftXObj.addProperty("argument1", "minecraft:shift_x");
        shiftXObj.addProperty("argument2", (double) shiftX);

        // Build shift_z: minecraft:shift_z + planetShift
        JsonObject shiftZObj = new JsonObject();
        shiftZObj.addProperty("type", "minecraft:add");
        shiftZObj.addProperty("argument1", "minecraft:shift_z");
        shiftZObj.addProperty("argument2", (double) shiftZ);

        // Build shifted_noise
        JsonObject shiftedNoise = new JsonObject();
        shiftedNoise.addProperty("type", "minecraft:shifted_noise");
        shiftedNoise.addProperty("noise", noiseId);
        shiftedNoise.add("shift_x", shiftXObj);
        shiftedNoise.addProperty("shift_y", "minecraft:zero");
        shiftedNoise.add("shift_z", shiftZObj);
        shiftedNoise.addProperty("xz_scale", xzScale);
        shiftedNoise.addProperty("y_scale", yScale);

        writeJsonFile(path + fileName + ".json", shiftedNoise);
    }
}
```

**Important note on xz_scale/y_scale values:** The vanilla noise router in `NoiseRouterData.java` constructs these density functions inline. The exact scale values used by vanilla need to be extracted from the decompiled source. The values above are approximations based on the noise `firstOctave` values. Before implementing, verify by reading `NoiseRouterData.java` — search for `aquifer_barrier` to find the exact `DensityFunctions.noise()` calls and their scale parameters.

**Step 2: Call the new method from `generateVanillaQualityDensityFunctions`**

At the end of `generateVanillaQualityDensityFunctions()` (before the method returns), add:
```java
generateAquiferDensityFunctions(planet, shiftX, shiftZ);
```

**Step 3: Update noise router writing to use density function references for aquifer-enabled planets**

At lines 5029-5036, change the aquifer field writing from:
```java
noiseRouter.addProperty("barrier", planet.barrierNoise);
noiseRouter.addProperty("fluid_level_floodedness", planet.fluidLevelFloodedness);
noiseRouter.addProperty("fluid_level_spread", planet.fluidLevelSpread);
noiseRouter.addProperty("lava", planet.lavaNoise);
```
to:
```java
if (planet.aquifersEnabled) {
    String planetRef = "adastramekanized:" + planet.name;
    noiseRouter.addProperty("barrier",                 planetRef + "/aquifer_barrier");
    noiseRouter.addProperty("fluid_level_floodedness", planetRef + "/aquifer_fluid_level_floodedness");
    noiseRouter.addProperty("fluid_level_spread",      planetRef + "/aquifer_fluid_level_spread");
    noiseRouter.addProperty("lava",                    planetRef + "/aquifer_lava");
} else {
    noiseRouter.addProperty("barrier",                 planet.barrierNoise);
    noiseRouter.addProperty("fluid_level_floodedness", planet.fluidLevelFloodedness);
    noiseRouter.addProperty("fluid_level_spread",      planet.fluidLevelSpread);
    noiseRouter.addProperty("lava",                    planet.lavaNoise);
}
```

**Step 4: Verify exact vanilla aquifer scale values**

Before implementing, read `NoiseRouterData.java` from the decompiled source to extract the exact `xz_scale` and `y_scale` values used for each aquifer noise. Search for `aquifer_barrier` in:
```
build/neoForm/neoFormJoined1.21.1-*/steps/unzipSources/unpacked/net/minecraft/world/level/levelgen/NoiseRouterData.java
```

**Step 5: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Run makePlanets and verify aquifer files generated**

Run: `./gradlew makePlanets`

Verify: Check that aquifer-enabled planets have the new files:
```bash
ls src/main/resources/data/adastramekanized/worldgen/density_function/europa/aquifer_*.json
ls src/main/resources/data/adastramekanized/worldgen/density_function/kepler442b/aquifer_*.json
```
Expected: 4 files per aquifer-enabled planet

Verify: Check that non-aquifer planets do NOT have aquifer files:
```bash
ls src/main/resources/data/adastramekanized/worldgen/density_function/moon/aquifer_*.json 2>&1
```
Expected: "No such file or directory"

Verify: Check noise_settings for an aquifer-enabled planet references the density functions:
```bash
grep "aquifer_barrier" src/main/resources/data/adastramekanized/worldgen/noise_settings/europa.json
```
Expected: `"barrier": "adastramekanized:europa/aquifer_barrier"`

**Step 7: Commit**

```
feat: generate per-planet aquifer density functions

Aquifer-enabled planets now get shifted aquifer noise density functions
instead of hardcoded 0.0 constants. This enables varied underground
water/lava levels in caves for the 14 aquifer-enabled planets.
```

---

### Task 4: Generate Per-Planet Cave Density Functions

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java`
  - Add cave generation method (new code)
  - Lines ~4918 (template replacement in `generateVanillaQualityDensityFunctions`)

**Context:** Vanilla cave density functions are at `data/minecraft/worldgen/density_function/overworld/caves/`. The functions use `minecraft:noise` type for noise sampling. To make caves unique per planet, we replace `minecraft:noise` with `minecraft:shifted_noise` using the planet's coordinate offset, and update all cross-references from `minecraft:overworld/caves/` to `adastramekanized:{planet}/caves/`.

There are 6 cave density function files to generate per planet:
1. `caves/entrances.json` — references `spaghetti_roughness_function`
2. `caves/spaghetti_2d.json` — references `spaghetti_2d_thickness_modulator`
3. `caves/spaghetti_roughness_function.json`
4. `caves/spaghetti_2d_thickness_modulator.json`
5. `caves/pillars.json`
6. `caves/noodle.json`

**Step 1: Create cave density function template strings**

Add static string constants containing the vanilla cave density function JSON. Each template uses placeholder `%PLANET_REF%` for cross-references and `%SHIFT_X%`/`%SHIFT_Z%` for coordinate offsets.

The approach: Store the vanilla JSON as string templates (like `VANILLA_FINAL_DENSITY_TEMPLATE`), with all `minecraft:noise` calls pre-converted to `minecraft:shifted_noise` format with shift placeholders.

For each `minecraft:noise` occurrence in the vanilla JSONs:
```json
{"type": "minecraft:noise", "noise": "minecraft:cave_entrance", "xz_scale": 0.75, "y_scale": 0.5}
```
Convert to:
```json
{
  "type": "minecraft:shifted_noise",
  "noise": "minecraft:cave_entrance",
  "shift_x": {"type": "minecraft:add", "argument1": "minecraft:shift_x", "argument2": %SHIFT_X%},
  "shift_y": "minecraft:zero",
  "shift_z": {"type": "minecraft:add", "argument1": "minecraft:shift_z", "argument2": %SHIFT_Z%},
  "xz_scale": 0.75,
  "y_scale": 0.5
}
```

And replace all `minecraft:overworld/caves/` with `%PLANET_REF%/caves/`.

**Note on `weird_scaled_sampler`:** This type takes a density function `input` and a `noise` reference. The `noise` field is sampled internally at world coordinates and cannot be shifted via JSON. However, the `input` (which controls rarity/where caves form) CAN be shifted. So shift all `minecraft:noise` calls in the input chain — this changes WHERE caves form even though the exact noise shape at each point is the same. This produces meaningfully different cave systems per planet.

**Step 2: Add the cave generation method**

```java
/**
 * Generate per-planet cave density function files with coordinate-shifted noise.
 * This gives each planet unique cave systems instead of sharing the vanilla Overworld pattern.
 */
private static void generateCaveDensityFunctions(PlanetBuilder planet, int shiftX, int shiftZ) throws IOException {
    String path = RESOURCES_PATH + "worldgen/density_function/" + planet.name + "/caves/";
    String planetRef = "adastramekanized:" + planet.name;

    // Generate each cave file from template, replacing placeholders
    String[][] caveFiles = {
        {"entrances.json",                      CAVE_ENTRANCES_TEMPLATE},
        {"spaghetti_2d.json",                   CAVE_SPAGHETTI_2D_TEMPLATE},
        {"spaghetti_roughness_function.json",   CAVE_SPAGHETTI_ROUGHNESS_TEMPLATE},
        {"spaghetti_2d_thickness_modulator.json", CAVE_SPAGHETTI_2D_THICKNESS_TEMPLATE},
        {"pillars.json",                        CAVE_PILLARS_TEMPLATE},
        {"noodle.json",                         CAVE_NOODLE_TEMPLATE}
    };

    for (String[] fileDef : caveFiles) {
        String content = fileDef[1]
            .replace("%PLANET_REF%", planetRef)
            .replace("%SHIFT_X%", String.valueOf((double) shiftX))
            .replace("%SHIFT_Z%", String.valueOf((double) shiftZ));
        writeStringToFile(path + fileDef[0], content);
    }
}
```

**Step 3: Create the 6 template strings**

Each template is the vanilla JSON with:
- All `minecraft:noise` → `minecraft:shifted_noise` with shift placeholders
- All `minecraft:overworld/caves/` → `%PLANET_REF%/caves/`

The templates are large (especially entrances and noodle). Add them as static final String constants near the existing `VANILLA_FINAL_DENSITY_TEMPLATE`.

The exact content for each template should be derived from the vanilla JSON files extracted in the research phase. For each file:

1. **entrances.json** — 8 `minecraft:noise` calls to convert, 1 cross-reference to `spaghetti_roughness_function`
2. **spaghetti_2d.json** — 3 `minecraft:noise` calls, 2 cross-references to `spaghetti_2d_thickness_modulator`
3. **spaghetti_roughness_function.json** — 2 `minecraft:noise` calls, no cross-references
4. **spaghetti_2d_thickness_modulator.json** — 1 `minecraft:noise` call, no cross-references
5. **pillars.json** — 3 `minecraft:noise` calls, no cross-references
6. **noodle.json** — 5 `minecraft:noise` calls, no cross-references

**Step 4: Call cave generation from `generateVanillaQualityDensityFunctions`**

At the end of `generateVanillaQualityDensityFunctions()`, add:
```java
generateCaveDensityFunctions(planet, shiftX, shiftZ);
```

**Step 5: Update `VANILLA_FINAL_DENSITY_TEMPLATE` replacement logic**

At line 4918, change:
```java
String finalDensityJson = VANILLA_FINAL_DENSITY_TEMPLATE
    .replace("minecraft:overworld/sloped_cheese", planetRef + "/sloped_cheese");
```
to:
```java
String finalDensityJson = VANILLA_FINAL_DENSITY_TEMPLATE
    .replace("minecraft:overworld/sloped_cheese", planetRef + "/sloped_cheese")
    .replace("minecraft:overworld/caves/entrances", planetRef + "/caves/entrances")
    .replace("minecraft:overworld/caves/spaghetti_roughness_function", planetRef + "/caves/spaghetti_roughness_function")
    .replace("minecraft:overworld/caves/spaghetti_2d", planetRef + "/caves/spaghetti_2d")
    .replace("minecraft:overworld/caves/pillars", planetRef + "/caves/pillars")
    .replace("minecraft:overworld/caves/noodle", planetRef + "/caves/noodle");
```

**IMPORTANT:** The `spaghetti_roughness_function` replacement MUST come BEFORE `spaghetti_2d` to avoid partial string matching (since `spaghetti_2d` is a substring that could match part of other references). Actually — check if `spaghetti_roughness_function` is the full string in the template, vs just `spaghetti_2d`. If the template uses `minecraft:overworld/caves/spaghetti_2d` as a standalone reference (not as part of a longer string), then order matters. Verify the exact strings in the template before implementing.

**Step 6: Handle cave presets that disable cave types**

For planets with specific cave types disabled (e.g., `enableCheeseCaves=false`, `enableSpaghettiCaves=false`, `enableNoodleCaves=false`), the corresponding cave density functions should output a constant value instead. Modify `generateCaveDensityFunctions()`:

```java
// If spaghetti caves disabled, write constant files
if (!planet.enableSpaghettiCaves) {
    writeStringToFile(path + "spaghetti_2d.json", "1.0");
    writeStringToFile(path + "spaghetti_roughness_function.json", "0.0");
    writeStringToFile(path + "spaghetti_2d_thickness_modulator.json", "0.0");
} else {
    // ... normal template generation
}

if (!planet.enableNoodleCaves) {
    writeStringToFile(path + "noodle.json", "1.0");
} else {
    // ... normal template generation
}
```

Note: A constant of `1.0` for spaghetti_2d and noodle prevents those caves from carving (the min() in final_density will use the terrain value instead). A constant of `0.0` for roughness/thickness means no modulation.

**Step 7: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 8: Run makePlanets and verify cave files generated**

Run: `./gradlew makePlanets`

Verify cave files exist for a terrain planet:
```bash
ls src/main/resources/data/adastramekanized/worldgen/density_function/moon/caves/
```
Expected: 6 files (entrances.json, spaghetti_2d.json, spaghetti_roughness_function.json, spaghetti_2d_thickness_modulator.json, pillars.json, noodle.json)

Verify final_density references planet caves:
```bash
grep "caves/entrances" src/main/resources/data/adastramekanized/worldgen/density_function/moon/final_density.json
```
Expected: `"adastramekanized:moon/caves/entrances"` (NOT `minecraft:overworld/caves/entrances`)

Verify shift values are present in cave files:
```bash
grep "shift_x" src/main/resources/data/adastramekanized/worldgen/density_function/moon/caves/entrances.json | head -1
```
Expected: Contains `5000.0` (moon's coordinate shift)

**Step 9: Commit**

```
feat: generate per-planet cave density functions with coordinate shifting

Each planet now gets 6 unique cave density function files with
coordinate-shifted noise sampling. This replaces the hardcoded
minecraft:overworld/caves/* references that made all planets share
identical cave patterns.

Cave noise types (cave_entrance, spaghetti_3d_*, noodle_*, pillar_*)
are converted from minecraft:noise to minecraft:shifted_noise with
per-planet coordinate offsets. Internal cross-references updated
from minecraft:overworld/caves/ to adastramekanized:{planet}/caves/.
```

---

### Task 5: Update Build System Cleanup

**Files:**
- Modify: `build.gradle:279-288`

**Step 1: Add density_function to cleanup**

At line 287 (inside the `delete fileTree` block), add:
```groovy
include "worldgen/density_function/**/*.json"
```

The full block becomes:
```groovy
delete fileTree(dataDir) {
    include "planets/*.json"
    include "dimension/*.json"
    include "dimension_type/*.json"
    include "worldgen/noise_settings/*.json"
    include "worldgen/configured_feature/*.json"
    include "worldgen/placed_feature/*.json"
    include "worldgen/biome/*.json"
    include "worldgen/density_function/**/*.json"
    include "neoforge/biome_modifier/**/*.json"
}
```

**Step 2: Verify the task runs**

Run: `./gradlew makePlanets --info 2>&1 | head -20`
Expected: Shows cleanup messages including density_function files

**Step 3: Commit**

```
fix: add density_function/ to makePlanets cleanup

Previously, stale density function files persisted across planet
regenerations. Now worldgen/density_function/**/*.json is cleaned
before regenerating, preventing orphaned files from old configurations.
```

---

### Task 6: Delete Legacy Orphaned Files

**Files to delete:**
- `src/main/resources/data/adastramekanized/worldgen/density_function/mars/noise/` (entire directory)
- `src/main/resources/data/adastramekanized/worldgen/density_function/glacio_vegetation.json`
- `src/main/resources/data/adastramekanized/worldgen/density_function/mars_vegetation.json`
- `src/main/resources/data/adastramekanized/worldgen/density_function/mercury_vegetation.json`
- `src/main/resources/data/adastramekanized/worldgen/density_function/venus_vegetation.json`

**Step 1: Verify files are unreferenced**

Search for references to these files anywhere in the codebase:
```bash
grep -r "mars/noise" src/main/resources/ src/main/java/
grep -r "mars_vegetation" src/main/resources/ src/main/java/
grep -r "glacio_vegetation" src/main/resources/ src/main/java/
grep -r "mercury_vegetation" src/main/resources/ src/main/java/
grep -r "venus_vegetation" src/main/resources/ src/main/java/
```
Expected: No matches (files are orphaned)

Also check for legacy noise files for non-existent planets:
```bash
ls src/main/resources/data/adastramekanized/worldgen/noise/ | grep -E "mustafar|hoth|tatooine|dagobah|pandora"
```

**Step 2: Delete orphaned files**

```bash
rm -rf src/main/resources/data/adastramekanized/worldgen/density_function/mars/noise/
rm src/main/resources/data/adastramekanized/worldgen/density_function/glacio_vegetation.json
rm src/main/resources/data/adastramekanized/worldgen/density_function/mars_vegetation.json
rm src/main/resources/data/adastramekanized/worldgen/density_function/mercury_vegetation.json
rm src/main/resources/data/adastramekanized/worldgen/density_function/venus_vegetation.json
```

Delete any legacy noise files found in step 1.

**Step 3: Verify build still works**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
chore: remove orphaned legacy density function and noise files

Removes mars/noise/ directory (raw_continents, raw_erosion, raw_ridges)
and root-level vegetation density functions (glacio, mars, mercury,
venus) that are not referenced by any current noise settings.
```

---

### Task 7: Full Regeneration and Verification

**Step 1: Run full planet regeneration**

Run: `./gradlew makePlanets`
Expected: Generates all planet files without errors

**Step 2: Verify density function file counts**

```bash
# Each terrain planet should have: 11 base files + 6 cave files = 17 files minimum
# Aquifer-enabled planets get 4 more = 21 files
find src/main/resources/data/adastramekanized/worldgen/density_function/moon/ -name "*.json" | wc -l
# Expected: 17 (11 base + 6 caves, no aquifers)

find src/main/resources/data/adastramekanized/worldgen/density_function/europa/ -name "*.json" | wc -l
# Expected: 21 (11 base + 6 caves + 4 aquifer)
```

**Step 3: Verify corrected base_3d_noise values**

```bash
# Moon should have smooth values (xz_factor=160, y_factor=300, y_scale=0.125)
python3 -c "import json; d=json.load(open('src/main/resources/data/adastramekanized/worldgen/density_function/moon/base_3d_noise.json')); print(f'xz_factor={d[\"xz_factor\"]}, y_factor={d[\"y_factor\"]}, y_scale={d[\"y_scale\"]}')"

# Mars (mountainous) should have dramatic values (xz_factor=50, y_factor=80)
python3 -c "import json; d=json.load(open('src/main/resources/data/adastramekanized/worldgen/density_function/mars/base_3d_noise.json')); print(f'xz_factor={d[\"xz_factor\"]}, y_factor={d[\"y_factor\"]}, y_scale={d[\"y_scale\"]}')"

# Venus (flat) should have smooth values (xz_factor=180, y_factor=300)
python3 -c "import json; d=json.load(open('src/main/resources/data/adastramekanized/worldgen/density_function/venus/base_3d_noise.json')); print(f'xz_factor={d[\"xz_factor\"]}, y_factor={d[\"y_factor\"]}, y_scale={d[\"y_scale\"]}')"
```

**Step 4: Verify no orphaned vanilla cave references remain**

```bash
grep -r "minecraft:overworld/caves" src/main/resources/data/adastramekanized/worldgen/ | wc -l
```
Expected: 0 (all should now reference `adastramekanized:{planet}/caves/`)

**Step 5: Full build verification**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit regenerated files**

```
chore: regenerate all planet files with terrain bugfixes

Regenerated via ./gradlew makePlanets with:
- Corrected base3DNoiseYScale (0.125 instead of 0.2)
- Fixed flat/lunar/mountainous preset parameters
- Per-planet aquifer density functions for 14 aquifer-enabled planets
- Per-planet cave density functions with coordinate shifting for all planets
```

---

### Task 8: Update CLAUDE.md Documentation

**Files:**
- Modify: `CLAUDE.md` (project root)

**Step 1: Update the preset parameter table**

Find the "Preset Parameter Values" table in CLAUDE.md and update to:

| Preset | terrainFactor | jaggedScale | xzFactor | yFactor | smear |
|--------|---------------|-------------|----------|---------|-------|
| Standard | 4.0 | 1500.0 | 80.0 | 160.0 | 8.0 |
| Flat | 2.0 | 2000.0 | 180.0 | 300.0 | 8.0 |
| Mountainous | 6.0 | 1000.0 | 50.0 | 80.0 | 8.0 |
| Alien | 5.0 | 1200.0 | 60.0 | 140.0 | 6.0 |
| Cratered | 3.0 | 800.0 | 100.0 | 160.0 | 4.0 |
| Lunar | 1.8 | 2500.0 | 160.0 | 300.0 | 8.0 |
| Archipelago | 4.0 | 1500.0 | 50.0 | 160.0 | 8.0 |

**Step 2: Update the "Generated Density Function Files" section**

Add the new cave and aquifer files to the per-planet file listing:
```
worldgen/density_function/{planet}/
├── continents.json
├── erosion.json
├── ridges.json
├── ridges_folded.json
├── offset.json
├── factor.json
├── jaggedness.json
├── depth.json
├── base_3d_noise.json
├── sloped_cheese.json
├── final_density.json
├── caves/
│   ├── entrances.json
│   ├── spaghetti_2d.json
│   ├── spaghetti_roughness_function.json
│   ├── spaghetti_2d_thickness_modulator.json
│   ├── pillars.json
│   └── noodle.json
├── aquifer_barrier.json              (aquifer-enabled planets only)
├── aquifer_fluid_level_floodedness.json  (aquifer-enabled planets only)
├── aquifer_fluid_level_spread.json       (aquifer-enabled planets only)
└── aquifer_lava.json                     (aquifer-enabled planets only)
```

**Step 3: Add a note about the xz_factor/y_factor direction**

Add under the "Configurable Terrain Parameters" section:
```
**IMPORTANT:** Higher xz_factor and y_factor values produce SMOOTHER terrain.
Lower values produce BUMPIER, more dramatic terrain. This is counterintuitive
but follows from how old_blended_noise math divides coordinates by these factors.
```

**Step 4: Commit**

```
docs: update CLAUDE.md with corrected terrain parameters and new file listings
```
