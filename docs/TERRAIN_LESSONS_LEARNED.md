# Terrain Generation Lessons Learned

Hard-won knowledge from multiple failed and successful attempts at customizing Minecraft 1.21.1 terrain generation for Ad Astra Mekanized planets.

## The Two-Layer Terrain System

Minecraft terrain is driven by two independent layers that many modders confuse:

### Layer 1: Splines (Macro Terrain Shape)

The vanilla spline files control the **large-scale terrain shape** — mountains, valleys, ocean basins, plateaus:

| File | Size | Controls |
|------|------|----------|
| `offset.json` | ~60KB | Surface height (Y35-Y250+ range) |
| `factor.json` | ~34KB | Terrain compression (higher = flatter features) |
| `jaggedness.json` | ~11KB | Mountain peak sharpness (0 = no peaks) |

These are nested cubic spline lookup tables that map `continentalness × erosion × ridges` to terrain parameters. They are the **primary driver** of terrain shape.

**Key insight:** You cannot make flat terrain by only tuning base_3d_noise parameters. As long as vanilla splines are active, they will produce mountains regardless of noise settings.

### Layer 2: base_3d_noise (Micro Terrain Variation)

The `old_blended_noise` function adds **small-scale 3D variation** — gentle hills, bumps, overhangs:

| Parameter | Vanilla | Effect |
|-----------|---------|--------|
| `xz_factor` | 80.0 | Horizontal smoothing (**higher = smoother**) |
| `y_factor` | 160.0 | Vertical smoothing (**higher = smoother**) |
| `xz_scale` | 0.25 | Horizontal frequency |
| `y_scale` | 0.125 | Vertical frequency |
| `smear_scale_multiplier` | 8.0 | Terrain smoothing (range 1.0-8.0) |

### The Formula

```
sloped_cheese = terrainFactor * quarter_negative(
    (depth + jaggedness * half_negative(jagged_noise)) * factor
) + base_3d_noise
```

- **Splines** control: depth (via offset), factor, jaggedness
- **base_3d_noise** is simply added on top
- **terrainFactor** scales the spline-driven term

## Lesson 1: Factor Values Are Counterintuitive

**Higher `xz_factor` and `y_factor` = SMOOTHER terrain.**
**Lower `xz_factor` and `y_factor` = BUMPIER terrain.**

This is the opposite of what you'd expect. The math divides coordinates by these factors, so larger divisors = coarser sampling = smoother result.

### The Mistake (March 2026)

All preset parameters were backwards:
- `vanillaQualityFlat()` used `xz_factor=40.0` (actually mountainous range)
- `vanillaQualityMountainous()` used `xz_factor=120.0` (actually gentle rolling)

### The Fix

Swap the values:
- `vanillaQualityFlat()`: `xz_factor=180.0` (actually smooth)
- `vanillaQualityMountainous()`: `xz_factor=50.0` (actually dramatic)

## Lesson 2: y_scale Default Was Wrong

**Vanilla y_scale is 0.125, not 0.2.**

PlanetMaker's default `base3DNoiseYScale` was 0.2, causing 60% more vertical noise variation across ALL planets. This created chaotic overhangs and unnatural vertical terrain features.

Always explicitly set `y_scale = 0.125` in every terrain preset.

## Lesson 3: Tuning the Wrong Layer

### The Failed Attempt (December 2025)

**Goal:** Make the Moon flat with gentle hills.
**Approach:** Created `vanillaQualityUltraFlat()` that replaced vanilla splines with constants.
**Result:** Completely featureless flat terrain — reverted same day.
**Why it failed:** The constant splines worked, but base_3d_noise was ALSO too smooth (`xz_factor=180`, `y_factor=300`), leaving nothing to create hills.

### The Second Failed Attempt (March 2026)

**Goal:** Same — flat Moon with gentle hills.
**Approach:** Fixed backwards factor values and y_scale, but kept vanilla splines.
**Result:** Still "huge peaks and spikes" — mountains from splines dominated everything.
**Why it failed:** Smoothing base_3d_noise can't override spline-driven mountains. Wrong layer.

### The Successful Approach (March 2026)

**Constant splines + moderate base_3d_noise:**
- offset.json = constant `-0.50375` (flat surface at Y63)
- factor.json = constant `6.0` (high compression)
- jaggedness.json = constant `0.0` (no peaks)
- base_3d_noise: `xz_factor=100`, `y_factor=200`, `terrainFactor=3.0`

Result: Flat baseline with gentle rolling hills of ±10 blocks. Exactly what was wanted.

**The key insight:** For flat terrain, replace splines with constants AND give base_3d_noise moderate parameters so it provides the gentle hills. Neither alone works — constants without noise = featureless, noise without constant splines = mountains.

## Lesson 4: Cave Frequency Is Ignored for Vanilla-Quality Planets

