# Lunar Terrain & Crater Feature Design

**Goal:** Make Moon terrain flat/plains-like with impact craters; apply craters to all cratered rocky bodies

**Architecture:** New `vanillaQualityLunar()` terrain preset + custom `CraterFeature` placed feature system

## Part A: Lunar Terrain Preset

### New Preset: `vanillaQualityLunar()`
```java
public PlanetBuilder vanillaQualityLunar() {
    vanillaQualityStandard();
    this.terrainFactor = 1.8f;           // Moderate terrain (highlands exist but subdued)
    this.base3DNoiseXZFactor = 35.0f;    // Some surface features, not dramatic
    this.base3DNoiseYFactor = 60.0f;     // Moderate vertical range
    this.jaggednessNoiseScale = 2500.0f; // Rare peaks
    this.smearScaleMultiplier = 10.0f;   // Smooth transitions
    return this;
}
```

### Changes
- Moon switches from `vanillaQualityFlat()` to `vanillaQualityLunar()`
- Keeps notable highland terrain but smoother/flatter than vanilla Overworld

## Part B: Crater Feature System

### New Java Classes

#### CraterConfiguration.java
- Codec-based feature configuration
- Parameters: min_radius, max_radius, depth, rim_height, floor_block, rim_block

#### CraterFeature.java
- Extends `Feature<CraterConfiguration>`
- Bowl-shaped terrain carving algorithm:
  1. Pick surface position via heightmap
  2. Choose random radius within configured range
  3. For each (x,z) within radius * 1.3:
     - Center zone (0-70% radius): Flat crater floor at full depth
     - Wall zone (70-100%): Sloping wall from floor to surface
     - Rim zone (100-130%): Raised rim above surface
  4. Replace blocks with configured floor/rim materials

#### ModFeatures.java
- DeferredRegister<Feature<?>> for feature registration
- Register CraterFeature with event bus

### Configured Features (3 size variants)

| Variant | Radius | Depth | Rim Height | Frequency |
|---------|--------|-------|------------|-----------|
| Small crater | 4-8 | 3-5 | 1-2 | ~1 in 3 chunks |
| Medium crater | 10-16 | 5-8 | 2-3 | ~1 in 12 chunks |
| Large crater | 20-30 | 8-14 | 3-5 | ~1 in 64 chunks |

### Planet Distribution

| Planet | Floor Block | Rim Block | Small | Medium | Large |
|--------|-------------|-----------|-------|--------|-------|
| Moon | moon_stone | moon_sand | 1/3 | 1/12 | 1/64 |
| Mercury | gray_terracotta | light_gray_terracotta | 1/2 | 1/8 | 1/48 |
| Callisto | end_stone | end_stone | 1/3 | 1/12 | 1/64 |
| Ganymede | stone | gravel | 1/4 | 1/16 | 1/80 |
| Ceres | stone | gravel | 1/4 | 1/16 | 1/80 |

### Integration with PlanetMaker

Add builder methods:
```java
.enableCraters(true)                    // Enable crater generation
.craterFloorBlock("adastramekanized:moon_stone")
.craterRimBlock("adastramekanized:moon_sand")
.craterFrequency("heavy"|"moderate"|"light")
```

PlanetMaker generates the configured_feature and placed_feature JSON files per planet,
and adds them to the biome features list (step 9: vegetal decoration or step 10).

## Files to Create/Modify

### New Files
- `src/main/java/.../worldgen/feature/CraterConfiguration.java`
- `src/main/java/.../worldgen/feature/CraterFeature.java`
- `src/main/java/.../common/registry/ModFeatures.java`

### Modified Files
- `PlanetMaker.java` — add vanillaQualityLunar(), crater builder methods, crater feature generation
- `PlanetGenerationRunner.java` — update Moon to use vanillaQualityLunar(), add crater config to 5 planets
- `AdAstraMekanized.java` — register ModFeatures on event bus