The `addCavePreset()` method sets `caveFrequency` which affects threshold-based cave generation in `createCaveDensity()`. But for planets using `useVanillaQualityTerrain`, the `VANILLA_FINAL_DENSITY_TEMPLATE` generates its own cave system with hardcoded density functions. The `caveFrequency` parameter is never applied.

**Implication:** Noise caves (cheese, spaghetti, noodle, entrances) always run at vanilla frequency for vanilla-quality planets. The `caveFrequency` only affects carver-based caves.

**For surface-accessible caves:** Use the `surface_accessible` cave preset which sets carver frequency to vanilla levels (1.0). Combined with constant splines giving predictable surface height, entrance caves reliably intersect the surface.

## Lesson 5: Constant Spline JSON Structure

Minecraft requires specific wrapper structures for density functions used in the noise router. Constant splines must be wrapped in `flat_cache > cache_2d > blend` to support chunk border blending:

```json
// Constant offset (surface at Y63)
{
  "type": "minecraft:flat_cache",
  "argument": {
    "type": "minecraft:cache_2d",
    "argument": {
      "type": "minecraft:add",
      "argument1": {
        "type": "minecraft:mul",
        "argument1": { "type": "minecraft:blend_offset" },
        "argument2": /* (1 - blend_alpha) term */
      },
      "argument2": {
        "type": "minecraft:mul",
        "argument1": -0.50375,  /* configurable surface offset */
        "argument2": { "type": "minecraft:cache_once", "argument": { "type": "minecraft:blend_alpha" } }
      }
    }
  }
}
```

The `blend_offset` / `blend_alpha` wrapping ensures smooth transitions at chunk borders when adjacent to other dimensions. Without this, you get hard edges at dimension boundaries.

## Lesson 6: Crater Features Are Post-Terrain

Craters are placed features in the `local_modifications` generation step (step 2 of 11). They carve into already-generated terrain using CraterFeature, which creates:
- Floor zone (0-70% radius): flat bottom
- Wall zone (70-100%): cosine-interpolated slope
- Rim zone (100-130%): raised rim above surface

**On flat terrain, crater rims become the most prominent feature.** Heavy crater frequency (1/2 small chance) can dominate the landscape. For barren planets with constant splines, use `sparse` or `minimal` frequency so craters are accent features, not terrain-defining.

### Crater Frequency Reference

| Preset | Small | Medium | Large |
|--------|-------|--------|-------|
| heavy | 1/2 | 1/8 | 1/48 |
| moderate | 1/3 | 1/12 | 1/64 |
| light | 1/4 | 1/16 | 1/80 |
| sparse | 1/8 | 1/32 | 1/128 |
| minimal | 1/16 | 1/64 | 1/256 |

## Lesson 7: Offset-to-Y Mapping

The terrain offset value maps to surface height via:

```
surface_Y ≈ 128 + offset_value * 128
```

| Offset | Approx Y |
|--------|----------|
| -0.50375 | Y63 (sea level) |
| -0.40 | Y77 |
| -0.30 | Y90 |
| 0.0 | Y128 |

Use `constantSurfaceHeight(int y)` which handles this conversion automatically.

## Lesson 8: smear_scale_multiplier Crashes Above 8.0

The `old_blended_noise` codec has a hard range of [1.0, 8.0] for `smear_scale_multiplier`. Values outside this range crash the game during registry loading. Always clamp this value.

## Quick Reference: Making Flat Terrain

1. Use `vanillaQualityBarren()` preset (does everything below automatically)
2. Or manually:
   - Call `useConstantSplines(true)` — replaces splines with flat constants
   - Set `constantSurfaceHeight(63)` — or desired Y level
   - Set `terrainFactor = 3.0` — enough for visible hills
   - Set `base3DNoiseXZFactor = 100.0` — moderate horizontal smoothing
   - Set `base3DNoiseYFactor = 200.0` — smooth vertically (no overhangs)
   - Set `smearScaleMultiplier = 6.0` — moderate smoothing
3. Use `addCavePreset("surface_accessible")` for reachable caves
4. Use `enableCraters("sparse")` or `"minimal"` for accent craters
5. Run `./gradlew makePlanets` then `./gradlew build`

## Quick Reference: Parameter Tuning

| Problem | Fix |
|---------|-----|
| Too flat / featureless | Increase terrainFactor (3.0 → 4.0) |
| Too bumpy | Increase xz_factor (100 → 140) |
| Overhangs / floating terrain | Increase y_factor (200 → 300) |
| Hills too far apart | Decrease xz_factor (100 → 70) |
| Hills too close together | Increase xz_factor (100 → 130) |
| Still has mountains | Verify useConstantSplines is true (splines must be replaced) |
| No caves visible | Use surface_accessible preset, verify not using minimal_airless |
| Too many craters | Use sparse or minimal frequency |
